package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class GuiContainerTransformer : ITransformer {
    override var className: Array<String> = arrayOf()
        /**
         * [net.minecraft.client.gui.inventory.GuiContainer]
         */
        get() = arrayOf(TransformerClass.GuiContainer.transformerName)

    override fun transform(classNode: ClassNode?, name: String?) {
        for (methodNode in classNode!!.methods) {
            if (TransformerMethod.drawScreen.matches(methodNode)) {
                // Objective 1:
                // Find: int l = 240;
                // Add: GuiContainerHook.setLastSlot();

                // Objective 2:
                // Find: this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                // Add: GuiContainerHook.drawGradientRect(this, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433, this.theSlot);

                // Objective 3:
                // Find: this.drawSlot(slot);
                // Add: GuiContainerHook.drawSlot(this, slot);

                // Objective 4:
                // Find: Return statement.
                // Add: GuiContainerHook.drawBackpacks(this, mouseX, mouseY, this.fontRendererObj);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is VarInsnNode && abstractNode.getOpcode() == Opcodes.ISTORE) {
                        if (abstractNode.`var` == 7) {
                            methodNode.instructions.insert(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",
                                    "setLastSlot", "()V", false
                                )
                            ) // GuiContainerHook.setLastSlot();
                        }
                    } else if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.GuiContainer.nameRaw &&
                            TransformerMethod.drawGradientRect.matches(methodInsnNode)
                        ) {
                            methodNode.instructions.insertBefore(abstractNode, VarInsnNode(Opcodes.ALOAD, 0))
                            methodNode.instructions.insertBefore(
                                abstractNode,
                                TransformerField.theSlot.getField(TransformerClass.GuiContainer)
                            ) // this.theSlot

                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",
                                    "drawGradientRect",
                                    "(" + TransformerClass.GuiContainer.getName() + "IIIIII" + TransformerClass.Slot.getName() + ")V",
                                    false
                                )
                            )

                            iterator.remove() // Remove previous call.
                        }
                    } else if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESPECIAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.GuiContainer.nameRaw && TransformerMethod.drawSlot.matches(
                                methodInsnNode
                            )
                        ) {
                            methodNode.instructions.insert(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",
                                    "drawSlot",
                                    "(" + TransformerClass.GuiContainer.getName() + TransformerClass.Slot.getName() + ")V",
                                    false
                                )
                            )

                            methodNode.instructions.insert(abstractNode, VarInsnNode(Opcodes.ALOAD, 9)) // slot

                            methodNode.instructions.insert(abstractNode, VarInsnNode(Opcodes.ALOAD, 0)) // this
                        }
                    } else if (abstractNode is InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertDrawBackpacks())
                    }
                }
            } else if (TransformerMethod.keyTyped.matches(methodNode)) {
                // Objective 1:
                // Find: 2 lines before "this.checkHotbarKeys(keyCode);"
                // Add: ReturnValue returnValue = new ReturnValue();
                //      GuiContainerHook.keyTyped(this, keyCode, this.theSlot, returnValue);
                //      if (returnValue.isCancelled) {
                //          return;
                //      }

                val iterator: Iterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.GuiContainer.nameRaw &&
                            TransformerMethod.checkHotbarKeys.matches(methodInsnNode)
                        ) {
                            methodNode.instructions.insertBefore(abstractNode.getPrevious().previous, insertKeyTyped())
                        }
                    }
                }

                // Objective 2:
                // Find: Method head.
                // Add: GuiContainerHook.keyTyped(keyCode);
                methodNode.instructions.insertBefore(methodNode.instructions.first, insertKeyTypedTwo())
            } else if (TransformerMethod.handleMouseClick.matches(methodNode)) {
                // Objective 1:
                // Find: Method head.
                // Add:
                //     ReturnValue returnValue = new ReturnValue();
                //     GuiInventoryHook.handleMouseClick(this.guiLeft, this.guiTop, this.oldMouseX, this.oldMouseY, this.xSize, this.ySize, returnValue);
                //     if (returnValue.isCancelled()) {
                //         return;
                //     }
                //     super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
                // }

                methodNode.instructions.insertBefore(methodNode.instructions.first, onHandleMouseClick())
            }
        }
    }

    private fun insertKeyTypedTwo(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // keyCode
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",
                "keyTyped", "(I)V", false
            )
        ) // GuiContainerHook.keyTyped(keyCode);

        return list
    }

    private fun insertDrawBackpacks(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this

        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.
        list.add(TransformerField.fontRendererObj.getField(TransformerClass.GuiContainer)) // fontRendererObj
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",  // GuiContainerHook.drawBackpacks(this, this.fontRendererObj);
                "drawBackpacks",
                "(" + TransformerClass.GuiContainer.getName() + "II" + TransformerClass.FontRenderer.getName() + ")V",
                false
            )
        )

        return list
    }

    private fun insertKeyTyped(): InsnList {
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

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // keyCode

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.theSlot
        list.add(TransformerField.theSlot.getField(TransformerClass.GuiContainer))

        list.add(VarInsnNode(Opcodes.ALOAD, 3)) // GuiContainerHook.keyTyped(this, keyCode, this.theSlot, returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiContainerHook",
                "keyTyped",
                "(" + TransformerClass.GuiContainer.getName() + "I" + TransformerClass.Slot.getName() + "Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 3))
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

    private fun onHandleMouseClick(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // slotIn
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // slotId
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // clickedButton
        list.add(VarInsnNode(Opcodes.ILOAD, 4)) // clickType

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiContainerHook", "onHandleMouseClick",
                "(" + TransformerClass.Slot.getName() + "III)Z", false
            )
        )
        val notCancelled =
            LabelNode() // if (GuiContainerHook.onHandleMouseClick(slotIn, slotId, clickedButton, clickType))
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(InsnNode(Opcodes.RETURN)) // return;
        list.add(notCancelled) // }

        return list
    }
}
