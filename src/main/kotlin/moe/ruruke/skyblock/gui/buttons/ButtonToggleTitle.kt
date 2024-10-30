package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import net.minecraft.client.Minecraft

class ButtonToggleTitle(
    x: Double,
    y: Double,
    buttonText: String,
    main: SkyblockAddonsPlus.Companion,
    feature: Feature?
) :
    ButtonToggle(x, y, main, feature) {
    private val main: SkyblockAddonsPlus.Companion

    init {
        displayString = buttonText
        this.main = main
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        super.drawButton(mc, mouseX, mouseY)
        val fontColor: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue(255)
        drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2, yPosition - 10, fontColor)
    }
}
