package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.util.function.Supplier

class ButtonToggleNew(
    x: Double,
    y: Double,
    height: Int,
    enabledSupplier: Supplier<Boolean>,
    onClickRunnable: Runnable
) :
    GuiButton(0, x.toInt(), y.toInt(), "") {
    private val circlePaddingLeft: Int
    private val animationSlideDistance: Int
    private val animationSlideTime = 150

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    private var animationButtonClicked: Long = -1

    private val enabledSupplier: Supplier<Boolean>
    private val onClickRunnable: Runnable

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.width = Math.round(height * 2.07).toInt()
        this.height = height
        this.enabledSupplier = enabledSupplier
        this.onClickRunnable = onClickRunnable

        circlePaddingLeft = height / 3
        animationSlideDistance = Math.round(height * 0.8f)
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

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
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.7f)
        if (hovered) {
            GlStateManager.color(1f, 1f, 1f, 1f)
        }

        ColorUtils.bindColor(-0xe1dad2)
        mc.getTextureManager().bindTexture(TOGGLE_BORDER)
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

        val enabled = enabledSupplier.get()
        if (enabled) {
            ColorUtils.bindColor(36, 255, 98, 255) // Green
        } else {
            ColorUtils.bindColor(222, 68, 76, 255) // Red
        }

        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_BACKGROUND)
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

        var startingX = getButtonStartingX(enabled)
        var slideAnimationOffset = 0

        if (animationButtonClicked != -1L) {
            startingX = getButtonStartingX(!enabled) // They toggled so start from the opposite side.

            var timeSinceOpen = (System.currentTimeMillis() - animationButtonClicked).toInt()
            val animationTime = animationSlideTime
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime
            }

            slideAnimationOffset = animationSlideDistance * timeSinceOpen / animationTime
        }

        startingX += if (enabled) slideAnimationOffset else -slideAnimationOffset

        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE)
        val circleSize = Math.round(height * 0.6f).toInt() // 60% of the height.
        val y =
            Math.round(yPosition + (this.height * 0.2f)).toInt() // 20% OF the height.
        DrawUtils.drawModalRectWithCustomSizedTexture(
            startingX.toFloat(),
            y.toFloat(),
            0f,
            0f,
            circleSize.toFloat(),
            circleSize.toFloat(),
            circleSize.toFloat(),
            circleSize.toFloat(),
            true
        )
    }

    /**
     * The inside circle starts at either the left or right
     * side depending on whether this button is enabled.
     * This returns that x position.
     */
    private fun getButtonStartingX(enabled: Boolean): Int {
        return if (!enabled) {
            xPosition + circlePaddingLeft
        } else {
            getButtonStartingX(false) + animationSlideDistance
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val pressed =
            mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height

        if (pressed) {
            this.animationButtonClicked = System.currentTimeMillis()
            onClickRunnable.run()
        }

        return pressed
    }

    companion object {
        private val TOGGLE_INSIDE_CIRCLE = ResourceLocation("skyblockaddons", "gui/toggleinsidecircle.png")
        private val TOGGLE_BORDER = ResourceLocation("skyblockaddons", "gui/toggleborder.png")
        private val TOGGLE_INSIDE_BACKGROUND = ResourceLocation("skyblockaddons", "gui/toggleinsidebackground.png")
    }
}
