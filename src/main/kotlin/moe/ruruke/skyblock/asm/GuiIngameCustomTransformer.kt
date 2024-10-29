package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class GuiIngameCustomTransformer : ITransformer {
    private var foundHealthBlock = false
    private var foundFoodBlock = false

    private var doneHealth = false
    private var doneFood = false
    private var doneArmor = false
    private var doneMountHealth = false

    override var className: Array<String?>? = arrayOf()
        /**
         * Labymod: net.labymod.core_implementation.mc18.gui.GuiIngameCustom
         */
        get() = arrayOf("net.labymod.core_implementation.mc18.gui.GuiIngameCustom")

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (methodNode.name == "renderPlayerStatsNew") {
                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()

                    if (abstractNode is LdcInsnNode) {
                        val ldcInsnNode = abstractNode
                        if (!doneArmor && "armor" == ldcInsnNode.cst) {
                            methodNode.instructions.insert(ldcInsnNode.next, insertCancelArmorRendering())
                            doneArmor = true
                        } else if ("health" == ldcInsnNode.cst) {
                            foundHealthBlock = true
                        } else if ("food" == ldcInsnNode.cst) {
                            foundFoodBlock = true
                        }
                    }

                    if (abstractNode is JumpInsnNode) {
                        val jumpInsnNode = abstractNode
                        if (!doneHealth && foundHealthBlock && abstractNode.getOpcode() == Opcodes.IFLT) {
                            doneHealth = true
                            methodNode.instructions.insert(
                                abstractNode,
                                insertCancelHealthRendering(jumpInsnNode.label)
                            )
                        }

                        if (!doneFood && abstractNode.getOpcode() == Opcodes.IFNONNULL) {
                            doneFood = true
                            methodNode.instructions.insert(abstractNode, insertCancelFoodRendering(jumpInsnNode.label))
                        }
                    }

                    if (!doneMountHealth && foundFoodBlock && abstractNode is TypeInsnNode && abstractNode.getOpcode() == Opcodes.INSTANCEOF) {
                        val typeInsnNode = abstractNode

                        if (typeInsnNode.desc == TransformerClass.EntityLivingBase.nameRaw && typeInsnNode.next.opcode == Opcodes.IFEQ && typeInsnNode.next is JumpInsnNode) {
                            val jumpInsnNode = typeInsnNode.next as JumpInsnNode

                            doneMountHealth = true
                            methodNode.instructions.insert(
                                jumpInsnNode,
                                insertCancelMountHealthRendering(jumpInsnNode.label)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun insertCancelArmorRendering(): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiIngameCustomHook",
                "shouldRenderArmor",
                "()Z",
                false
            )
        )

        val notCancelled = LabelNode() // if (!GuiIngameCustomHook.shouldRenderArmor())
        list.add(JumpInsnNode(Opcodes.IFNE, notCancelled))

        list.add(InsnNode(Opcodes.ICONST_0))
        list.add(VarInsnNode(Opcodes.ISTORE, 22)) // k2 = 0;

        list.add(notCancelled) // }

        return list
    }

    private fun insertCancelHealthRendering(label: LabelNode): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiIngameCustomHook",
                "shouldRenderHealth",
                "()Z",
                false
            )
        )
        list.add(JumpInsnNode(Opcodes.IFEQ, label)) // && shouldRenderHealth()

        return list
    }

    private fun insertCancelFoodRendering(label: LabelNode): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiIngameCustomHook",
                "shouldRenderFood",
                "()Z",
                false
            )
        )
        list.add(JumpInsnNode(Opcodes.IFEQ, label)) // && shouldRenderFood()

        return list
    }

    private fun insertCancelMountHealthRendering(label: LabelNode): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiIngameCustomHook",
                "shouldRenderMountHealth",
                "()Z",
                false
            )
        )
        list.add(JumpInsnNode(Opcodes.IFEQ, label)) // && shouldRenderMountHealth()

        return list
    }
}