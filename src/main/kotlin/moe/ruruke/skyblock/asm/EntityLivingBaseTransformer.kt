package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class EntityLivingBaseTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.entity.EntityLivingBase]
         */
        get() = arrayOf(TransformerClass.EntityLivingBase.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.handleStatusUpdate.matches(methodNode)) {
                // Objective:
                // Find: this.hurtTime =
                // Insert After: EntityLivingBaseHook.onResetHurtTime();

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is FieldInsnNode && abstractNode.getOpcode() == Opcodes.PUTFIELD) {
                        val fieldInsnNode = abstractNode
                        if (fieldInsnNode.owner == TransformerClass.EntityLivingBase.nameRaw &&
                            TransformerField.hurtTime.matches(fieldInsnNode)
                        ) {
                            methodNode.instructions.insert(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/EntityLivingBaseHook",
                                    "onResetHurtTime", "(" + TransformerClass.EntityLivingBase.getName() + ")V", false
                                )
                            )
                            methodNode.instructions.insert(abstractNode, VarInsnNode(Opcodes.ALOAD, 0))
                        }
                    }
                }
            } else if (TransformerMethod.removePotionEffectClient.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, onRemovePotionEffect())
            } else if (TransformerMethod.addPotionEffect.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, onAddPotionEffect())
            }
        }
    }

    private fun onRemovePotionEffect(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // int potionId
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/EntityLivingBaseHook",
                "onRemovePotionEffect",
                "(" + TransformerClass.EntityLivingBase.getName() + "I)Z",
                false
            )
        )
        val notCancelled = LabelNode() // if (EntityLivingBaseHook.onRemovePotionEffect(this, potionId)) {
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled) // }

        return list
    }

    private fun onAddPotionEffect(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(
            VarInsnNode(
                Opcodes.ALOAD,
                1
            )
        ) // PotionEffect potioneffectIn // EntityLivingBaseHook.onAddPotionEffect(this, potioneffectIn);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/EntityLivingBaseHook",
                "onAddPotionEffect",
                "(" + TransformerClass.EntityLivingBase.getName() + TransformerClass.PotionEffect.getName() + ")V",
                false
            )
        )

        return list
    }
}
