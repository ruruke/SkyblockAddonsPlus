package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.DrawUtils
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager

open class ButtonText
/**
 * Create a button that displays text.
 */
    (buttonId: Int, x: Int, y: Int, buttonText: String?, feature: Feature?) :
    ButtonFeature(buttonId, x, y, buttonText, feature) {
    fun drawButtonBoxAndText(boxColor: Int, scale: Float, fontColor: Int) {
        Gui.drawRect(xPosition, yPosition, xPosition + this.width, yPosition + this.height, boxColor)
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        DrawUtils.drawCenteredText(
            displayString,
            ((xPosition + width / 2) / scale),
            ((yPosition + (this.height - (8 * scale)) / 2) / scale),
            fontColor
        )
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
}
