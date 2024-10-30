package moe.ruruke.skyblock.newgui.sizes.parent

import moe.ruruke.skyblock.newgui.GuiElement
import moe.ruruke.skyblock.newgui.sizes.SizeBase

class ParentPercentageSize(
    guiElement: GuiElement<*>, private val xPercentage: Float,
    private val yPercentage: Float
) : SizeBase() {
    private val guiElement: GuiElement<*> = guiElement

    constructor(guiElement: GuiElement<*>, percentage: Float) : this(guiElement, percentage, percentage)

    override fun updateSizes() {
        h = guiElement.getParent().getH() * xPercentage
        w = guiElement.getParent().getW() * yPercentage
    }

    override fun updatePositions() {
        y = guiElement.getParent().getY() * xPercentage
        x = guiElement.getParent().getX() * yPercentage
    }
}
