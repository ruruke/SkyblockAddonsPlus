package moe.ruruke.skyblock.utils

import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.util.*

/**
 * @author Brian Graham (CraftedFury)
 */
enum class ColorCode(
    val code: Char,
    val isFormat: Boolean,
    private val jsonName: String?,
    rgb: Int = -1
) {
    BLACK('0', 0x000000),
    DARK_BLUE('1', 0x0000AA),
    DARK_GREEN('2', 0x00AA00),
    DARK_AQUA('3', 0x00AAAA),
    DARK_RED('4', 0xAA0000),
    DARK_PURPLE('5', 0xAA00AA),
    GOLD('6', 0xFFAA00),
    GRAY('7', 0xAAAAAA),
    DARK_GRAY('8', 0x555555),
    BLUE('9', 0x5555FF),
    GREEN('a', 0x55FF55),
    AQUA('b', 0x55FFFF),
    RED('c', 0xFF5555),
    LIGHT_PURPLE('d', 0xFF55FF),
    YELLOW('e', 0xFFFF55),
    WHITE('f', 0xFFFFFF),
    MAGIC('k', true, "obfuscated"),
    BOLD('l', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true, "underlined"),
    ITALIC('o', true),
    RESET('r'),
    CHROMA('z', 0xFFFFFE);

    private val toString: String
    private val color: Int

    @JvmOverloads
    constructor(code: Char, rgb: Int = -1) : this(code, false, rgb)

    @JvmOverloads
    constructor(code: Char, isFormat: Boolean, rgb: Int = -1) : this(code, isFormat, null, rgb)

    init {
        this.toString = String(charArrayOf('\u00a7', code))
        this.color = (255 shl 24) or (rgb and 0x00FFFFFF)
    }

    val colorObject: Color
        get() = Color(color)
    fun getColor(alpha: Int): Int {
        return ColorUtils.setColorAlpha(color, alpha)
    }

    @JvmName("getColor")
    fun getColor(): Int {
        return color
    }

    fun getJsonName(): String {
        return if (StringUtils.isEmpty(this.jsonName)) name.lowercase(Locale.getDefault()) else jsonName!!
    }

    fun isColor(): Boolean {
        return !this.isFormat && this != RESET
    }

    val nextFormat: ColorCode
        get() = this.getNextFormat(ordinal)

    private fun getNextFormat(ordinal: Int): ColorCode {
        val values = entries.toTypedArray()
        val nextColor = ordinal + 1

        if (nextColor > values.size - 1) {
            return values[0]
        } else if (!values[nextColor].isColor()) {
            return getNextFormat(nextColor)
        }

        return values[nextColor]
    }

    override fun toString(): String {
        return this.toString
    }

    companion object {
        const val COLOR_CHAR: Char = '\u00a7'

        /**
         * Get the color represented by the specified code.
         *
         * @param code The code to search for.
         * @return The mapped color, or null if non exists.
         */
        fun getByChar(code: Char): ColorCode? {
            for (color in entries) {
                if (color.code == code) return color
            }

            return null
        }
    }
}