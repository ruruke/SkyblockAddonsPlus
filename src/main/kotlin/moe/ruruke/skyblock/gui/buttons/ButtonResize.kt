package moe.ruruke.skyblock.gui.buttons


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse



class ButtonResize( var x: Float, var y: Float, feature: Feature?, private val corner: Corner) :
    ButtonFeature(0, 0, 0, "", feature) {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    fun getCorner(): Corner {
        return corner
    }
    private var cornerOffsetX = 0f
    private var cornerOffsetY = 0f

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val scale: Float = SkyblockAddonsPlus.configValues!!.getGuiScale(feature!!)
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)

        hovered =
            mouseX >= (x - SIZE) * scale && mouseY >= (y - SIZE) * scale && mouseX < (x + SIZE) * scale && mouseY < (y + SIZE) * scale
        val color: Int = if (hovered) ColorCode.WHITE.getColor() else ColorCode.WHITE.getColor(70)
        DrawUtils.drawRectAbsolute(
            (x - SIZE).toDouble(),
            (y - SIZE).toDouble(),
            (x + SIZE).toDouble(),
            (y + SIZE).toDouble(),
            color
        )

        GlStateManager.popMatrix()
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val sr: ScaledResolution = ScaledResolution(mc)
        val minecraftScale: Float = sr.getScaleFactor().toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY: Float = (mc.displayHeight - Mouse.getY()) / minecraftScale

        cornerOffsetX = floatMouseX
        cornerOffsetY = floatMouseY

        return hovered
    }

    enum class Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    companion object {
        private const val SIZE = 2
    }
}
