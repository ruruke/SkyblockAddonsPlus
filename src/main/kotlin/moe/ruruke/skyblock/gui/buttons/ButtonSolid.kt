package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.gui.SkyblockAddonsGui
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ButtonSolid(
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

    override var feature: Feature?

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button that has a solid color and text.
     */
    init {
        this.feature = feature
        this.width = width
        this.height = height
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (feature === Feature.TEXT_STYLE) {
            displayString = SkyblockAddonsPlus.configValues!!.getTextStyle().getMessage()
        } else if (feature === Feature.CHROMA_MODE) {
            //TODO:
//            displayString = SkyblockAddonsPlus.configValues!!.getChromaMode().getMessage()
        } else if (feature === Feature.WARNING_TIME) {
            displayString =  "${SkyblockAddonsPlus.configValues!!.getWarningSeconds()}s"
        } else if (feature === Feature.TURN_ALL_FEATURES_CHROMA) {
            var enable = false
            for (loopFeature in Feature.values()) {
                if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData()!!.getDefaultColor() != null
                ) {
                    if (!SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(loopFeature)) {
                        enable = true
                        break
                    }
                }
            }
            displayString =
                if (enable) Translations.getMessage("messages.enableAll") else Translations.getMessage("messages.disableAll")
        }
        var alpha: Int
        var alphaMultiplier = 1f
        if (SkyblockAddonsPlus.utils!!.isFadingIn()) {
            val timeSinceOpen = System.currentTimeMillis() - timeOpened
            val fadeMilis = 500
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = timeSinceOpen.toFloat() / fadeMilis
            }
            alpha = (255 * alphaMultiplier).toInt()
        } else {
            alpha = 255
        }
        hovered =
            mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
        var boxAlpha = 100
        if (hovered && feature !== Feature.WARNING_TIME) boxAlpha = 170
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        boxAlpha = (boxAlpha * alphaMultiplier).toInt()
        var boxColor: Int = SkyblockAddonsPlus.utils!!.getDefaultColor(boxAlpha.toFloat())
        if (this.feature === Feature.RESET_LOCATION) {
            boxColor = ColorUtils.setColorAlpha(0xFF7878, boxAlpha)
        }
        GlStateManager.enableBlend()
        if (alpha < 4) alpha = 4
        var fontColor = Color(224, 224, 224, alpha).rgb
        if (hovered && feature !== Feature.WARNING_TIME) {
            fontColor = Color(255, 255, 160, alpha).rgb
        }
        var scale = 1f
        val stringWidth: Int = mc.fontRendererObj.getStringWidth(displayString)
        var widthLimit: Float = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10f
        if (feature === Feature.WARNING_TIME) {
            widthLimit = 90f
        }
        if (stringWidth > widthLimit) {
            scale = 1 / (stringWidth / widthLimit)
        }
        drawButtonBoxAndText(boxColor, scale, fontColor)
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        if (feature !== Feature.WARNING_TIME) super.playPressSound(soundHandlerIn)
    }
}
