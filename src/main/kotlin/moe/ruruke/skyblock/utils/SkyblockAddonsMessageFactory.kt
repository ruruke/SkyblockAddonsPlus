package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.SkyblockAddonsPlus
import org.apache.logging.log4j.message.*

/**
 * This is a simple `MessageFactory` implementation that adds the mod name and logger display name in square
 * brackets to the beginning of each log event message. This is required since the Minecraft Log4J config doesn't
 * include logger names when writing logs to file.
 */
class SkyblockAddonsMessageFactory
/**
 * Creates a new instance of `SkyblockAddonsMessageFactory` that uses the given logger display name.
 * The display name should be the simple class name of the class that is creating the logger.
 * The logger display name will be shown as a `[SkyblockAddons/loggerDisplayName]` prefix added to the
 * beginning of all logger messages.
 *
 * @param loggerDisplayName the name of the logger
 */(private val LOGGER_DISPLAY_NAME: String) : AbstractMessageFactory() {
    override fun newMessage(message: Any): Message {
        return getFormattedMessage(ObjectMessage(message))
    }

    override fun newMessage(message: String): Message {
        return getFormattedMessage(SimpleMessage(message))
    }

    override fun newMessage(message: String, vararg params: Any): Message {
        return getFormattedMessage(ParameterizedMessage(message, params))
    }

    private fun getFormattedMessage(message: Message): FormattedMessage {
        return if (LOGGER_DISPLAY_NAME != SkyblockAddonsPlus.NAME) {
            FormattedMessage(
                MESSAGE_FORMAT,
                "/$LOGGER_DISPLAY_NAME", message.formattedMessage
            )
        } else {
            FormattedMessage(
                MESSAGE_FORMAT,
                null,
                message.formattedMessage
            )
        }
    }

    companion object {
        private const val MESSAGE_FORMAT = "[" + SkyblockAddonsPlus.NAME + "%s] %s"
    }
}
