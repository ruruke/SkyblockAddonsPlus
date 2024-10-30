package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.utils.ReturnValue
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.features.backpacks.ContainerPreviewManager
import moe.ruruke.skyblock.features.backpacks.ContainerPreviewManager.drawContainerPreviews
import moe.ruruke.skyblock.features.backpacks.ContainerPreviewManager.onContainerKeyTyped
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPattern
import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.ResourceLocation

object GuiContainerHook {
    private val LOCK = ResourceLocation("skyblockaddons", "lock.png")
    private val OVERLAY_RED = ColorCode.RED.getColor(127)
    private val OVERLAY_GREEN = ColorCode.GREEN.getColor(127)

    fun keyTyped(keyCode: Int) {
        onContainerKeyTyped(keyCode)
    }

    fun drawBackpacks(guiContainer: GuiContainer?, mouseX: Int, mouseY: Int, fontRendererObj: FontRenderer?) {
        drawContainerPreviews(guiContainer!!, mouseX, mouseY)
    }

    fun setLastSlot() {
        SkyblockAddonsPlus.utils!!.setLastHoveredSlot(-1)
    }

    fun drawGradientRect(
        guiContainer: GuiContainer,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        startColor: Int,
        endColor: Int,
        theSlot: Slot?
    ) {
        if (ContainerPreviewManager.isFrozen()) {
            return
        }

        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        if (theSlot != null && theSlot.hasStack && main.configValues!!
                .isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) && main.utils!!.isEmptyGlassPane(theSlot.stack)
        ) {
            return
        }
        val container = Minecraft.getMinecraft().thePlayer.openContainer
        if (theSlot != null) {
            val slotNum: Int = theSlot.slotNumber + main.inventoryUtils!!.getSlotDifference(container)
            main.utils!!.setLastHoveredSlot(slotNum)
            if (main.configValues!!.isEnabled(Feature.LOCK_SLOTS) &&
                main.utils!!.isOnSkyblock() && main.configValues!!.getLockedSlots().contains(slotNum)
                && (slotNum >= 9 || container is ContainerPlayer && slotNum >= 5)
            ) {
                guiContainer.drawGradientRect(left, top, right, bottom, OVERLAY_RED, OVERLAY_RED)
                return
            }
        }
        guiContainer.drawGradientRect(left, top, right, bottom, startColor, endColor)
    }

    fun drawSlot(guiContainer: GuiContainer, slot: Slot?) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val mc = Minecraft.getMinecraft()
        val container = mc.thePlayer.openContainer

        if (slot != null) {
            // Draw crafting pattern overlays inside the crafting grid.
            if (false /*main.configValues!!.isEnabled(Feature.CRAFTING_PATTERNS)*/ && main.utils!!.isOnSkyblock()
                && slot.inventory.displayName.unformattedText == CraftingPattern.CRAFTING_TABLE_DISPLAYNAME
                && main.persistentValuesManager!!.getPersistentValues().getSelectedCraftingPattern() !== CraftingPattern.FREE
            ) {
                val craftingGridIndex = CraftingPattern.slotToCraftingGridIndex(slot.slotIndex)
                if (craftingGridIndex >= 0) {
                    val slotLeft = slot.xDisplayPosition
                    val slotTop = slot.yDisplayPosition
                    val slotRight = slotLeft + 16
                    val slotBottom = slotTop + 16
                    if (main.persistentValuesManager!!.getPersistentValues().getSelectedCraftingPattern()
                            .isSlotInPattern(craftingGridIndex)
                    ) {
                        if (!slot.hasStack) {
                            guiContainer.drawGradientRect(
                                slotLeft,
                                slotTop,
                                slotRight,
                                slotBottom,
                                OVERLAY_GREEN,
                                OVERLAY_GREEN
                            )
                        }
                    } else {
                        if (slot.hasStack) {
                            guiContainer.drawGradientRect(
                                slotLeft,
                                slotTop,
                                slotRight,
                                slotBottom,
                                OVERLAY_RED,
                                OVERLAY_RED
                            )
                        }
                    }
                }
            }

            if (main.configValues!!.isEnabled(Feature.LOCK_SLOTS) &&
                main.utils!!.isOnSkyblock()
            ) {
                val slotNum: Int = slot.slotNumber + main.inventoryUtils!!.getSlotDifference(container)
                if (main.configValues!!.getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container is ContainerPlayer && slotNum >= 5)
                ) {
                    GlStateManager.disableLighting()
                    GlStateManager.disableDepth()
                    GlStateManager.color(1f, 1f, 1f, 0.4f)
                    GlStateManager.enableBlend()
                    mc.textureManager.bindTexture(LOCK)
                    mc.ingameGUI.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16)
                    GlStateManager.enableLighting()
                    GlStateManager.enableDepth()
                }
            }
        }
    }

    fun keyTyped(guiContainer: GuiContainer?, keyCode: Int, theSlot: Slot?, returnValue: ReturnValue<*>) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val mc = Minecraft.getMinecraft()
        if (main.utils!!.isOnSkyblock()) {
            if (main.configValues!!
                    .isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.keyCode)
            ) {
                var slot: Int = main.utils!!.getLastHoveredSlot()
                var isHotkeying = false
                if (mc.thePlayer.inventory.itemStack == null && theSlot != null) {
                    for (i in 0..8) {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].keyCode) {
                            slot = i + 36 // They are hotkeying, the actual slot is the targeted one, +36 because
                            isHotkeying = true
                        }
                    }
                }
                if (slot >= 9 || mc.thePlayer.openContainer is ContainerPlayer && slot >= 5) {
                    //TODO:
//                    if (main.configValues!!.getLockedSlots().contains(slot)) {
//                        if (main.getLockSlotKey().getKeyCode() === keyCode) {
//                            main.utils!!.playLoudSound("random.orb", 1)
//                            main.configValues!!.getLockedSlots().remove(slot)
//                            main.configValues!!.saveConfig()
//                        } else if (isHotkeying || mc.gameSettings.keyBindDrop.keyCode == keyCode) {
//                            // Only buttons that would cause an item to move/drop out of the slot will be canceled
//                            returnValue.cancel() // slot is locked
//                            main.utils!!.playLoudSound("note.bass", 0.5)
//                            return
//                        }
//                    } else {
//                        if (main.getLockSlotKey().getKeyCode() === keyCode) {
//                            main.utils!!.playLoudSound("random.orb", 0.1)
//                            main.configValues!!.getLockedSlots().add(slot)
//                            main.configValues!!.saveConfig()
//                        }
//                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.keyCode == keyCode && main.configValues!!
                    .isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.utils!!.isInDungeon()
            ) {
                if (!main.utils!!.getItemDropChecker().canDropItem(theSlot)) returnValue.cancel()
            }
        }
    }

    /**
     * This method returns true to CANCEL the click in a GUI (lol I get confused)
     */
    fun onHandleMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int): Boolean {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        return slot != null && slot.hasStack && main.configValues!!.isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) &&
                main.utils!!.isEmptyGlassPane(slot.stack) && main.utils!!.isOnSkyblock() && !main.utils!!
            .isInDungeon() &&
                (main.inventoryUtils!!.getInventoryType() !== InventoryType.ULTRASEQUENCER || main.utils!!
                    .isGlassPaneColor(slot.stack, EnumDyeColor.BLACK))
    }
}
