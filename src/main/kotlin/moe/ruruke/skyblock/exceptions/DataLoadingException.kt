package moe.ruruke.skyblock.exceptions

import net.minecraft.util.EnumChatFormatting

/**
 * This exception is thrown when the mod fails to load a necessary data file during startup.
 */
class DataLoadingException(filePathString: String?, cause: Throwable?) :
    LoadingException(String.format(ERROR_MESSAGE_FORMAT, filePathString), cause, true) {
    companion object {
        private val ERROR_MESSAGE_FORMAT = """
            Failed to load file at
            ${EnumChatFormatting.DARK_RED}%s
            """.trimIndent()
    }
}