package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.MathUtils
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import org.lwjgl.input.Mouse

class NewButtonSlider(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    sliderCallback: UpdateCallback<Float>
) :
    GuiButton(0, x.toInt(), y.toInt(), "") {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private val min: Float
    private val max: Float
    private val step: Float
    private val sliderCallback: UpdateCallback<Float>
    private var prefix = ""

    private var dragging = false
    private var normalizedValue: Float

    init {
        this.width = width
        this.height = height
        this.sliderCallback = sliderCallback
        this.min = min
        this.max = max
        this.step = step
        this.normalizedValue = MathUtils.normalizeSliderValue(value, min, max, step)
        this.displayString = Utils.roundForString(value, 2)
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
            val sr: ScaledResolution = ScaledResolution(mc)
            val minecraftScale: Float = sr.getScaleFactor().toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale

            if (this.dragging) {
                this.normalizedValue = (floatMouseX - (this.xPosition + 4)) / (this.width - 8).toFloat()
                this.normalizedValue = MathHelper.clamp_float(normalizedValue, 0.0f, 1.0f)
                onUpdate()
            }

            mc.getTextureManager().bindTexture(GuiButton.buttonTextures)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            Gui.drawRect(
                this.xPosition + (this.normalizedValue * (this.width - 8).toFloat()).toInt() + 1,
                this.yPosition,
                this.xPosition + (this.normalizedValue * (this.width - 8).toFloat()).toInt() + 7,
                this.yPosition + this.height, ColorCode.GRAY.getColor()
            )
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.normalizedValue = (mouseX - (this.xPosition + 4)).toFloat() / (this.width - 8).toFloat()
            this.normalizedValue = MathHelper.clamp_float(this.normalizedValue, 0.0f, 1.0f)
            onUpdate()
            this.dragging = true
            return true
        } else {
            return false
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        this.dragging = false
    }

    fun setPrefix(text: String): NewButtonSlider {
        prefix = text
        this.updateDisplayString()
        return this
    }

    private fun onUpdate() {
        sliderCallback.onUpdate(denormalize())
        this.updateDisplayString()
    }

    private fun updateDisplayString() {
        this.displayString = prefix + Utils.roundForString(denormalize(), 2)
    }

    fun denormalize(): Float {
        return MathUtils.denormalizeSliderValue(normalizedValue, min, max, step)
    }
}

