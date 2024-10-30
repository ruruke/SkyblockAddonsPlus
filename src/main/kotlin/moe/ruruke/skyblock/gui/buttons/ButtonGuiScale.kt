package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.objects.FloatPair
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import java.math.BigDecimal
import java.math.RoundingMode

class ButtonGuiScale : ButtonFeature {
    private var sliderValue = 0f
    private var dragging = false
    private val isXScale: Boolean?

    private val main: SkyblockAddonsPlus.Companion

    constructor(
        x: Double,
        y: Double,
        width: Int,
        height: Int,
        main: SkyblockAddonsPlus.Companion,
        feature: Feature
    ) : super(
        0,
        x.toInt(),
        y.toInt(),
        "",
        feature
    ) {
        val sliderValue: Float = SkyblockAddonsPlus.configValues!!.getGuiScale(feature, false)

        if (java.lang.Float.isInfinite(sliderValue) || java.lang.Float.isNaN(sliderValue)) {
            throw NumberFormatException(("GUI scale for feature " + feature.getId()).toString() + " is infinite or NaN.")
        }

        this.sliderValue = sliderValue
        this.displayString = Translations.getMessage(
            "settings.guiScale",
            getRoundedValue(SkyblockAddonsPlus.configValues!!.getGuiScale(feature)).toString()
        )
        this.main = main
        this.width = width
        this.height = height
        this.isXScale = null
    }

    /**
     * Overloaded for x and y scale (only used on bars currently)
     */
    constructor(
        x: Double,
        y: Double,
        width: Int,
        height: Int,
        main: SkyblockAddonsPlus.Companion,
        feature: Feature?,
        isXScale: Boolean
    ) : super(0, x.toInt(), y.toInt(), "", feature) {
        val sizes: FloatPair = SkyblockAddonsPlus.configValues!!.getSizes(feature!!)
        if (isXScale) {
            this.sliderValue = sizes.getX()
            this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_X.getMessage(
                getRoundedValue(
                    SkyblockAddonsPlus.configValues!!.getSizesX(feature)
                ).toString()
            )
        } else {
            this.sliderValue = sizes.getY()
            this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_Y.getMessage(
                getRoundedValue(
                    SkyblockAddonsPlus.configValues!!.getSizesX(feature)
                ).toString()
            )
        }
        this.isXScale = isXScale
        this.main = main
        this.width = width
        this.height = height
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        mc.getTextureManager().bindTexture(GuiButton.buttonTextures)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        this.hovered =
            mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.blendFunc(770, 771)
        var boxAlpha = 100
        if (hovered) {
            boxAlpha = 170
        }
        Gui.drawRect(
            this.xPosition,
            this.yPosition,
            this.xPosition + this.width,
            this.yPosition + this.height, SkyblockAddonsPlus.utils!!.getDefaultColor(boxAlpha.toFloat())
        )
        this.mouseDragged(mc, mouseX, mouseY)
        var j = 14737632
        if (packedFGColour != 0) {
            j = packedFGColour
        } else if (!this.enabled) {
            j = 10526880
        } else if (this.hovered) {
            j = 16777120
        }
        drawCenteredString(
            mc.fontRendererObj,
            this.displayString,
            this.xPosition + this.width / 2,
            this.yPosition + (this.height - 8) / 2, j
        )
    }

    override fun getHoverState(mouseOver: Boolean): Int {
        return 0
    }

    override fun mouseDragged(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (mouseX - (this.xPosition + 4)).toFloat() / (this.width - 8).toFloat()
                this.sliderValue = MathHelper.clamp_float(sliderValue, 0.0f, 1.0f)
                setNewScale()
            }

            mc.getTextureManager().bindTexture(GuiButton.buttonTextures)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            Gui.drawRect(
                this.xPosition + (this.sliderValue * (this.width - 8).toFloat()).toInt() + 1,
                this.yPosition,
                this.xPosition + (this.sliderValue * (this.width - 8).toFloat()).toInt() + 7,
                this.yPosition + this.height, ColorCode.GRAY.getColor()
            )
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (mouseX - (this.xPosition + 4)).toFloat() / (this.width - 8).toFloat()
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0f, 1.0f)
            setNewScale()
            this.dragging = true
            return true
        } else {
            return false
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        this.dragging = false
    }

    private fun getRoundedValue(value: Float): Float {
        return BigDecimal.valueOf(value.toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
    }

    private fun setNewScale() {
        if (isXScale == null) {
            SkyblockAddonsPlus.configValues!!.setGuiScale(feature!!, sliderValue)
            this.displayString = Translations.getMessage(
                "settings.guiScale",
                getRoundedValue(SkyblockAddonsPlus.configValues!!.getGuiScale(feature!!)).toString()
            )
        } else {
            val sizes: FloatPair = SkyblockAddonsPlus.configValues!!.getSizes(feature!!)
            if (isXScale) {
                sizes.setX(sliderValue)
                this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_X.getMessage(
                    getRoundedValue(
                        SkyblockAddonsPlus.configValues!!.getSizesX(feature!!)
                    ).toString()
                )
            } else {
                sizes.setY(sliderValue)
                this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_Y.getMessage(
                    getRoundedValue(
                        SkyblockAddonsPlus.configValues!!.getSizesY(feature!!)
                    ).toString()
                )
            }
        }
    }
}

