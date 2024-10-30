package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

open class ButtonToggle(x: Double, y: Double, main: SkyblockAddonsPlus.Companion, feature: Feature?) :
    ButtonFeature(0, x.toInt(), y.toInt(), "", feature) {
    private val main: SkyblockAddonsPlus.Companion = main

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    private var animationButtonClicked: Long = -1

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        this.feature = feature
        this.width = 31
        this.height = 15
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

        val enabled: Boolean = SkyblockAddonsPlus.configValues!!.isEnabled(feature!!)
        val remoteDisabled: Boolean = SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature)

        if (enabled) {
            ColorUtils.bindColor(36, 255, 98, if (remoteDisabled) 25 else 255) // Green
        } else {
            ColorUtils.bindColor(222, 68, 76, if (remoteDisabled) 25 else 255) // Red
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

        var startingX = getStartingPosition(enabled)
        var slideAnimationOffset = 0

        if (animationButtonClicked != -1L) {
            startingX = getStartingPosition(!enabled) // They toggled so start from the opposite side.

            var timeSinceOpen = (System.currentTimeMillis() - animationButtonClicked).toInt()
            val animationTime = ANIMATION_SLIDE_TIME
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime
            }

            slideAnimationOffset = ANIMATION_SLIDE_DISTANCE * timeSinceOpen / animationTime
        }

        startingX += if (enabled) slideAnimationOffset else -slideAnimationOffset

        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE)
        DrawUtils.drawModalRectWithCustomSizedTexture(startingX.toFloat(), yPosition + 3f, 0f, 0f, 9f, 9f, 9f, 9f, true)
    }

    private fun getStartingPosition(enabled: Boolean): Int {
        return if (!enabled) {
            xPosition + CIRCLE_PADDING_LEFT
        } else {
            getStartingPosition(false) + ANIMATION_SLIDE_DISTANCE
        }
    }

    fun onClick() {
        this.animationButtonClicked = System.currentTimeMillis()
    }

    override fun playPressSound(soundHandler: SoundHandler) {
        if (!SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature)) {
            super.playPressSound(soundHandler)
        }
    }

    companion object {
        private val TOGGLE_INSIDE_CIRCLE = ResourceLocation("skyblockaddons", "gui/toggleinsidecircle.png")
        private val TOGGLE_BORDER = ResourceLocation("skyblockaddons", "gui/toggleborder.png")
        private val TOGGLE_INSIDE_BACKGROUND = ResourceLocation("skyblockaddons", "gui/toggleinsidebackground.png")

        private const val CIRCLE_PADDING_LEFT = 5
        private const val ANIMATION_SLIDE_DISTANCE = 12
        private const val ANIMATION_SLIDE_TIME = 150
    }
}
