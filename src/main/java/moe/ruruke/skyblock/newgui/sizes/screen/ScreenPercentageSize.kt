package moe.ruruke.skyblock.newgui.sizes.screen

import moe.ruruke.skyblock.newgui.sizes.SizeBase
import net.minecraft.client.Minecraft

class ScreenPercentageSize(private val xPercentage: Float, private val yPercentage: Float) : SizeBase() {
    constructor(percentage: Float) : this(percentage, percentage)

    override fun updatePositions() {
        y = Minecraft.getMinecraft().displayHeight * xPercentage
        x = Minecraft.getMinecraft().displayWidth * yPercentage
    }

    override fun updateSizes() {
        h = Minecraft.getMinecraft().displayHeight * xPercentage
        w = Minecraft.getMinecraft().displayWidth * yPercentage
    }
}
