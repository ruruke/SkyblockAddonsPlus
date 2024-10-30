package moe.ruruke.skyblock.gui.buttons


import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton

class ButtonNewTag(x: Int, y: Int) : GuiButton(0, x, y, "NEW") {
    init {
        width = 25
        height = 11
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, ColorCode.RED.getColor())
        mc.fontRendererObj.drawString(displayString, xPosition + 4, yPosition + 2, ColorCode.WHITE.getColor())
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
    }
}
