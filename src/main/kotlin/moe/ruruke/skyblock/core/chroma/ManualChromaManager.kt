package moe.ruruke.skyblock.core.chroma

import lombok.Getter
import lombok.Setter
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * This class is used to manual
 */
object ManualChromaManager {
    @Getter
    @Setter
    private var coloringTextChroma = false

    @Getter
    private var featureScale = 0f

    private val defaultColorHSB = floatArrayOf(0f, 0.75f, 0.9f)

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br></br><br></br>
     *
     * After calling this & doing the drawString, make sure to call [ManualChromaManager.doneRenderingText].
     *
     * @param feature The feature to check if fade chroma is enabled.
     */
    fun renderingText(feature: Feature) {
        if (SkyblockAddonsPlus.configValues!!.getChromaMode() === EnumUtils.ChromaMode.FADE &&
            SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(feature)
        ) {
            coloringTextChroma = true
            featureScale = SkyblockAddonsPlus.configValues!!.getGuiScale(feature)
        }
    }

    // TODO Don't force alpha in the future...
    fun getChromaColor(x: Float, y: Float, alpha: Int): Int {
        return getChromaColor(x, y, defaultColorHSB, alpha)
    }

    fun getChromaColor(x: Float, y: Float, currentHSB: FloatArray, alpha: Int): Int {
        var x = x
        var y = y
        if (SkyblockAddonsPlus.configValues!!.getChromaMode() === EnumUtils.ChromaMode.ALL_SAME_COLOR) {
            x = 0f
            y = 0f
        }
        if (coloringTextChroma) {
            x *= featureScale
            y *= featureScale
        }
        val scale: Int = ScaledResolution(Minecraft.getMinecraft()).getScaleFactor()
        x *= scale.toFloat()
        y *= scale.toFloat()

        val chromaSize: Float = SkyblockAddonsPlus.configValues!!.getChromaSize().toFloat() * (Minecraft.getMinecraft().displayWidth / 100f)
        val chromaSpeed: Float = SkyblockAddonsPlus.configValues!!.getChromaSpeed().toFloat() / 360f

        val ticks: Float =
            SkyblockAddonsPlus.instance.newScheduler!!.totalTicks as Float + Utils.getPartialTicks()
        val timeOffset = ticks * chromaSpeed

        val newHue = ((x + y) / chromaSize - timeOffset) % 1

        //if (currentHSB[2] < 0.3) { // Keep shadows as shadows
        //    return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]), alpha);
        //} else {
        val saturation: Float = SkyblockAddonsPlus.configValues!!.getChromaSaturation().toFloat()
        val brightness: Float =
            SkyblockAddonsPlus.configValues!!.getChromaBrightness().toFloat() * currentHSB[2]
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha)
        //}
    }

    fun getChromaColor(x: Float, y: Float, z: Float, alpha: Int): Int {
        var x = x
        var y = y
        var z = z
        if (SkyblockAddonsPlus.configValues!!.getChromaMode() === EnumUtils.ChromaMode.ALL_SAME_COLOR) {
            x = 0f
            y = 0f
            z = 0f
        }
        val chromaSize: Float = SkyblockAddonsPlus.configValues!!.getChromaSize()
            .toFloat() * (Minecraft.getMinecraft().displayWidth / 100f)
        val chromaSpeed: Float = SkyblockAddonsPlus.configValues!!.getChromaSpeed().toFloat() / 360f

        val ticks: Float =
            SkyblockAddonsPlus.instance.newScheduler!!.totalTicks.toFloat() + Utils.getPartialTicks()
        val timeOffset = ticks * chromaSpeed

        val newHue = ((x - y + z) / (chromaSize / 20f) - timeOffset) % 1

        val saturation: Float = SkyblockAddonsPlus.configValues!!.getChromaSaturation().toFloat()
        val brightness: Float = SkyblockAddonsPlus.configValues!!.getChromaBrightness().toFloat()
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha)
    }

    /**
     * Disables any chroma stuff.
     */
    fun doneRenderingText() {
        coloringTextChroma = false
        featureScale = 1f
    }
}
