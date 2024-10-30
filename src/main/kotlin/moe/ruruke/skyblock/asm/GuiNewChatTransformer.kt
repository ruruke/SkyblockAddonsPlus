package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class GuiNewChatTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.gui.GuiNewChat]
         */
        get() = arrayOf(TransformerClass.GuiNewChat.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.printChatMessageWithOptionalDeletion.matches(methodNode)) {
                // Objective:
                // Find: chatComponent.getUnformattedText();
                // Replace With: GuiNewChatHook.getUnformattedText(chatComponent);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEINTERFACE) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.IChatComponent.nameRaw && TransformerMethod.getUnformattedText.matches(
                                methodInsnNode
                            )
                        ) {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/GuiNewChatHook",
                                    "getUnformattedText",
                                    "(" + TransformerClass.IChatComponent.getName() + ")Ljava/lang/String;",
                                    false
                                )
                            ) // GuiNewChatHook.getUnformattedText(chatComponent);

                            iterator.remove() // Remove the old line.
                            break
                        }
                    }
                }
            }
        }
    }
}
