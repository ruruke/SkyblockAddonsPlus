package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.gui.buttons.ButtonText
import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ButtonModify(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    buttonText: String?,
    main: SkyblockAddonsPlus.Companion,
    feature: Feature
) :
    ButtonText(0, x.toInt(), y.toInt(), buttonText, feature) {
    private val main: SkyblockAddonsPlus.Companion = main

    override var feature: Feature? = feature

    /**
     * Create a button for adding or subtracting a number.
     */
    init {
        this.width = width
        this.height = height
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        hovered =
            mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
        val boxColor: Int
        var boxAlpha = 100
        if (hovered && !hitMaximum()) {
            boxAlpha = 170
        }
        boxColor = if (hitMaximum()) {
            ColorCode.GRAY.getColor(boxAlpha)
        } else {
            if (feature === Feature.ADD) {
                ColorCode.GREEN.getColor(boxAlpha)
            } else {
                ColorCode.RED.getColor(boxAlpha)
            }
        }
        GlStateManager.enableBlend()
        var fontColor: Int = ColorCode.WHITE.getColor()
        if (hovered && !hitMaximum()) {
            fontColor = Color(255, 255, 160, 255).rgb
        }
        drawButtonBoxAndText(boxColor, 1f, fontColor)
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        if (!hitMaximum()) {
            super.playPressSound(soundHandlerIn)
        }
    }

    private fun hitMaximum(): Boolean {
        return (feature === Feature.SUBTRACT && SkyblockAddonsPlus.configValues!!.getWarningSeconds() === 1) ||
                (feature === Feature.ADD && SkyblockAddonsPlus.configValues!!.getWarningSeconds() === 99)
    }
}