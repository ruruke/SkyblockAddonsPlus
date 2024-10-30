package moe.ruruke.skyblock.utils.draw

import moe.ruruke.skyblock.core.chroma.MulticolorShaderManager.Companion.instance
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.ColorUtils.getAlpha
import moe.ruruke.skyblock.utils.ColorUtils.getBlue
import moe.ruruke.skyblock.utils.ColorUtils.getGreen
import moe.ruruke.skyblock.utils.ColorUtils.getRed
import moe.ruruke.skyblock.utils.DrawUtils.disableOutlineMode
import moe.ruruke.skyblock.utils.DrawUtils.enableOutlineMode
import moe.ruruke.skyblock.utils.DrawUtils.outlineColor
import moe.ruruke.skyblock.utils.SkyblockColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.VertexFormat
import org.lwjgl.opengl.GL11


abstract class DrawState {
    protected var canAddVertices: Boolean
    protected var drawType: Int = 0
    protected var format: VertexFormat? = null
    protected var textured: Boolean
    protected var ignoreTexture: Boolean
    protected var color: SkyblockColor


    constructor(
        theColor: SkyblockColor,
        theDrawType: Int,
        theFormat: VertexFormat?,
        isTextured: Boolean,
        shouldIgnoreTexture: Boolean
    ) {
        color = theColor
        drawType = theDrawType
        format = theFormat
        textured = isTextured
        ignoreTexture = shouldIgnoreTexture
        canAddVertices = true
    }

    constructor(theColor: SkyblockColor, isTextured: Boolean, shouldIgnoreTexture: Boolean) {
        color = theColor
        ignoreTexture = shouldIgnoreTexture
        textured = isTextured
        canAddVertices = false
    }

    fun beginWorld() {
        if (canAddVertices) {
            worldRenderer.begin(drawType, format)
        }
    }

    fun draw() {
        if (canAddVertices) {
            tessellator.draw()
        }
    }

    protected open fun setColor(_color: SkyblockColor): DrawState {
        color = _color
        return this
    }


    protected fun newColor(is3D: Boolean) {
        if (color.drawMulticolorUsingShader()) {
            instance.begin(textured, ignoreTexture, is3D)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        }
        if (textured && ignoreTexture) {
            enableOutlineMode()
            // Textured shader needs white color to work properly
            if (color.drawMulticolorUsingShader()) {
                outlineColor(-0x1)
            } else {
                outlineColor(color.color)
            }
        }
    }

    protected fun bindColor(colorInt: Int) {
        if (textured && ignoreTexture) {
            if (color.isPositionalMulticolor && color.drawMulticolorManually()) {
                outlineColor(colorInt)
            }
        } else {
            GlStateManager.color(
                ColorUtils.getRed(colorInt) / 255f,
                ColorUtils.getGreen(colorInt) / 255f,
                ColorUtils.getBlue(colorInt) / 255f,
                ColorUtils.getAlpha(colorInt) / 255f
            )
        }
    }

    protected fun endColor() {
        if (color.drawMulticolorUsingShader()) {
            instance.end()
            GlStateManager.shadeModel(GL11.GL_FLAT)
        }
        if (textured && ignoreTexture) {
            disableOutlineMode()
        }
    }

    fun reColor(newColor: SkyblockColor) {
        color = newColor
    }

    companion object {
        private val tessellator: Tessellator = Tessellator.getInstance()
        private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    }
}
