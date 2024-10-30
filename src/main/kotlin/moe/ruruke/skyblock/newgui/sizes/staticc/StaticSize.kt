package moe.ruruke.skyblock.newgui.sizes.staticc

import moe.ruruke.skyblock.newgui.sizes.SizeBase


class StaticSize : SizeBase() {
    fun xy(x: Float, y: Float): StaticSize {
        this.x = x
        this.y = y
        return this
    }

    fun wh(w: Float, h: Float): StaticSize {
        this.w = w
        this.h = h
        return this
    }
}
