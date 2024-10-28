package moe.ruruke.skyblock.tweaker

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import moe.ruruke.skyblock.tweaker.transformer.ITransformer
import net.minecraft.launchwrapper.IClassTransformer
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.relauncher.FMLRelaunchLog
import org.apache.commons.lang3.mutable.MutableInt
import org.apache.logging.log4j.Level
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.util.function.Consumer

class SkyblockAddonsTransformer : IClassTransformer {
    private val transformerMap: Multimap<String, ITransformer> = ArrayListMultimap.create()

    private fun registerTransformer(transformer: ITransformer) {
        for (cls in transformer.className!!) {
            transformerMap.put(cls, transformer)
        }
    }

    override fun transform(name: String, transformedName: String, bytes: ByteArray?): ByteArray? {
        if (bytes == null) {
            return null
        }

        val transformers = transformerMap[transformedName]
        if (transformers.isEmpty()) {
            return bytes
        }

        val reader = ClassReader(bytes)
        val node = ClassNode()
        reader.accept(node, ClassReader.EXPAND_FRAMES)

        val classWriterFlags = MutableInt(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

        transformers.forEach(Consumer { transformer: ITransformer ->
            log(
                Level.INFO,
                String.format("Applying transformer %s on %s...", transformer.javaClass.name, transformedName)
            )
            transformer.transform(node, transformedName)
        })

        val writer = ClassWriter(classWriterFlags.value)

        try {
            node.accept(writer)
        } catch (ex: Throwable) {
            log(Level.ERROR, "An exception occurred while transforming $transformedName")
            ex.printStackTrace()
            outputBytecode(transformedName, writer)
            return bytes
        }

        outputBytecode(transformedName, writer)

        return writer.toByteArray()
    }

    private fun outputBytecode(transformedName: String, writer: ClassWriter) {
        try {
            val bytecodeDirectory = File("bytecode")
            if (!bytecodeDirectory.exists()) return

            val bytecodeOutput = File(bytecodeDirectory, "$transformedName.class")
            if (!bytecodeOutput.exists()) bytecodeOutput.createNewFile()

            val os = FileOutputStream(bytecodeOutput)
            os.write(writer.toByteArray())
            os.close()
        } catch (ex: Exception) {
            log(
                Level.ERROR,
                "An error occurred writing bytecode of transformed class \"$transformedName\" to file"
            )
            ex.printStackTrace()
        }
    }

    /**
     * Logs a message ot the console at the specified level. This does not use the standard logger implementation because
     * this class is loaded before Minecraft has started.
     *
     * @param level the level to log the message to
     * @param message the message to log
     */
    fun log(level: Level?, message: String) {
        val name = "SkyblockAddons/" + javaClass.simpleName
        FMLRelaunchLog.log(name, level, (if (isDeobfuscated) "" else "[$name] ") + message)
    }

    companion object {
        @kotlin.jvm.JvmField
        val isDeobfuscated: Boolean = Launch.blackboard["fml.deobfuscatedEnvironment"] as Boolean
        private val usingNotchMappings = !isDeobfuscated
    }
}