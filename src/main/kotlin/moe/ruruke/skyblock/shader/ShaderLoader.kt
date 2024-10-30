package moe.ruruke.skyblock.shader

import lombok.Getter
import moe.ruruke.skyblock.shader.ShaderHelper.glAttachShader
import moe.ruruke.skyblock.shader.ShaderHelper.glCompileShader
import moe.ruruke.skyblock.shader.ShaderHelper.glCreateShader
import moe.ruruke.skyblock.shader.ShaderHelper.glDeleteShader
import moe.ruruke.skyblock.shader.ShaderHelper.glGetShaderInfoLog
import moe.ruruke.skyblock.shader.ShaderHelper.glGetShaderi
import moe.ruruke.skyblock.shader.ShaderHelper.glShaderSource
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.apache.commons.lang3.StringUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.OpenGLException
import java.io.BufferedInputStream

class ShaderLoader private constructor(
    private val shaderType: ShaderType,
    private val shader: Int,
    private val fileName: String
) {
    private var shaderAttachCount = 0

    fun attachShader(shader: Shader) {
        ++this.shaderAttachCount
        glAttachShader(shader.program, this.shader)
    }

    fun deleteShader() {
        --this.shaderAttachCount

        if (this.shaderAttachCount <= 0) {
            glDeleteShader(this.shader)
            shaderType.getSavedShaderLoaders().remove(this.fileName)
        }
    }

    @Getter
    enum class ShaderType(private val shaderExtension: String, private val glShaderType: Int) {
        VERTEX(".vsh", ShaderHelper.GL_VERTEX_SHADER),
        FRAGMENT(".fsh", ShaderHelper.GL_FRAGMENT_SHADER);

        fun getShaderExtension(): String {
            return shaderExtension
        }
        fun getGlShaderType(): Int {
            return glShaderType
        }
        private var savedShaderLoaders: MutableMap<String, ShaderLoader> = HashMap()
        fun getSavedShaderLoaders(): MutableMap<String, ShaderLoader> {
            return savedShaderLoaders
        }
    }

    companion object {
        @Throws(Exception::class)
        fun load(type: ShaderType, fileName: String): ShaderLoader {
            var shaderLoader: ShaderLoader = type.getSavedShaderLoaders().get(fileName)!!


            if (shaderLoader == null) {
                val resourceLocation =
                    ResourceLocation("skyblockaddons", "shaders/program/" + fileName + type.getShaderExtension())
                val bufferedInputStream =
                    BufferedInputStream(Minecraft.getMinecraft().resourceManager.getResource(resourceLocation).inputStream)
                val bytes: ByteArray = Utils.toByteArray(bufferedInputStream)
                val buffer = BufferUtils.createByteBuffer(bytes.size)
                buffer.put(bytes)
                buffer.position(0)

                val shaderID = glCreateShader(type.getGlShaderType())
                glShaderSource(shaderID, buffer)
                glCompileShader(shaderID)

                if (glGetShaderi(shaderID, ShaderHelper.GL_COMPILE_STATUS) == 0) {
                    throw OpenGLException(
                        "An error occurred while compiling shader " + fileName + ": " +
                                StringUtils.trim(glGetShaderInfoLog(shaderID, 32768))
                    )
                }

                shaderLoader = ShaderLoader(type, shaderID, fileName)
                type.getSavedShaderLoaders().put(fileName, shaderLoader)
            }

            return shaderLoader
        }
    }
}