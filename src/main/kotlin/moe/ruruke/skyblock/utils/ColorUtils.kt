package moe.ruruke.skyblock.utils

import net.minecraft.client.renderer.GlStateManager

object ColorUtils {
    //    private static final SkyblockColor SKYBLOCK_COLOR = new SkyblockColor();
    /**
     * Binds a color given its rgb integer representation.
     */
    fun bindWhite() {
        bindColor(1f, 1f, 1f, 1f)
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values.
     */
    fun bindColor(r: Float, g: Float, b: Float, a: Float) {
        GlStateManager.color(r, g, b, a)
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values.
     */
    fun bindColor(r: Int, g: Int, b: Int, a: Int) {
        bindColor(r / 255f, g / 255f, b / 255f, a / 255f)
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values, multiplying
     * all color values by the specified multiplier (for example to make the color darker).
     */
    private fun bindColor(r: Int, g: Int, b: Int, a: Int, colorMultiplier: Float) {
        bindColor(r / 255f * colorMultiplier, g / 255f * colorMultiplier, b / 255f * colorMultiplier, a / 255f)
    }

    /**
     * Binds a color given its rgb integer representation.
     */
    fun bindColor(color: Int) {
        bindColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color))
    }

    /**
     * Binds a color, multiplying all color values by the specified
     * multiplier (for example to make the color darker).
     */
    fun bindColor(color: Int, colorMultiplier: Float) {
        bindColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color), colorMultiplier)
    }

    /**
     * Takes the color input integer and sets its alpha color value,
     * returning the resulting color.
     */
    fun setColorAlpha(color: Int, alpha: Float): Int {
        return setColorAlpha(color, getAlphaIntFromFloat(alpha))
    }

    /**
     * Takes the color input integer and sets its alpha color value,
     * returning the resulting color.
     */
    fun setColorAlpha(color: Int, alpha: Int): Int {
        return (alpha shl 24) or (color and 0x00FFFFFF)
    }

    fun getRed(color: Int): Int {
        return color shr 16 and 0xFF
    }

    fun getGreen(color: Int): Int {
        return color shr 8 and 0xFF
    }

    fun getBlue(color: Int): Int {
        return color and 0xFF
    }

    fun getAlpha(color: Int): Int {
        return color shr 24 and 0xFF
    }

    fun getAlphaFloat(color: Int): Float {
        return getAlpha(color) / 255f
    }

    fun getAlphaIntFromFloat(alpha: Float): Int {
        return (alpha * 255).toInt()
    }

    fun getColor(r: Int, g: Int, b: Int, a: Int): Int {
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }


    fun getDummySkyblockColor(color: Int): SkyblockColor {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, color)
    }

    fun getDummySkyblockColor(r: Int, g: Int, b: Int, a: Int): SkyblockColor {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, getColor(r, g, b, a))
    }

    fun getDummySkyblockColor(r: Int, g: Int, b: Int, a: Float): SkyblockColor {
        return getDummySkyblockColor(r, g, b, getAlphaIntFromFloat(a))
    }

    fun getDummySkyblockColor(colorAnimation: SkyblockColor.ColorAnimation?): SkyblockColor {
        return getDummySkyblockColor(colorAnimation, -1)
    }

    fun getDummySkyblockColor(color: Int, chroma: Boolean): SkyblockColor {
        return getDummySkyblockColor(
            if (chroma) SkyblockColor.ColorAnimation.CHROMA else SkyblockColor.ColorAnimation.NONE,
            color
        )
    }

    private val SKYBLOCK_COLOR: SkyblockColor = SkyblockColor()
    fun getDummySkyblockColor(colorAnimation: SkyblockColor.ColorAnimation?, color: Int): SkyblockColor {
        return SKYBLOCK_COLOR.setColorAnimation(colorAnimation).setColor(color)
    }
}
