package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ButtonSwitchTab(
    x: Double, y: Double, width: Int, height: Int, buttonText: String?, main: SkyblockAddonsPlus.Companion,
    tab: EnumUtils.GuiTab, currentTab: EnumUtils.GuiTab
) : GuiButton(0, x.toInt(), y.toInt(), width, height, buttonText) {
    private val main: SkyblockAddonsPlus.Companion = main
    private val currentTab: EnumUtils.GuiTab
    private val tab: EnumUtils.GuiTab

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.width = width
        this.height = height
        this.currentTab = currentTab
        this.tab = tab
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            var alphaMultiplier = 1f
            if (SkyblockAddonsPlus.utils!!.isFadingIn()) {
                val timeSinceOpen = System.currentTimeMillis() - timeOpened
                val fadeMilis = 500
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = timeSinceOpen.toFloat() / fadeMilis
                }
            }
            hovered =
                mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
            if (currentTab === tab) hovered = false
            if (alphaMultiplier < 0.1) alphaMultiplier = 0.1f
            val boxColor: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue((alphaMultiplier * 50).toInt())
            var fontColor: Int
            fontColor = if (currentTab !== tab) {
                SkyblockAddonsPlus.utils!!.getDefaultBlue((alphaMultiplier * 255).toInt())
            } else {
                SkyblockAddonsPlus.utils!!.getDefaultBlue((alphaMultiplier * 127).toInt())
            }
            if (hovered) {
                fontColor = Color(255, 255, 160, (alphaMultiplier * 255).toInt()).rgb
            }
            Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, boxColor)
            val scale = 1.4f
            val scaleMultiplier = 1 / scale
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, 1f)
            GlStateManager.enableBlend()
            drawCenteredString(
                mc.fontRendererObj, displayString, ((xPosition + width / 2) * scaleMultiplier).toInt(),
                ((yPosition + (this.height - (8 / scaleMultiplier)) / 2) * scaleMultiplier).toInt(), fontColor
            )
            GlStateManager.popMatrix()
        }
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        if (currentTab !== tab) super.playPressSound(soundHandlerIn)
    }

    fun getTab(): EnumUtils.GuiTab {
        return tab
    }
}
