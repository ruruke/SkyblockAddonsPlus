package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.hooks.utils.TransformerClass
import moe.ruruke.skyblock.asm.hooks.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class RendererLivingEntityTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.renderer.entity.RendererLivingEntity]
         */
        get() = arrayOf(TransformerClass.RendererLivingEntity.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.rotateCorpse.matches(methodNode)) {
                // Objective:
                // Find: s.equals("Dinnerbone");
                // Replace RendererLivingEntityHook.equals(s, "Dinnerbone");

                // Objective 2:
                // Find: ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE);
                // Replace RendererLivingEntityHook.isWearing(((EntityPlayer)bat), EnumPlayerModelParts.CAPE;

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == "java/lang/String" && methodInsnNode.name == "equals") {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/RendererLivingEntityHook",
                                    "equals",
                                    "(Ljava/lang/String;Ljava/lang/Object;)Z",
                                    false
                                )
                            ) // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove() // Remove the old line.
                        } else if (methodInsnNode.owner == TransformerClass.EntityPlayer.nameRaw && TransformerMethod.isWearing.matches(
                                methodInsnNode
                            )
                        ) {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/RendererLivingEntityHook",
                                    "isWearing",
                                    "(" + TransformerClass.EntityPlayer.getName() + TransformerClass.EnumPlayerModelParts.getName() + ")Z",
                                    false
                                )
                            ) // RendererLivingEntityHook.equals(s, "Dinnerbone");

                            iterator.remove() // Remove the old line.
                            break
                        }
                    }
                }
            } else if (TransformerMethod.setScoreTeamColor.matches(methodNode)) {
                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is VarInsnNode && abstractNode.getOpcode() == Opcodes.ILOAD) {
                        methodNode.instructions.insertBefore(abstractNode, setOutlineColor())
                        break
                    }
                }
            }
        }
    }

    private fun setOutlineColor(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // entityLivingBaseIn
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // i
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/RendererLivingEntityHook",
                "setOutlineColor",
                "(" + TransformerClass.EntityLivingBase.getName() + "I)I",
                false
            )
        )
        list.add(VarInsnNode(Opcodes.ISTORE, 2)) // i

        return list
    }
}
