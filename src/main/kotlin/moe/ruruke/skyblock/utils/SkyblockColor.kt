package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.utils.ColorUtils.getAlphaIntFromFloat
import moe.ruruke.skyblock.utils.ColorUtils.getColor
import moe.ruruke.skyblock.utils.ColorUtils.setColorAlpha
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import java.util.*

class SkyblockColor {
    var colorAnimation: ColorAnimation = ColorAnimation.NONE

    private val colors = LinkedList<Int>()

    @JvmOverloads
    constructor(color: Int = DEFAULT_COLOR) {
        colors.add(color)
    }

    constructor(color: Int, alpha: Float) {
        colors.add(setColorAlpha(color, alpha))
    }

    constructor(r: Int, g: Int, b: Int, a: Int) {
        colors.add(getColor(r, g, b, a))
    }

    constructor(r: Int, g: Int, b: Int, a: Float) {
        colors.add(getColor(r, g, b, getAlphaIntFromFloat(a)))
    }

    fun setColorAnimation(_colorAnimation: ColorAnimation?): SkyblockColor {
        colorAnimation = _colorAnimation!!
        return this
    }

    fun getColorAtPosition(x: Float, y: Float): Int {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            //TODO:
//            return ManualChromaManager.getChromaColor(x, y, ColorUtils.getAlpha(getColor()));
        }

        return colors[0]
    }

    fun getTintAtPosition(x: Float, y: Float): Int {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            //TODO:
//            return ManualChromaManager.getChromaColor(x, y, Color.RGBtoHSB(ColorUtils.getRed(getColor()), ColorUtils.getGreen(getColor()), ColorUtils.getBlue(getColor()), null) , ColorUtils.getAlpha(getColor()));
        }

        return colors[0]
    }

    fun getColorAtPosition(x: Double, y: Double, z: Double): Int {
        return getColorAtPosition(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun getColorAtPosition(x: Float, y: Float, z: Float): Int {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            //TODO:
//            return ManualChromaManager.getChromaColor(x, y, z, ColorUtils.getAlpha(getColor()));
        }

        return colors[0]
    }

    fun setColor(color: Int): SkyblockColor {
        return setColor(0, color)
    }

    fun setColor(index: Int, color: Int): SkyblockColor {
        if (index >= colors.size) {
            colors.add(color)
        } else {
            colors[index] = color
        }
        return this
    }

    val isMulticolor: Boolean
        get() = colorAnimation != ColorAnimation.NONE

    val isPositionalMulticolor: Boolean
        get() = colorAnimation != ColorAnimation.NONE && false
    //        return colorAnimation != ColorAnimation.NONE && SkyblockAddons.getInstance().configValues!!.getChromaMode() != EnumUtils.ChromaMode.ALL_SAME_COLOR;

    val color: Int
        get() = getColorSafe(0)

    private fun getColorSafe(index: Int): Int {
        while (index >= colors.size) {
            colors.add(DEFAULT_COLOR)
        }
        return colors[index]
    }

    fun drawMulticolorManually(): Boolean {
        return colorAnimation == ColorAnimation.CHROMA && !shouldUseChromaShaders()
    }

    fun drawMulticolorUsingShader(): Boolean {
        return colorAnimation == ColorAnimation.CHROMA && shouldUseChromaShaders()
    }

    enum class ColorAnimation {
        NONE,
        CHROMA
    }

    companion object {
        private val tessellator: Tessellator = Tessellator.getInstance()
        private val worldRenderer: WorldRenderer = tessellator.worldRenderer


        private const val DEFAULT_COLOR = -0x1

        fun shouldUseChromaShaders(): Boolean {
            //TODO:
//        ConfigValues config = SkyblockAddons.getInstance().configValues!!;
//        return config.getChromaMode() != EnumUtils.ChromaMode.ALL_SAME_COLOR && ShaderManager.getInstance().areShadersSupported() && config.isEnabled(Feature.USE_NEW_CHROMA_EFFECT);
            return false
        }
    }
}
