package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation


class ButtonSettings(x: Double, y: Double, buttonText: String?, main: SkyblockAddonsPlus.Companion, feature: Feature?) :
    ButtonFeature(0, x.toInt(), y.toInt(), buttonText, feature) {
    private val main: SkyblockAddonsPlus.Companion = main

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.feature = feature
        this.width = 15
        this.height = 15
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
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.enableBlend()
            GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.7f)
            if (hovered) {
                GlStateManager.color(1f, 1f, 1f, 1f)
            }
            mc.getTextureManager().bindTexture(GEAR)
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
    }


    companion object {
        private val GEAR = ResourceLocation("skyblockaddonsplus", "gui/gear.png")
    }
}
