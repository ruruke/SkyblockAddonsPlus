package moe.ruruke.skyblock.core

import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.EnumUtils


class GuiFeatureData {
    private var defaultColor: ColorCode? = null
    private var drawType: EnumUtils.DrawType? = null

    fun getDefaultColor(): ColorCode {
        return defaultColor!!
    }

    fun getDrawType(): EnumUtils.DrawType {
        return drawType!!
    }
    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br></br>
     *
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    private var colorsRestricted: Boolean

    constructor(defaultColor: ColorCode?) : this(defaultColor, false)

    constructor(defaultColor: ColorCode?, colorsRestricted: Boolean) {
        this.defaultColor = defaultColor
        this.colorsRestricted = colorsRestricted
    }

    constructor(drawType: EnumUtils.DrawType?) : this(drawType!!, false)

    constructor(drawType: EnumUtils.DrawType?, defaultColor: ColorCode?) : this(drawType, defaultColor, false)

    private constructor(drawType: EnumUtils.DrawType, colorsRestricted: Boolean) {
        this.drawType = drawType
        this.colorsRestricted = colorsRestricted
    }

    constructor(drawType: EnumUtils.DrawType?, defaultColor: ColorCode?, colorsRestricted: Boolean) {
        this.drawType = drawType
        this.defaultColor = defaultColor
        this.colorsRestricted = colorsRestricted
    }
}
