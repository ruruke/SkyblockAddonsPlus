package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ItemArmorDispenserBehaviorTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.item.ItemArmor]#dispenserBehavior
         */
        get() = arrayOf(TransformerClass.ItemArmor.transformerName + "$1")

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.dispenseStack.matches(methodNode)) {
                // Objective:
                // Replace "int l = 0;"
                // with: "int l = entitylivingbase instanceof EntityPlayer?1:0;"

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_0) {
                        if (iterator.hasNext()) {
                            val nextAbstractNode = iterator.next()

                            if (nextAbstractNode is VarInsnNode && nextAbstractNode.getOpcode() == Opcodes.ISTORE) {
                                methodNode.instructions.insertBefore(abstractNode, revertVanillaBehavior())

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

    private fun revertVanillaBehavior(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 9)) // entitylivingbase
        list.add(TypeInsnNode(Opcodes.INSTANCEOF, TransformerClass.EntityPlayer.nameRaw))
        val notEqual = LabelNode() // if (entitylivingbase instanceof EntityPlayer) {
        list.add(JumpInsnNode(Opcodes.IFEQ, notEqual))

        list.add(InsnNode(Opcodes.ICONST_1)) // 1
        val afterCondition = LabelNode()
        list.add(JumpInsnNode(Opcodes.GOTO, afterCondition))

        list.add(notEqual) // } else {
        list.add(InsnNode(Opcodes.ICONST_0)) // 0

        list.add(afterCondition) // }
        list.add(VarInsnNode(Opcodes.ISTORE, 10)) // l =

        return list
    }
}
