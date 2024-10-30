package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.EnumUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

class ButtonCredit(
    x: Double,
    y: Double,
    buttonText: String?,
    credit: EnumUtils.FeatureCredit,
    feature: Feature?,
    smaller: Boolean
) :
    ButtonFeature(0, x.toInt(), y.toInt(), buttonText, feature) {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private val credit: EnumUtils.FeatureCredit
    private val smaller: Boolean

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    init {
        this.feature = feature
        this.width = 12
        this.height = 12
        this.credit = credit
        this.smaller = smaller
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
            var scale = 0.8f
            if (smaller) {
                scale = 0.6f
            }

            hovered =
                mouseX >= this.xPosition * scale && mouseY >= this.yPosition * scale && mouseX < this.xPosition * scale +
                        this.width * scale && mouseY < this.yPosition * scale + this.height * scale
            GlStateManager.enableBlend()

            if (hovered) {
                GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 1)
            } else {
                GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.7f)
            }
            if (main.configValues!!.isRemoteDisabled(feature)) {
                GlStateManager.color(0.3f, 0.3f, 0.3f, 0.7f)
            }
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, 1f)
            mc.getTextureManager().bindTexture(WEB)
            Gui.drawModalRectWithCustomSizedTexture(
                xPosition,
                yPosition, 0f, 0f, 12, 12, 12f, 12f
            )
            GlStateManager.popMatrix()
        }
    }

    fun getCredit(): EnumUtils.FeatureCredit {
        return credit
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val scale = 0.8f
        return mouseX >= this.xPosition * scale && mouseY >= this.yPosition * scale && mouseX < this.xPosition * scale +
                this.width * scale && mouseY < this.yPosition * scale + this.height * scale
    }

    companion object {
        private val WEB = ResourceLocation("skyblockaddons", "gui/web.png")
    }
}
