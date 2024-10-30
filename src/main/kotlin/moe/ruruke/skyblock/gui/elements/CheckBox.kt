package moe.ruruke.skyblock.gui.elements

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPattern
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager

/**
 * CheckBox GUI element to use in other GUI elements.
 *
 * @author DidiSkywalker
 */
class CheckBox internal constructor(
    private val mc: Minecraft,
    private val x: Int,
    private val y: Int,
    private val size: Int,
    private val text: String?
) {
    fun interface OnToggleListener {
        fun onToggle(value: Boolean)
    }

    private val scale: Float

    private val textWidth: Int

    private var value: Boolean = false
    private var onToggleListener: OnToggleListener? = null

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     * @param value Default value
     */
    constructor(mc: Minecraft, x: Int, y: Int, size: Int, text: String?, value: Boolean) : this(mc, x, y, size, text) {
        this.value = value
    }

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     */
    init {
        this.scale = size.toFloat() / ICON_SIZE.toFloat()
        this.textWidth = mc.fontRendererObj.getStringWidth(text)
    }

    fun draw() {
        val scaledX = Math.round(x / scale)
        val scaledY = Math.round(y / scale)

        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)

        val color = if (value) ColorCode.WHITE.getColor() else ColorCode.GRAY.getColor()
        DrawUtils.drawText(
            text,
            (scaledX + Math.round(size * 1.5f / scale)).toFloat(), (scaledY + (size / 2)).toFloat(), color
        )

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        Minecraft.getMinecraft().textureManager.bindTexture(CraftingPattern.ICONS)
        GlStateManager.color(1f, 1f, 1f, 1f)

        if (value) {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 49, 34, 16, 16)
        } else {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 33, 34, 16, 16)
        }

        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && mouseX > this.x && mouseX < this.x + this.size + this.textWidth && mouseY > this.y && mouseY < this.y + this.size) {
            value = !value
            SkyblockAddonsPlus.utils!!.playSound("gui.button.press", 0.25, 1.0)
            if (onToggleListener != null) {
                onToggleListener!!.onToggle(value)
            }

            Utils.blockNextClick = true
        }
    }

    /**
     * Attaches a listener that gets notified whenever the CheckBox is toggled
     *
     * @param listener Listener to attach
     */
    fun setOnToggleListener(listener: OnToggleListener?) {
        onToggleListener = listener
    }

    companion object {
        /**
         * Size of the CheckBox icon
         */
        private const val ICON_SIZE = 16
    }
}
