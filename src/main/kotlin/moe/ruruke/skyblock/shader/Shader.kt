package moe.ruruke.skyblock.shader

import lombok.Getter
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.shader.ShaderLinkHelper
import net.minecraft.util.Vector3d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.OpenGLException
import java.util.function.Supplier

abstract class Shader private constructor(vertex: String, fragment: String, vertexFormat: VertexFormat?) {
    private val VERTEX: String? = vertex
    private val FRAGMENT: String? = fragment
    private val VERTEX_FORMAT = vertexFormat

    @Getter
    var program: Int = 0
    private val uniforms: MutableList<Uniform<*>> = ArrayList<Uniform<*>>()

    constructor(vertex: String, fragment: String) : this(vertex, fragment, null)

    init {
        this.init()
    }

    @Throws(Exception::class)
    private fun init() {
        // Create programs, load shaders, and link shaders
        program = ShaderLinkHelper.getStaticShaderLinkHelper().createProgram()
        if (VERTEX != null) {
            val vertexShaderLoader: ShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.VERTEX, VERTEX)
            vertexShaderLoader.attachShader(this)
        }
        if (FRAGMENT != null) {
            val fragmentShaderLoader: ShaderLoader = ShaderLoader.load(ShaderLoader.ShaderType.FRAGMENT, FRAGMENT)
            fragmentShaderLoader.attachShader(this)
        }
        ShaderHelper.glLinkProgram(program)

        // Check link status
        val linkStatus: Int = ShaderHelper.glGetProgrami(program, ShaderHelper.GL_LINK_STATUS)
        if (linkStatus == GL11.GL_FALSE) {
            throw OpenGLException(
                ("Error encountered when linking program containing VS " + VERTEX + " and FS " + FRAGMENT + ": "
                        + ShaderHelper.glGetProgramInfoLog(program, 32768))
            )
        }

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
        // If the vertex format is null we are using the fixed pipeline instead
//        if (!isUsingFixedPipeline()) {
        // Set up VAOs & VBOs
//            ShaderHelper.glBindVertexArray(ShaderManager.INSTANCE.getVertexArrayObject());
//            ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, ShaderManager.INSTANCE.getVertexBufferObject());
        // or ShaderManager.INSTANCE.getDataBuffer()
//            ShaderHelper.glBufferData(ShaderHelper.GL_ARRAY_BUFFER, Tessellator.getInstance().getWorldRenderer().getByteBuffer(), ShaderHelper.GL_DYNAMIC_DRAW);

//            int stride = VERTEX_FORMAT.getVertexFormatElements().stream().mapToInt(VertexFormatElement::getTotalSize).sum();
//            int index = 0;
//            int bufferOffset = 0;
//            for (VertexFormatElement bufferElementType : VERTEX_FORMAT.getVertexFormatElements()) {
//                ShaderHelper.glEnableVertexAttribArray(index);
//                ShaderHelper.glVertexAttribPointer(index, bufferElementType.getCount(), bufferElementType.getElementType().getGlType(),
//                        bufferElementType.getElementType().isNormalize(), stride, bufferOffset);
//                index++;
//                bufferOffset += bufferElementType.getTotalSize();
//            }
//        }

        // Add uniforms
        this.registerUniforms(UniformType.VEC3, "playerWorldPosition") {
            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
            arrayOf<Float>(viewPosition.x.toFloat(), viewPosition.y.toFloat(), viewPosition.z.toFloat())
        }

        // TODO Disable this code until there is a shader that actually uses a custom pipeline
//        if (!isUsingFixedPipeline()) {
        // Unbind all
//            ShaderHelper.glBindVertexArray(0);
//            ShaderHelper.glBindBuffer(ShaderHelper.GL_ARRAY_BUFFER, 0);
//        }
    }

    protected open fun registerUniforms(veC3: UniformType<Array<Float>>, s: String, function: () -> Array<Float>) {
    }

    fun updateUniforms() {
        for (uniform in uniforms) {
            uniform.update()
        }
    }

    fun enable() {
        ShaderHelper.glUseProgram(program)
    }

    fun disable() {
        ShaderHelper.glUseProgram(0)
    }

    val isUsingFixedPipeline: Boolean
        get() = VERTEX_FORMAT == null

    fun <T> registerUniform(uniformType: UniformType<T>?, name: String?, uniformValuesSupplier: Supplier<T>?) {
        uniforms.add(Uniform(this, uniformType!!, name!!, uniformValuesSupplier!!))
    }

    companion object {
        protected val main: SkyblockAddonsPlus.Companion = instance
    }
}
