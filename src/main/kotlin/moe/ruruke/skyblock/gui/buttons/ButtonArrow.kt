package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

class ButtonArrow(x: Double, y: Double, main: SkyblockAddonsPlus.Companion, arrowType: ArrowType, max: Boolean) :
    GuiButton(0, x.toInt(), y.toInt(), null) {
    private val main: SkyblockAddonsPlus.Companion = main

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()
    private val arrowType: ArrowType
    fun getArrowType(): ArrowType {
        return arrowType
    }
    private val max: Boolean

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.width = 30
        this.height = 30
        this.arrowType = arrowType
        this.max = max
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
            if (arrowType == ArrowType.RIGHT) {
                mc.getTextureManager().bindTexture(ARROW_RIGHT)
            } else {
                mc.getTextureManager().bindTexture(ARROW_LEFT)
            }
            if (max) {
                GlStateManager.color(0.5f, 0.5f, 0.5f, alphaMultiplier * 0.5f)
            } else {
                GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.7f)
                if (hovered) {
                    GlStateManager.color(1f, 1f, 1f, 1f)
                }
            }
            Gui.drawModalRectWithCustomSizedTexture(
                xPosition,
                yPosition,
                0f,
                0f,
                width,
                height,
                width.toFloat(),
                height.toFloat()
            )
        }
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        if (!max) {
            super.playPressSound(soundHandlerIn)
        }
    }

    fun isNotMax(): Boolean {
        return !max
    }

    enum class ArrowType {
        LEFT, RIGHT
    }

    companion object {
        private val ARROW_RIGHT = ResourceLocation("skyblockaddonsplus", "gui/arrowright.png")
        private val ARROW_LEFT = ResourceLocation("skyblockaddonsplus", "gui/arrowleft.png")
    }
}
