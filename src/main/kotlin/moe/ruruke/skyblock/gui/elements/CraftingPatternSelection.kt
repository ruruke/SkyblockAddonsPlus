package moe.ruruke.skyblock.gui.elements

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.PersistentValuesManager
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPattern
import moe.ruruke.skyblock.gui.CheckBox
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager

/**
 * GUI Element that lets the user select a [CraftingPattern]
 *
 * @author DidiSkywalker
 */
class CraftingPatternSelection(mc: Minecraft, private val x: Int, private val y: Int) {
    private val mc: Minecraft = mc
    private val blockIncompleteCheckBox: CheckBox

    init {
        val checkBoxY = (y - MARGIN - 8)
        val checkBoxText: String = Translations.getMessage("messages.blockIncompletePatterns")!!

        val persistentValuesManager: PersistentValuesManager = SkyblockAddonsPlus.persistentValuesManager!!
        blockIncompleteCheckBox = CheckBox(
            mc,
            x,
            checkBoxY,
            8,
            checkBoxText,
            persistentValuesManager.getPersistentValues().isBlockCraftingIncompletePatterns()
        )
        blockIncompleteCheckBox.setOnToggleListener { value: Boolean ->
            persistentValuesManager.getPersistentValues().setBlockCraftingIncompletePatterns(value)
            persistentValuesManager.saveValues()
        }
    }

    fun draw() {
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        Minecraft.getMinecraft().getTextureManager().bindTexture(CraftingPattern.ICONS)
        GlStateManager.color(1f, 1f, 1f, 1f)
        for (craftingPattern in CraftingPattern.values()) {
            val offset = getYOffsetByIndex(craftingPattern.index)
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.ingameGUI.drawTexturedModalRect(x, y + offset, 0, offset, ICON_SIZE, ICON_SIZE)
            if (craftingPattern !== SkyblockAddonsPlus.persistentValuesManager!!.getPersistentValues().getSelectedCraftingPattern()) {
                GlStateManager.color(1f, 1f, 1f, .5f)
                mc.ingameGUI.drawTexturedModalRect(x, y + offset, 33, 0, ICON_SIZE, ICON_SIZE)
            }
        }
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()

        blockIncompleteCheckBox.draw()
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        blockIncompleteCheckBox.onMouseClick(mouseX, mouseY, mouseButton)
        if (mouseButton != 0 || mouseX < this.x || mouseX > this.x + ICON_SIZE || mouseY < this.y || mouseY > this.y + CraftingPattern.values().size * (ICON_SIZE + MARGIN)) {
            return  // cannot hit
        }

        val persistentValuesManager: PersistentValuesManager = SkyblockAddonsPlus.persistentValuesManager!!

        for (craftingPattern in CraftingPattern.values()) {
            val offset = getYOffsetByIndex(craftingPattern.index)
            if (mouseY > this.y + offset && mouseY < this.y + offset + ICON_SIZE) {
                if (persistentValuesManager.getPersistentValues().getSelectedCraftingPattern() !== craftingPattern) {
                    SkyblockAddonsPlus.utils!!.playLoudSound("gui.button.press", 1.0)
                    persistentValuesManager.getPersistentValues().setSelectedCraftingPattern(craftingPattern)
                    persistentValuesManager.saveValues()
                }
            }
        }

        Utils.blockNextClick = true
    }

    private fun getYOffsetByIndex(index: Int): Int {
        return index * (ICON_SIZE + MARGIN)
    }

    companion object {
        /**
         * Icon size in pixel
         */
        const val ICON_SIZE: Int = 32

        /**
         * Margin value to use between the icons
         */
        private const val MARGIN = 2
    }
}
