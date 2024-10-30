package moe.ruruke.skyblock.gui.buttons


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse


class ButtonLocation
/**
 * Create a button that allows you to change the location of a GUI element.
 */
    (feature: Feature?) : ButtonFeature(-1, 0, 0, null, feature) {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private var boxXOne = 0f
    private var boxXTwo = 0f
    private var boxYOne = 0f
    private var boxYTwo = 0f

    private var scale = 0f


    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        val scale = configValues!!.getGuiScale(feature!!)
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)

        //TODO:
        if (false) {
//        if (feature == Feature.DEFENCE_ICON) { // this one is just a little different
//            main.getRenderListener().drawIcon(scale, mc, this)
        } else {
            feature!!.draw(scale, mc, this)
        }
        GlStateManager.popMatrix()

        if (hovered) {
            lastHoveredFeature = feature
        }
    }


    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    fun checkHoveredAndDrawBox(boxXOne: Float, boxXTwo: Float, boxYOne: Float, boxYTwo: Float, scale: Float) {
        val sr: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val minecraftScale: Float = sr.getScaleFactor().toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY: Float = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale

        hovered =
            floatMouseX >= boxXOne * scale && floatMouseY >= boxYOne * scale && floatMouseX < boxXTwo * scale && floatMouseY < boxYTwo * scale
        var boxAlpha = 70
        if (hovered) {
            boxAlpha = 120
        }
        val boxColor: Int = ColorCode.GRAY.getColor(boxAlpha)
        DrawUtils.drawRectAbsolute(
            boxXOne.toDouble(),
            boxYOne.toDouble(),
            boxXTwo.toDouble(),
            boxYTwo.toDouble(),
            boxColor
        )

        this.boxXOne = boxXOne
        this.boxXTwo = boxXTwo
        this.boxYOne = boxYOne
        this.boxYTwo = boxYTwo
        this.scale = scale
    }

    fun checkHoveredAndDrawBox(
        boxXOne: Float,
        boxXTwo: Float,
        boxYOne: Float,
        boxYTwo: Float,
        scale: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        val sr: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val minecraftScale: Float = sr.getScaleFactor().toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY: Float = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale

        hovered =
            floatMouseX >= boxXOne * scale * scaleX && floatMouseY >= boxYOne * scale * scaleY && floatMouseX < boxXTwo * scale * scaleX && floatMouseY < boxYTwo * scale * scaleY
        var boxAlpha = 70
        if (hovered) {
            boxAlpha = 120
        }
        val boxColor: Int = ColorCode.GRAY.getColor(boxAlpha)
        DrawUtils.drawRectAbsolute(
            boxXOne.toDouble(),
            boxYOne.toDouble(),
            boxXTwo.toDouble(),
            boxYTwo.toDouble(),
            boxColor
        )

        this.boxXOne = boxXOne
        this.boxXTwo = boxXTwo
        this.boxYOne = boxYOne
        this.boxYTwo = boxYTwo
        this.scale = scale
    }


    /**
     * Because the box changes with the scale, have to override this.
     */
    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return this.enabled && this.visible && hovered
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {}

    companion object {
        // So we know the latest hovered feature (used for arrow key movement).
        
        private var lastHoveredFeature: Feature? = null
    }
}
