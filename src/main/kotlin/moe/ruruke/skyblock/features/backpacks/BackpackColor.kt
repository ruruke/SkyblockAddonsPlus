package moe.ruruke.skyblock.features.backpacks

import java.awt.Color
import java.util.*

enum class BackpackColor(private val r: Int, private val g: Int, private val b: Int) {
    BLACK(29, 29, 33),
    RED(176, 46, 38),
    GREEN(94, 124, 22),
    BROWN(131, 84, 50),
    BLUE(60, 68, 170),
    PURPLE(137, 50, 184),
    CYAN(22, 156, 156),
    LIGHT_GREY(157, 157, 151),
    GREY(71, 79, 82),
    PINK(243, 139, 170),
    LIME(128, 199, 31),
    YELLOW(254, 216, 61),
    LIGHT_BLUE(58, 179, 218),
    MAGENTA(199, 78, 189),
    ORANGE(249, 128, 29),
    WHITE(255, 255, 255);

    fun getR(): Float {
        return r.toFloat() / 255
    }

    fun getG(): Float {
        return g.toFloat() / 255
    }

    fun getB(): Float {
        return b.toFloat() / 255
    }

    val inventoryTextColor: Int
        get() {
            var rgb = 4210752 // Default inventory grey.

            if (darkColors.contains(this)) { // Dark colors need a contrasting lighter white color.
                rgb = Color(219, 219, 219, 255).rgb
            }
            return rgb
        }

    companion object {
        private val darkColors: Set<BackpackColor> = EnumSet.of(
            BLACK, PURPLE, GREEN,
            MAGENTA, RED, BROWN, BLUE, GREY
        )
    }
}
