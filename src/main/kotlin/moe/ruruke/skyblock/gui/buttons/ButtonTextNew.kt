package moe.ruruke.skyblock.gui.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton

class ButtonTextNew(x: Int, y: Int, text: String?, private val centered: Boolean, private val color: Int) :
    GuiButton(0, x, y, text) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        var x = xPosition
        val y = yPosition

        if (centered) {
            x -= mc.fontRendererObj.getStringWidth(displayString) / 2
        }

        mc.fontRendererObj.drawString(displayString, x, y, color)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return false
    }
}
