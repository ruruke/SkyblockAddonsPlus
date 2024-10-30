package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.gui.SkyblockAddonsGui
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ButtonOpenColorMenu(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    buttonText: String?,
    main: SkyblockAddonsPlus.Companion,
    feature: Feature?
) :
    ButtonText(0, x.toInt(), y.toInt(), buttonText, feature) {
    private val main: SkyblockAddonsPlus.Companion = main

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    init {
        this.width = width
        this.height = height
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        hovered =
            mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
        val boxColor: Int
        var fontColor = Color(224, 224, 224, 255).rgb
        var boxAlpha = 100
        if (hovered) {
            boxAlpha = 170
            fontColor = Color(255, 255, 160, 255).rgb
        }
        boxColor = SkyblockAddonsPlus.configValues!!.getColor(feature!!, boxAlpha)
        // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
        GlStateManager.enableBlend()
        var scale = 1f
        val stringWidth: Int = mc.fontRendererObj.getStringWidth(displayString)
        val widthLimit: Float = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10f
        if (stringWidth > widthLimit) {
            scale = 1 / (stringWidth / widthLimit)
        }
        drawButtonBoxAndText(boxColor, scale, fontColor)
    }
}
