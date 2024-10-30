package moe.ruruke.skyblock.shader

import java.util.*
import java.util.function.Supplier

class Uniform<T>(
    shader: Shader,
    private val uniformType: UniformType<T>,
    private val name: String,
    private val uniformValuesSupplier: Supplier<T>
) {
    private var uniformID = 0
    private var previousUniformValue: T? = null

    init {
        init(shader, name)
    }

    private fun init(shader: Shader, name: String) {
        uniformID = ShaderHelper.glGetUniformLocation(shader.program, name)
    }

    fun update() {
        val newUniformValue = uniformValuesSupplier.get()
        if (!Objects.deepEquals(previousUniformValue, newUniformValue)) {
            if (uniformType == UniformType.FLOAT) {
                ShaderHelper.glUniform1f(uniformID, newUniformValue as Float)
            } else if (uniformType == UniformType.VEC3) {
                val values = newUniformValue as Array<Float>
                ShaderHelper.glUniform3f(uniformID, values[0], values[1], values[2])
            }

            previousUniformValue = newUniformValue
        }
    }
}
