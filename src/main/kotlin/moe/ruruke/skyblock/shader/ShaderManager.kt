package moe.ruruke.skyblock.shader


import moe.ruruke.skyblock.SkyblockAddonsPlus
import java.lang.reflect.TypeVariable


enum class ShaderManager {
    INSTANCE;

    // TODO Disable this code until there is a shader that actually uses a custom pipeline
    //    private ByteBuffer dataBuffer = BufferUtils.createByteBuffer(1_000);
    //    private int vertexArrayObject = ShaderHelper.getInstance().glGenVertexArrays();
    //    private int vertexBufferObject = ShaderHelper.getInstance().glGenBuffers();
    //    private FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    //    private FloatBuffer modelViewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private val shaders: HashMap<Class<out Shader>, Shader> =
        HashMap()
    private var activeShaderType: Class<out Shader>? = null
    private var activeShader: Shader? = null // Convenience

    fun <T : Shader> enableShader(shaderClass: Class<T>): T? {
        if (activeShaderType == shaderClass) {
            return activeShader as T?
        }

        if (activeShader != null) {
            disableShader()
        }

        var shader = shaders[shaderClass]// as T?
        if (shader == null) {
            shader = newInstance(shaderClass)
            shaders.put(shaderClass, shader!!)// = shader// as TypeVariable<*>
        }

        if (shader == null) {
            return null
        }

        activeShaderType = shaderClass
        activeShader = shader

        // Enable the shader
        activeShader!!.enable()
        // Update uniforms
        activeShader!!.updateUniforms()

        return shader as T?
    }

    private fun <T : Shader?> newInstance(shaderClass: Class<T>): T? {
        try {
            return shaderClass.getConstructor().newInstance()
        } catch (ex: Exception) {
            logger.error("An error occurred while creating a shader!", ex)
        }
        return null
    }

    fun disableShader() {
        if (activeShader == null) {
            return
        }

        activeShader!!.disable()

        activeShaderType = null
        activeShader = null
    }

    val isShaderEnabled: Boolean
        get() = activeShader != null

    fun onRenderWorldRendererBuffer(): Boolean {
        return isShaderEnabled && !activeShader!!.isUsingFixedPipeline

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
        // Copy world renderer buffer...
//        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
//        ByteBuffer worldRendererBuffer = worldRenderer.getByteBuffer();

        // Update buffer data
//        ShaderHelper.glBindVertexArray(ShaderManager.INSTANCE.getVertexArrayObject());
//        ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, ShaderManager.INSTANCE.getVertexBufferObject());
//        ShaderHelper.glBufferData(ShaderHelper.GL_ARRAY_BUFFER, worldRendererBuffer, ShaderHelper.GL_DYNAMIC_DRAW);

        // Render
//        ShaderHelper.glBindVertexArray(vertexArrayObject);
//        GL11.glDrawArrays(GL11.GL_QUADS, 0, worldRenderer.getVertexCount());

        // Finish
//        ShaderHelper.glBindVertexArray(0);
//        ShaderHelper.glBindBuffer(ShaderHelper.getInstance().GL_ARRAY_BUFFER, 0);
    }

    fun areShadersSupported(): Boolean {
        return ShaderHelper.isSHADERS_SUPPORTED()
    }
    fun getInstance(): Shader? {
        return activeShader
    }

    companion object {
        private val logger = SkyblockAddonsPlus.getLogger()

        val instance: ShaderManager
            get() = INSTANCE
    }
}
