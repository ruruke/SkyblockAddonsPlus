package moe.ruruke.skyblock.utils.data

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * A simple [Thread.UncaughtExceptionHandler] that prints the thread name,
 * exception message, and list of incomplete fetch requests when a data fetching thread in
 * [DataUtils] throws an uncaught exception.
 */
class UncaughtFetchExceptionHandler : Thread.UncaughtExceptionHandler {
    private val logger: Logger = LogManager.getLogger()

    override fun uncaughtException(t: Thread, e: Throwable) {
        logger.error("Exception in thread \"{}\"", t.name)
        logger.error(e.message)
    }
}
