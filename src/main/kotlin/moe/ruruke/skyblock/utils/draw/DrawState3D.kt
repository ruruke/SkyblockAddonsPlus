package moe.ruruke.skyblock.utils.draw

import moe.ruruke.skyblock.utils.ColorUtils.getAlpha
import moe.ruruke.skyblock.utils.ColorUtils.getBlue
import moe.ruruke.skyblock.utils.ColorUtils.getGreen
import moe.ruruke.skyblock.utils.ColorUtils.getRed
import moe.ruruke.skyblock.utils.SkyblockColor
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.VertexFormat

class DrawState3D : DrawState {
    constructor(
        theColor: SkyblockColor,
        theDrawType: Int,
        theFormat: VertexFormat?,
        isTextured: Boolean,
        shouldIgnoreTexture: Boolean
    ) : super(theColor, theDrawType, theFormat, isTextured, shouldIgnoreTexture)

    constructor(theColor: SkyblockColor, isTextured: Boolean, shouldIgnoreTexture: Boolean) : super(
        theColor,
        isTextured,
        shouldIgnoreTexture
    )

    fun newColorEnv(): DrawState3D {
        super.newColor(true)
        return this
    }

    fun endColorEnv(): DrawState3D {
        super.endColor()
        return this
    }

    public override fun setColor(color: SkyblockColor): DrawState3D {
        super.reColor(color)
        return this
    }

    fun beginWorldRenderer(): DrawState3D {
        super.beginWorld()
        return this
    }

    fun bindColor(x: Float, y: Float, z: Float): DrawState3D {
        super.bindColor(color.getColorAtPosition(x, y, z))
        return this
    }

    fun addColoredVertex(x: Float, y: Float, z: Float): DrawState3D {
        // Add a new position in the world with the correct color
        if (canAddVertices) {
            if (color.drawMulticolorManually()) {
                val colorInt = color.getColorAtPosition(x, y)
                worldRenderer.pos(x.toDouble(), y.toDouble(), z.toDouble())
                    .color(getRed(colorInt), getGreen(colorInt), getBlue(colorInt), getAlpha(colorInt)).endVertex()
            } else {
                worldRenderer.pos(x.toDouble(), y.toDouble(), z.toDouble()).endVertex()
            }
        } else {
            bindColor(x, y, z)
        }
        return this
    }

    companion object {
        private val worldRenderer: WorldRenderer = Tessellator.getInstance().worldRenderer
    }
}
