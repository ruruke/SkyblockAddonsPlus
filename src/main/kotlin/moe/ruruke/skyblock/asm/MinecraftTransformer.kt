package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class MinecraftTransformer : ITransformer {
    /**
     * [net.minecraft.client.Minecraft]
     */
    override fun getClassName(): Array<String> {
        return arrayOf(TransformerClass.Minecraft.getTransformerName())
    }

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.rightClickMouse.matches(methodNode)) {
                // Objective:
                // Find: Before "this.rightClickDelayTimer = 4;"
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           MinecraftHook.rightClickMouse(returnValue);
                //           if (returnValue.isCancelled()) {
                //               return;
                //           }

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_4) {
                        methodNode.instructions.insertBefore(abstractNode.getPrevious(), insertRightClickMouse())
                        break
                    }
                }
            }
            if (TransformerMethod.runTick.matches(methodNode)) {
                // Objective:
                // Insert Before:
                //    this.thePlayer.inventory.currentItem = l;
                //
                // Put:   MinecraftHook.updatedCurrentItem();

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is FieldInsnNode && abstractNode.getOpcode() == Opcodes.PUTFIELD && TransformerField.currentItem.matches(
                            abstractNode
                        )
                    ) {
                        methodNode.instructions.insertBefore(
                            abstractNode.getPrevious().previous.previous.previous,
                            MethodInsnNode(
                                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/MinecraftHook",
                                "updatedCurrentItem", "()V", false
                            )
                        ) // MinecraftHook.updatedCurrentItem();
                        break
                    }
                }
            }
            if (TransformerMethod.clickMouse.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertOnClickMouse())
            }
            if (TransformerMethod.sendClickBlockToController.matches(methodNode)) {
                methodNode.instructions.insertBefore(
                    methodNode.instructions.first,
                    insertOnSendClickBlockToController()
                )
            }
        }
    }

    private fun insertRightClickMouse(): InsnList {
        val list = InsnList()

        list.add(TypeInsnNode(Opcodes.NEW, "moe/ruruke/skyblock/asm/utils/ReturnValue"))
        list.add(InsnNode(Opcodes.DUP)) // ReturnValue returnValue = new ReturnValue();
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "<init>",
                "()V",
                false
            )
        )
        list.add(VarInsnNode(Opcodes.ASTORE, 6))

        list.add(VarInsnNode(Opcodes.ALOAD, 6)) // MinecraftHook.rightClickMouse(returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/MinecraftHook", "rightClickMouse",
                "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 6))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled",
                "()Z", false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled)

        return list
    }

    private fun insertOnClickMouse(): InsnList {
        val list = InsnList()

        list.add(TypeInsnNode(Opcodes.NEW, "moe/ruruke/skyblock/asm/utils/ReturnValue"))
        list.add(InsnNode(Opcodes.DUP)) // ReturnValue returnValue = new ReturnValue();
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "<init>",
                "()V",
                false
            )
        )
        list.add(VarInsnNode(Opcodes.ASTORE, 3))

        list.add(VarInsnNode(Opcodes.ALOAD, 3)) // MinecraftHook.onClickMouse(ReturnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/MinecraftHook", "onClickMouse",
                "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 3))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "isCancelled",
                "()Z",
                false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled)

        return list
    }


    private fun insertOnSendClickBlockToController(): InsnList {
        val list = InsnList()

        list.add(TypeInsnNode(Opcodes.NEW, "moe/ruruke/skyblock/asm/utils/ReturnValue"))
        list.add(InsnNode(Opcodes.DUP)) // ReturnValue returnValue = new ReturnValue();
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "<init>",
                "()V",
                false
            )
        )
        list.add(VarInsnNode(Opcodes.ASTORE, 3))

        list.add(VarInsnNode(Opcodes.ILOAD, 1))
        list.add(VarInsnNode(Opcodes.ALOAD, 3)) // MinecraftHook.onSendClickBlockToController(leftClick, ReturnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/MinecraftHook",
                "onSendClickBlockToController",
                "(ZLmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 3))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "moe/ruruke/skyblock/asm/utils/ReturnValue",
                "isCancelled",
                "()Z",
                false
            )
        )
        val notCancelled = LabelNode() // if (returnValue.isCancelled())
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled)

        return list
    }
}
