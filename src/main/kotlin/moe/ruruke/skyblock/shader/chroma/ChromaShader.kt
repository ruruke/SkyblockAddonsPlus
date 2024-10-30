package moe.ruruke.skyblock.shader.chroma

import moe.ruruke.skyblock.shader.Shader
import moe.ruruke.skyblock.shader.UniformType


abstract class ChromaShader(shaderName: String?) : Shader(shaderName!!, shaderName) {
    protected override fun registerUniforms(veC3: UniformType<Array<Float>>, s: String, function: () -> Array<Float>) {
        // Chroma size is made proportionate to the size of the screen (ex. in a 1920px width screen, 100 = 1920)
        //TODO:
//        registerUniform(UniformType.FLOAT, "chromaSize") {
//            main.configValues.getChromaSize().floatValue() * (Minecraft.getMinecraft().displayWidth / 100f)
//        }
//        registerUniform(UniformType.FLOAT, "timeOffset") {
//            val ticks: Float = main.getNewScheduler().getTotalTicks() as Float + Utils.getPartialTicks()
//            val chromaSpeed: Float = main.configValues!!.getChromaSpeed().floatValue() / 360f
//            ticks * chromaSpeed
//        }
//        registerUniform(UniformType.FLOAT, "saturation") { main.configValues!!.getChromaSaturation().floatValue() }
    }
}
