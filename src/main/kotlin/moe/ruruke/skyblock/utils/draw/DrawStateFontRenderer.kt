package moe.ruruke.skyblock.utils.draw

import moe.ruruke.skyblock.core.chroma.MulticolorShaderManager.Companion.instance
import moe.ruruke.skyblock.utils.ColorUtils.getAlpha
import moe.ruruke.skyblock.utils.ColorUtils.getBlue
import moe.ruruke.skyblock.utils.ColorUtils.getGreen
import moe.ruruke.skyblock.utils.ColorUtils.getRed
import moe.ruruke.skyblock.utils.SkyblockColor
import net.minecraft.client.renderer.GlStateManager

class DrawStateFontRenderer(theColor: SkyblockColor) : DrawState2D(theColor, true, false) {
    protected var multicolorFeatureOverride: Boolean = false
    protected var isActive: Boolean = false
    protected var featureScale: Float = 1f

    fun setupMulticolorFeature(theFeatureScale: Float) {
        if (color.drawMulticolorManually()) {
            featureScale = theFeatureScale
        }
        multicolorFeatureOverride = true
    }

    fun endMulticolorFeature() {
        if (color.drawMulticolorManually()) {
            featureScale = 1f
        }
        multicolorFeatureOverride = false
    }

    fun loadFeatureColorEnv() {
        if (multicolorFeatureOverride) {
            newColorEnv()
        }
    }

    fun restoreColorEnv() {
        if (color.drawMulticolorUsingShader()) {
            if (multicolorFeatureOverride) {
                // TODO: change order of restore to bind white here after font renderer binds the other color
            } else {
                instance.end()
            }
        }
        isActive = false
    }

    override fun newColorEnv(): DrawStateFontRenderer {
        super.newColorEnv()
        isActive = true
        return this
    }

    override fun endColorEnv(): DrawStateFontRenderer {
        super.endColorEnv()
        isActive = false
        return this
    }

    override fun bindAnimatedColor(x: Float, y: Float): DrawStateFontRenderer {
        // Handle feature scale here
        val colorInt = color.getTintAtPosition(x * featureScale, y * featureScale)
        GlStateManager.color(
            getRed(colorInt) / 255f,
            getGreen(colorInt) / 255f,
            getBlue(colorInt) / 255f,
            getAlpha(colorInt) / 255f
        )
        return this
    }

    fun shouldManuallyRecolorFont(): Boolean {
        return (multicolorFeatureOverride || isActive) && color.drawMulticolorManually()
    }
}
