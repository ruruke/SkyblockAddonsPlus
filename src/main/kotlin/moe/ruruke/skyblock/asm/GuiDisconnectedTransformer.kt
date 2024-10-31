package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode

class GuiDisconnectedTransformer : ITransformer {
    /**
     * [net.minecraft.client.gui.GuiDisconnected]
     */
    override fun getClassName(): Array<String> {
        return  arrayOf(TransformerClass.GuiDisconnected.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.init.matches(methodNode)) {
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
