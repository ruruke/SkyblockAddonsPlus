//package moe.ruruke.skyblock.asm
//
//import moe.ruruke.skyblock.asm.utils.InjectionHelper
//import moe.ruruke.skyblock.tweaker.transformer.ITransformer
//import org.objectweb.asm.Opcodes
//import org.objectweb.asm.tree.*
//
//class PlayerControllerMPTransformer : ITransformer {
//    override var className: Array<String> = arrayOf()
//        /**
//         * [net.minecraft.client.multiplayer.PlayerControllerMP]
//         */
//        get() = arrayOf<String>(TransformerClass.PlayerControllerMP.transformerName)
//
//    override fun transform(classNode: ClassNode?, name: String?) {
//        for (methodNode in classNode!!.methods) {
//            if (InjectionHelper.matches(methodNode, TransformerMethod.onPlayerDestroyBlock)) {
//                InjectionHelper.start()
//                    .matchingOwner(TransformerClass.World).matchingMethod(TransformerMethod.playAuxSFX).endCondition()
//
//                    .injectCodeBefore()
//                    .load(InstructionBuilder.VariableType.OBJECT, 1) // loc
//                    // PlayerControllerMPHook.onPlayerDestroyBlock(loc);
//                    .callStaticMethod(
//                        "moe/ruruke/skyblock/asm/hooks/PlayerControllerMPHook",
//                        "onPlayerDestroyBlock",
//                        "(" + TransformerClass.BlockPos.getName() + ")V"
//                    )
//                    .endCode()
//                    .finish()
//            } else if (TransformerMethod.windowClick.matches(methodNode)) {
//                // Objective:
//                // Find: Method head.
//                // Insert:   ReturnValue returnValue = new ReturnValue();
//                //           PlayerControllerMPHook.onWindowClick(slotId, mode, playerIn, returnValue);
//                //           if (returnValue.isCancelled()) {
//                //               return null;
//                //           }
//
//                methodNode.instructions.insertBefore(methodNode.instructions.first, insertOnWindowClick())
//            } else if (TransformerMethod.resetBlockRemoving.matches(methodNode)) {
//                methodNode.instructions.insertBefore(
//                    methodNode.instructions.first,
//                    MethodInsnNode(
//                        Opcodes.INVOKESTATIC,
//                        "moe/ruruke/skyblock/asm/hooks/PlayerControllerMPHook",
//                        "onResetBlockRemoving",
//                        "()V",
//                        false
//                    )
//                )
//            }
//        }
//    }
//
//    private fun insertOnWindowClick(): InsnList {
//        val list = InsnList()
//
//        list.add(TypeInsnNode(Opcodes.NEW, "moe/ruruke/skyblock/asm/utils/ReturnValue"))
//        list.add(InsnNode(Opcodes.DUP)) // ReturnValue returnValue = new ReturnValue();
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESPECIAL,
//                "moe/ruruke/skyblock/asm/utils/ReturnValue",
//                "<init>",
//                "()V",
//                false
//            )
//        )
//        list.add(VarInsnNode(Opcodes.ASTORE, 8))
//
//        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // slotId
//        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // mouseButtonClicked
//        list.add(VarInsnNode(Opcodes.ILOAD, 4)) // mode
//        list.add(VarInsnNode(Opcodes.ALOAD, 5)) // playerIn
//        list.add(
//            VarInsnNode(
//                Opcodes.ALOAD,
//                8
//            )
//        ) // PlayerControllerMPHook.onWindowClick(slotId, mouseButtonClicked, mode, playerIn, returnValue);
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "moe/ruruke/skyblock/asm/hooks/PlayerControllerMPHook",
//                "onWindowClick",
//                "(III" + TransformerClass.EntityPlayer.getName() + "Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
//                false
//            )
//        )
//
//        list.add(VarInsnNode(Opcodes.ALOAD, 8))
//        list.add(
//            MethodInsnNode(
//                Opcodes.INVOKEVIRTUAL, "moe/ruruke/skyblock/asm/utils/ReturnValue", "isCancelled",
//                "()Z", false
//            )
//        )
//        val notCancelled = LabelNode() // if (returnValue.isCancelled())
//        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))
//
//        list.add(InsnNode(Opcodes.ACONST_NULL)) // return null;
//        list.add(InsnNode(Opcodes.ARETURN))
//        list.add(notCancelled)
//
//        return list
//    }
//}
