package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import java.math.BigDecimal

class ButtonSlider(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    initialValue: Float,
    min: Float,
    max: Float,
    step: Float,
    sliderCallback: OnSliderChangeCallback
) :
    GuiButton(0, x.toInt(), y.toInt(), "") {
    private val min: Float
    private val max: Float
    private val step: Float

    private var valuePercentage = 0f
    private var dragging = false

    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private val sliderCallback: OnSliderChangeCallback

    private var prefix: String? = null

    init {
        this.displayString = ""
        this.valuePercentage = initialValue
        this.width = width
        this.height = height
        this.sliderCallback = sliderCallback
        this.min = min
        this.max = max
        this.step = step
        this.displayString = getRoundedValue(denormalizeScale(valuePercentage)).toString()
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
                this.valuePercentage = (mouseX - (this.xPosition + 4)).toFloat() / (this.width - 8).toFloat()
                this.valuePercentage = MathHelper.clamp_float(valuePercentage, 0.0f, 1.0f)
                valueUpdated()
            }

            mc.getTextureManager().bindTexture(GuiButton.buttonTextures)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            Gui.drawRect(
                this.xPosition + (this.valuePercentage * (this.width - 8).toFloat()).toInt() + 1,
                this.yPosition,
                this.xPosition + (this.valuePercentage * (this.width - 8).toFloat()).toInt() + 7,
                this.yPosition + this.height, ColorCode.GRAY.getColor()
            )
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.valuePercentage = (mouseX - (this.xPosition + 4)).toFloat() / (this.width - 8).toFloat()
            this.valuePercentage = MathHelper.clamp_float(this.valuePercentage, 0.0f, 1.0f)
            valueUpdated()
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
        return BigDecimal(value.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toFloat()
    }

    fun denormalizeScale(value: Float): Float {
        return MathUtils.denormalizeSliderValue(value, min, max, step)
    }

    fun valueUpdated() {
        sliderCallback.sliderUpdated(valuePercentage)
        this.displayString = (if (prefix != null) prefix else "") + getRoundedValue(denormalizeScale(valuePercentage))
    }

    abstract class OnSliderChangeCallback {
        abstract fun sliderUpdated(value: Float)
    }

    fun setPrefix(text: String?): ButtonSlider {
        prefix = text
        this.displayString = prefix + getRoundedValue(denormalizeScale(valuePercentage))
        return this
    }
}

