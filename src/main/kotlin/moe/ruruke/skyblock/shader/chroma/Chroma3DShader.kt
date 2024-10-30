package moe.ruruke.skyblock.shader.chroma

import lombok.Setter
import moe.ruruke.skyblock.shader.UniformType
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.util.Vector3d

/**
 * This shader shows a chroma color on a pixel depending on its position in the world
 *
 * This shader does:
 * - Take in account its position in 3-dimensional space
 *
 * This shader does not:
 * - Preserve the brightness and saturation of the original color
 * - Work with textures
 */
class Chroma3DShader : ChromaShader("chroma_3d") {
    private var alpha = 1f

    fun setAlpha(_alpha: Float) {
        alpha = _alpha
    }

    override fun registerUniforms(veC3: UniformType<Array<Float>>, s: String, function: () -> Array<Float>) {
        super.registerUniforms(UniformType.VEC3, "playerWorldPosition") {
            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
            arrayOf<Float>(viewPosition.x.toFloat(), viewPosition.y.toFloat(), viewPosition.z.toFloat())
        }

//        registerUniforms(UniformType.VEC3, "playerWorldPosition") {
//            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
//            arrayOf<Float>(viewPosition.x.toFloat(), viewPosition.y.toFloat(), viewPosition.z.toFloat())
//        }
        registerUniform(UniformType.FLOAT, "alpha") { alpha }
//        registerUniform(UniformType.FLOAT, "brightness") { main.getConfigValues().getChromaBrightness().floatValue() }
    }
}
