package moe.ruruke.skyblock.newgui.sizes.parent

import moe.ruruke.skyblock.newgui.GuiElement
import moe.ruruke.skyblock.newgui.sizes.SizeBase

class ParentOffsetSize(guiElement: GuiElement<*>, x: Float, y: Float) : SizeBase() {
    private val guiElement: GuiElement<*> = guiElement
    private var _x: Float
    private var _y: Float

    init {
        this._x = x
        this._y = y
    }

    constructor(guiElement: GuiElement<*>, offset: Float) : this(guiElement, offset, offset)

    override fun updateSizes() {
        h = guiElement.getParent().getH() + x
        w = guiElement.getParent().getW() + y
    }

    override fun updatePositions() {
        y = guiElement.getParent().getY() + x
        x = guiElement.getParent().getX() + y
    }
}
