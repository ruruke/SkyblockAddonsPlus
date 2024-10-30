package moe.ruruke.skyblock.shader


import moe.ruruke.skyblock.utils.SkyblockAddonsMessageFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.*
import java.nio.ByteBuffer

/**
 * This class provides methods to check what opengl capabilities are supported.
 *
 * Please use the provided methods instead of calling opengl methods directly to avoid crashes!
 */
object ShaderHelper {
    private val LOGGER: Logger = LogManager.getLogger(
        SkyblockAddonsMessageFactory(
            ShaderHelper::class.java.simpleName
        )
    )

    
    private val SHADERS_SUPPORTED: Boolean


    
    private val VBOS_SUPPORTED: Boolean

    
    private val VAOS_SUPPORTED: Boolean

    private var USING_ARB_SHADERS = false
    private val USING_ARB_VBOS: Boolean
    private var USING_ARB_VAOS = false

    var GL_LINK_STATUS: Int = 0
    var GL_ARRAY_BUFFER: Int = 0
    var GL_DYNAMIC_DRAW: Int = 0
    var GL_COMPILE_STATUS: Int = 0
    var GL_VERTEX_SHADER: Int = 0
    var GL_FRAGMENT_SHADER: Int = 0

    fun isSHADERS_SUPPORTED(): Boolean {
        return SHADERS_SUPPORTED
    }

    init {
        val infoBuilder = StringBuilder()

        val capabilities = GLContext.getCapabilities()

        // Check OpenGL 3.0
        val openGL33Supported = capabilities.OpenGL30
        VAOS_SUPPORTED = openGL33Supported || capabilities.GL_ARB_vertex_array_object
        infoBuilder.append("VAOs are ").append(if (VAOS_SUPPORTED) "" else "not ").append("available. ")
        if (VAOS_SUPPORTED) {
            if (capabilities.OpenGL30) {
                infoBuilder.append("OpenGL 3.0 is supported. ")
                USING_ARB_VAOS = false
            } else {
                infoBuilder.append("GL_ARB_vertex_array_object is supported. ")
                USING_ARB_VAOS = true
            }
        } else {
            infoBuilder.append("OpenGL 3.0 is not supported and GL_ARB_vertex_array_object is not supported. ")
            USING_ARB_VAOS = false
        }

        // Check OpenGL 2.0
        val openGL21Supported = capabilities.OpenGL20
        SHADERS_SUPPORTED =
            openGL21Supported || capabilities.GL_ARB_vertex_shader && capabilities.GL_ARB_fragment_shader && capabilities.GL_ARB_shader_objects
        infoBuilder.append("Shaders are ").append(if (SHADERS_SUPPORTED) "" else "not ").append("available. ")
        if (SHADERS_SUPPORTED) {
            if (capabilities.OpenGL20) {
                infoBuilder.append("OpenGL 2.0 is supported. ")
                USING_ARB_SHADERS = false
                GL_LINK_STATUS = GL20.GL_LINK_STATUS
                GL_COMPILE_STATUS = GL20.GL_COMPILE_STATUS
                GL_VERTEX_SHADER = GL20.GL_VERTEX_SHADER
                GL_FRAGMENT_SHADER = GL20.GL_FRAGMENT_SHADER
            } else {
                infoBuilder.append("ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported. ")
                USING_ARB_SHADERS = true
                GL_LINK_STATUS = ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB
                GL_COMPILE_STATUS = ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB
                GL_VERTEX_SHADER = ARBVertexShader.GL_VERTEX_SHADER_ARB
                GL_FRAGMENT_SHADER = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB
            }
        } else {
            infoBuilder.append("OpenGL 2.0 is not supported and ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are not supported. ")
            USING_ARB_SHADERS = false
            GL_LINK_STATUS = GL11.GL_FALSE
            GL_COMPILE_STATUS = GL11.GL_FALSE
            GL_VERTEX_SHADER = GL11.GL_FALSE
            GL_FRAGMENT_SHADER = GL11.GL_FALSE
        }

        // Check OpenGL 1.5
        USING_ARB_VBOS = !capabilities.OpenGL15 && capabilities.GL_ARB_vertex_buffer_object
        VBOS_SUPPORTED = capabilities.OpenGL15 || USING_ARB_VBOS
        infoBuilder.append("VBOs are ").append(if (VBOS_SUPPORTED) "" else "not ").append("available. ")
        if (VBOS_SUPPORTED) {
            if (USING_ARB_VBOS) {
                infoBuilder.append("ARB_vertex_buffer_object is supported. ")
                GL_ARRAY_BUFFER = ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB
                GL_DYNAMIC_DRAW = ARBVertexBufferObject.GL_DYNAMIC_DRAW_ARB
            } else {
                infoBuilder.append("OpenGL 1.5 is supported. ")
                GL_ARRAY_BUFFER = GL15.GL_ARRAY_BUFFER
                GL_DYNAMIC_DRAW = GL15.GL_DYNAMIC_DRAW
            }
        } else {
            infoBuilder.append("OpenGL 1.5 is not supported and ARB_vertex_buffer_object is not supported. ")
            GL_ARRAY_BUFFER = GL11.GL_FALSE
            GL_DYNAMIC_DRAW = GL11.GL_FALSE
        }

        LOGGER.info(infoBuilder.toString())
    }

    fun glLinkProgram(program: Int) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glLinkProgramARB(program)
        } else {
            GL20.glLinkProgram(program)
        }
    }

    fun glGetProgramInfoLog(program: Int, maxLength: Int): String {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetInfoLogARB(
            program,
            maxLength
        ) else GL20.glGetProgramInfoLog(program, maxLength)
    }

    fun glGetProgrami(program: Int, pname: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetObjectParameteriARB(program, pname) else GL20.glGetProgrami(
            program,
            pname
        )
    }

    fun glUseProgram(program: Int) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUseProgramObjectARB(program)
        } else {
            GL20.glUseProgram(program)
        }
    }

    fun glBindBuffer(target: Int, buffer: Int) {
        if (USING_ARB_VBOS) {
            ARBVertexBufferObject.glBindBufferARB(target, buffer)
        } else {
            GL15.glBindBuffer(target, buffer)
        }
    }

    fun glBufferData(target: Int, data: ByteBuffer, usage: Int) {
        if (USING_ARB_VBOS) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage)
        } else {
            GL15.glBufferData(target, data, usage)
        }
    }

    fun glGenBuffers(): Int {
        return if (USING_ARB_VBOS) ARBVertexBufferObject.glGenBuffersARB() else GL15.glGenBuffers()
    }

    fun glAttachShader(program: Int, shaderIn: Int) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glAttachObjectARB(program, shaderIn)
        } else {
            GL20.glAttachShader(program, shaderIn)
        }
    }

    fun glDeleteShader(p_153180_0_: Int) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glDeleteObjectARB(p_153180_0_)
        } else {
            GL20.glDeleteShader(p_153180_0_)
        }
    }

    /**
     * creates a shader with the given mode and returns the GL id. params: mode
     */
    fun glCreateShader(type: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glCreateShaderObjectARB(type) else GL20.glCreateShader(type)
    }

    fun glShaderSource(shaderIn: Int, string: ByteBuffer) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glShaderSourceARB(shaderIn, string)
        } else {
            GL20.glShaderSource(shaderIn, string)
        }
    }

    fun glCompileShader(shaderIn: Int) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glCompileShaderARB(shaderIn)
        } else {
            GL20.glCompileShader(shaderIn)
        }
    }

    fun glGetShaderi(shaderIn: Int, pname: Int): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetObjectParameteriARB(shaderIn, pname) else GL20.glGetShaderi(
            shaderIn,
            pname
        )
    }

    fun glGetShaderInfoLog(shaderIn: Int, maxLength: Int): String {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetInfoLogARB(
            shaderIn,
            maxLength
        ) else GL20.glGetShaderInfoLog(shaderIn, maxLength)
    }

    fun glUniform1f(location: Int, v0: Float) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform1fARB(location, v0)
        } else {
            GL20.glUniform1f(location, v0)
        }
    }

    fun glUniform3f(location: Int, v0: Float, v1: Float, v2: Float) {
        if (USING_ARB_SHADERS) {
            ARBShaderObjects.glUniform3fARB(location, v0, v1, v2)
        } else {
            GL20.glUniform3f(location, v0, v1, v2)
        }
    }

    fun glEnableVertexAttribArray(index: Int) {
        if (USING_ARB_SHADERS) {
            ARBVertexShader.glEnableVertexAttribArrayARB(index)
        } else {
            GL20.glEnableVertexAttribArray(index)
        }
    }

    fun glGetUniformLocation(programObj: Int, name: CharSequence): Int {
        return if (USING_ARB_SHADERS) ARBShaderObjects.glGetUniformLocationARB(
            programObj,
            name
        ) else GL20.glGetUniformLocation(programObj, name)
    }

    fun glVertexAttribPointer(
        index: Int,
        size: Int,
        type: Int,
        normalized: Boolean,
        stride: Int,
        buffer_buffer_offset: Long
    ) {
        if (USING_ARB_SHADERS) {
            ARBVertexShader.glVertexAttribPointerARB(index, size, type, normalized, stride, buffer_buffer_offset)
        } else {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset)
        }
    }

    fun glGenVertexArrays(): Int {
        return if (USING_ARB_VAOS) ARBVertexArrayObject.glGenVertexArrays() else GL30.glGenVertexArrays()
    }

    fun glBindVertexArray(array: Int) {
        if (USING_ARB_VAOS) {
            ARBVertexArrayObject.glBindVertexArray(array)
        } else {
            GL30.glBindVertexArray(array)
        }
    }
}
