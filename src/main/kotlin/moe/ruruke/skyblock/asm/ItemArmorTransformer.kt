package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode

class ItemArmorTransformer : ITransformer {
    /**
     * [net.minecraft.item.ItemArmor]
     */
    override fun getClassName(): Array<String> {
        return arrayOf(TransformerClass.ItemArmor.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.onItemRightClick.matches(methodNode)) {
                // Objective:
                // Remove "+ 1"

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_1) {
                        if (iterator.hasNext()) {
                            val nextAbstractNode = iterator.next()

                            if (nextAbstractNode is InsnNode && nextAbstractNode.getOpcode() == Opcodes.IADD) {
                                methodNode.instructions.remove(abstractNode)
                                methodNode.instructions.remove(nextAbstractNode)
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}
