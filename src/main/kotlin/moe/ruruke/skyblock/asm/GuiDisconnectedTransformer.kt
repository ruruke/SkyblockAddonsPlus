package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode

class GuiDisconnectedTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.gui.GuiDisconnected]
         */
        get() = arrayOf(TransformerClass.GuiDisconnected.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod._init.matches(methodNode)) {
                // Objective:
                // Find: Constructor return.
                // Insert: GuiDisconnectedHook.onDisconnect();

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(
                            abstractNode, MethodInsnNode(
                                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiDisconnectedHook",
                                "onDisconnect", "()V", false
                            )
                        ) // GuiDisconnectedHook.onDisconnect();
                        break
                    }
                }
            }
        }
    }
}
