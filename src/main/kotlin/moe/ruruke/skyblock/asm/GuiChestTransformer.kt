package moe.ruruke.skyblock.asm

import moe.ruruke.skyblock.asm.utils.TransformerClass
import moe.ruruke.skyblock.asm.utils.TransformerField
import moe.ruruke.skyblock.asm.utils.TransformerMethod
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class GuiChestTransformer : ITransformer {
    override val className: Array<String?>?
        /**
         * [net.minecraft.client.gui.inventory.GuiChest]
         */
        get() = arrayOf(TransformerClass.GuiChest.transformerName)


    override fun transform(classNode: ClassNode?, name: String?) {
        // Objective: Add:
        //
        // @Override
        // public updateScreen() {
        //     GuiChestHook.updateScreen();
        // }

        val updateScreen = TransformerMethod.updateScreen.createMethodNode()
        updateScreen.instructions.add(updateScreen())
        classNode!!.methods.add(updateScreen)

        // Objective: Add:
        //
        // @Override
        // public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //     super.drawScreen(mouseX, mouseY, partialTicks);
        //     GuiChestHook.drawScreen(this.guiLeft, this.guiTop);
        // }
        val drawScreen = TransformerMethod.drawScreen.createMethodNode()
        drawScreen.instructions.add(drawScreen())
        classNode!!.methods.add(drawScreen)

        // Objective: Add:
        //
        // @Override
        // public initGui() {
        //     super.initGui();
        //     GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);
        // }
        val initGui = TransformerMethod.initGui.createMethodNode()
        initGui.instructions.add(initGui())
        classNode!!.methods.add(initGui)

        // Objective: Add:
        //
        // @Override
        // public keyTyped(char typedChar, int keyCode) throws IOException {
        //     if (GuiChestHook.keyTyped(typedChar, keyCode)) {
        //         super.keyTyped(typedChar, keyCode);
        //     }
        // }
        val keyTyped = TransformerMethod.keyTyped.createMethodNode()
        keyTyped.instructions.add(keyTyped())
        classNode!!.methods.add(keyTyped)

        // Objective: Add:
        //
        // @Override
        // public void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj, returnValue);
        //     if (returnValue.isCancelled()) {
        //         return;
        //     }
        //     super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        // }
        val handleMouseClick = TransformerMethod.handleMouseClick.createMethodNode()
        handleMouseClick.instructions.add(handleMouseClick())
        classNode!!.methods.add(handleMouseClick)

        // Objective: Add:
        //
        // @Override
        // public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton, returnValue);
        //     if (returnValue.isCancelled()) {
        //         return;
        //     }
        //     super.mouseClicked(mouseX, mouseY, mouseButton);
        // }
        val mouseClicked = TransformerMethod.mouseClicked.createMethodNode()
        mouseClicked.instructions.add(mouseClicked())
        classNode!!.methods.add(mouseClicked)

        // Objective: Add:
        //
        // @Override
        // public void mouseReleased(int mouseX, int mouseY, int state) {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiChestHook.mouseReleased(mouseX, mouseY, state, returnValue);
        //     if (returnValue.isCancelled()) {
        //         return;
        //     }
        //     super.mouseReleased(mouseX, mouseY, state);
        // }
        val mouseReleased = TransformerMethod.mouseReleased.createMethodNode()
        mouseReleased.instructions.add(mouseReleased())
        classNode!!.methods.add(mouseReleased)

        // Objective: Add:
        //
        // @Override
        // public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiChestHook.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, returnValue);
        //     if (returnValue.isCancelled()) {
        //         return;
        //     }
        //     super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        // }
        val mouseClickMove = TransformerMethod.mouseClickMove.createMethodNode()
        mouseClickMove.instructions.add(mouseClickMove())
        classNode!!.methods.add(mouseClickMove)

        for (methodNode in classNode!!.methods) { // Loop through all methods inside of the class.
            if (TransformerMethod.drawGuiContainerBackgroundLayer.matches(methodNode)) {
                // Objective:
                // Find: GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                // Replace With: GuiChestHook.color(1.0F, 1.0F, 1.0F, 1.0F, this.lowerChestInventory);

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.GlStateManager.nameRaw && methodInsnNode.name == TransformerMethod.color._name) {
                            methodNode.instructions.insertBefore(
                                abstractNode,
                                VarInsnNode(Opcodes.ALOAD, 0)
                            ) // this.lowerChestInventory
                            methodNode.instructions.insertBefore(
                                abstractNode,
                                TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest)
                            )

                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook",
                                    "color", "(FFFF" + TransformerClass.IInventory.getName() + ")V", false
                                )
                            )

                            // GuiChestHook.color(1.0F, 1.0F, 1.0F, 1.0F);
                            iterator.remove() // Remove the old line.
                            break
                        }
                    }
                }
            } else if (TransformerMethod.drawGuiContainerForegroundLayer.matches(methodNode)) {
                // Objective:
                // Find:
                // this.fontRendererObj.drawString(this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
                // this.fontRendererObj.drawString(this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
                //
                // Replace With:
                // GuiChestHook.drawString(this.fontRendererObj, this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
                // GuiChestHook.drawString(this.fontRendererObj, this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

                methodNode.instructions.insertBefore(methodNode.instructions.first, onRenderChestForegroundLayer())

                val iterator: MutableIterator<AbstractInsnNode> = methodNode.instructions.iterator()
                while (iterator.hasNext()) {
                    val abstractNode = iterator.next()
                    if (abstractNode is MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        val methodInsnNode = abstractNode
                        if (methodInsnNode.owner == TransformerClass.FontRenderer.nameRaw && methodInsnNode.name == TransformerMethod.drawString._name) {
                            methodNode.instructions.insertBefore(
                                abstractNode, MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "moe/ruruke/skyblock/asm/hooks/GuiChestHook",
                                    "drawString",
                                    "(" + TransformerClass.FontRenderer.getName() + "Ljava/lang/String;III)I",
                                    false
                                )
                            )

                            // GuiChestHook.drawString(this.fontRendererObj, this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, ..., 4210752);
                            iterator.remove() // Remove the old line. Don't break because we need to do this to two lines.
                        }
                    }
                }
            }
        }
    }

    private fun onRenderChestForegroundLayer(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiChestHook",
                "onRenderChestForegroundLayer",
                "(" + TransformerClass.GuiChest.getName() + ")V",
                false
            )
        ) // GuiChestHook.onRenderChestForegroundLayer(this);

        return list
    }

    private fun updateScreen(): InsnList {
        val list = InsnList()

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "updateScreen",
                "()V", false
            )
        ) // GuiChestHook.updateScreen();

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.nameRaw, TransformerMethod.updateScreen._name,
                "()V", false
            )
        ) // super.updateScreen();

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun drawScreen(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 4))

        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(VarInsnNode(Opcodes.ALOAD, 4)) // returnValue
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "drawScreenIslands",
                "(IILmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        ) // GuiChestHook.drawScreenIslands(returnValue);

        list.add(VarInsnNode(Opcodes.ALOAD, 4)) // returnValue
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
        list.add(InsnNode(Opcodes.RETURN))
        list.add(notCancelled)

        list.add(
            FrameNode(
                Opcodes.F_APPEND,
                1,
                arrayOf<Any>("moe/ruruke/skyblock/asm/utils/ReturnValue"),
                0,
                null
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(VarInsnNode(Opcodes.FLOAD, 3)) // partialTicks // super.drawScreen(mouseX, mouseY, partialTicks);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.drawScreen._name,
                "(IIF)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.guiLeft
        list.add(TransformerField.guiLeft.getField(TransformerClass.GuiChest))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.guiTop
        list.add(TransformerField.guiTop.getField(TransformerClass.GuiChest))

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "drawScreen",
                "(II)V", false
            )
        ) // GuiChestHook.drawScreen(this.guiLeft, this.guiTop);

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun initGui(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // super.initGui();
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.initGui._name,
                "()V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.lowerChestInventory
        list.add(TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.guiLeft
        list.add(TransformerField.guiLeft.getField(TransformerClass.GuiChest))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.guiTop
        list.add(TransformerField.guiTop.getField(TransformerClass.GuiChest))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.fontRendererObj
        list.add(TransformerField.fontRendererObj.getField(TransformerClass.GuiChest))

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiChestHook",
                "initGui",
                "(" + TransformerClass.IInventory.getName() + "II" + TransformerClass.FontRenderer.getName() + ")V",
                false
            )
        )

        // GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);
        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun keyTyped(): InsnList {
        val list = InsnList()

        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // typedChar
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // keyCode

        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "keyTyped",
                "(CI)Z", false
            )
        ) // GuiChestHook.keyTyped(typedChar, keyCode)
        val notCancelled = LabelNode()
        list.add(JumpInsnNode(Opcodes.IFEQ, notCancelled))

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // typedChar
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // keyCode
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.keyTyped._name,
                "(CI)V",
                false
            )
        )

        list.add(notCancelled)
        list.add(FrameNode(Opcodes.F_SAME, 0, null, 0, null))
        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun handleMouseClick(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 5))

        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // slotIn

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.inventorySlots
        list.add(TransformerField.inventorySlots.getField(TransformerClass.GuiChest))

        list.add(VarInsnNode(Opcodes.ALOAD, 0)) // this.lowerChestInventory
        list.add(TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest))

        list.add(
            VarInsnNode(
                Opcodes.ALOAD,
                5
            )
        ) // EntityPlayerSPHook.handleMouseClick(slotIn, this.inventorySlots, this.lowerChestInventory, returnValue)
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "moe/ruruke/skyblock/asm/hooks/GuiChestHook",
                "handleMouseClick",
                "(" + TransformerClass.Slot.getName() + TransformerClass.Container.getName() + TransformerClass.IInventory.getName() + "Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V",
                false
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 5))
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

        list.add(
            FrameNode(
                Opcodes.F_APPEND,
                1,
                arrayOf<Any>("moe/ruruke/skyblock/asm/utils/ReturnValue"),
                0,
                null
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ALOAD, 1)) // slotIn
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // slotId
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // clickedButton
        list.add(
            VarInsnNode(
                Opcodes.ILOAD,
                4
            )
        ) // clickType // super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.handleMouseClick._name,
                "(" + TransformerClass.Slot.getName() + "III)V",
                false
            )
        )

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun mouseClicked(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 4))

        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // mouseButton
        list.add(VarInsnNode(Opcodes.ALOAD, 4)) // returnValue
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "mouseClicked",
                "(IIILmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        ) // GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton);

        list.add(VarInsnNode(Opcodes.ALOAD, 4))
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

        list.add(
            FrameNode(
                Opcodes.F_APPEND,
                1,
                arrayOf<Any>("moe/ruruke/skyblock/asm/utils/ReturnValue"),
                0,
                null
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(
            VarInsnNode(
                Opcodes.ILOAD,
                3
            )
        ) // mouseButton // super.mouseClicked(mouseX, mouseY, mouseButton, returnValue);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.mouseClicked._name,
                "(III)V",
                false
            )
        )

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun mouseReleased(): InsnList {
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
        list.add(VarInsnNode(Opcodes.ASTORE, 4))

        list.add(VarInsnNode(Opcodes.ALOAD, 4)) // returnValue
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "mouseReleased",
                "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        ) // GuiChestHook.mouseReleased(returnValue);

        list.add(VarInsnNode(Opcodes.ALOAD, 4))
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

        list.add(
            FrameNode(
                Opcodes.F_APPEND,
                1,
                arrayOf<Any>("moe/ruruke/skyblock/asm/utils/ReturnValue"),
                0,
                null
            )
        )

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // state // super.mouseReleased(mouseX, mouseY, state);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.mouseReleased._name,
                "(III)V",
                false
            )
        )

        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    private fun mouseClickMove(): InsnList {
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

        list.add(VarInsnNode(Opcodes.ALOAD, 6)) // returnValue
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESTATIC, "moe/ruruke/skyblock/asm/hooks/GuiChestHook", "mouseClickMove",
                "(Lmoe/ruruke/skyblock/asm/utils/ReturnValue;)V", false
            )
        ) // GuiChestHook.mouseClickMove(returnValue);

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

        list.add(VarInsnNode(Opcodes.ALOAD, 0))
        list.add(VarInsnNode(Opcodes.ILOAD, 1)) // mouseX
        list.add(VarInsnNode(Opcodes.ILOAD, 2)) // mouseY
        list.add(VarInsnNode(Opcodes.ILOAD, 3)) // clickedMouseButton
        list.add(
            VarInsnNode(
                Opcodes.LLOAD,
                4
            )
        ) // timeSinceLastClick // super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        list.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                TransformerClass.GuiContainer.nameRaw,
                TransformerMethod.mouseClickMove._name,
                "(IIIJ)V",
                false
            )
        )


        list.add(InsnNode(Opcodes.RETURN))
        return list
    }

    override fun nameMatches(method: String, vararg names: String): Boolean {
        return super.nameMatches(method, *names)
    }
}
