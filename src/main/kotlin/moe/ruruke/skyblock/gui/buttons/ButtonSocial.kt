package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager

class ButtonSocial(x: Double, y: Double, main: SkyblockAddonsPlus.Companion, social: EnumUtils.Social) :
    GuiButton(0, x.toInt(), y.toInt(), "") {
    private val main: SkyblockAddonsPlus.Companion = main

    private val social: EnumUtils.Social

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.width = 20
        this.height = 20
        this.social = social
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        var alphaMultiplier = 1f
        if (SkyblockAddonsPlus.utils!!.isFadingIn()) {
            val timeSinceOpen = System.currentTimeMillis() - timeOpened
            val fadeMilis = 500
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = timeSinceOpen.toFloat() / fadeMilis
            }
        }

        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition +
                width && mouseY < yPosition + height
        GlStateManager.enableBlend()

        if (hovered) {
            GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 1)
        } else {
            GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.7f)
        }

        mc.getTextureManager().bindTexture(social.getResourceLocation())
        DrawUtils.drawModalRectWithCustomSizedTexture(
            xPosition.toFloat(),
            yPosition.toFloat(),
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            width.toFloat(),
            height.toFloat(),
            true
        )
    }


    fun getSocial(): EnumUtils.Social {
        return social
    }
}
