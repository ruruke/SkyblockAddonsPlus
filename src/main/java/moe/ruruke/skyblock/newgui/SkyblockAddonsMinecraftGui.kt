package moe.ruruke.skyblock.newgui

import net.minecraft.client.gui.GuiScreen

class SkyblockAddonsMinecraftGui(private val gui: GuiBase) : GuiScreen() {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        gui.render()
    }

    override fun onGuiClosed() {
        gui.close()
    }
}
