package moe.ruruke.skyblock.utils.data

import org.apache.http.concurrent.FutureCallback
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI

/**
 * This is a simple [FutureCallback] to log the result of a request for debugging.
 *
 * @param <T> the type of the result, unused
</T> */
class DataFetchCallback<T>(url: URI) : FutureCallback<T> {
    private val LOGGER: Logger = LogManager.getLogger()
    private val URL_STRING = url.toString()

    override fun completed(result: T) {
        LOGGER.debug("Successfully fetched {}", URL_STRING)
    }

    override fun failed(ex: Exception) {
        LOGGER.error("Failed to fetch {}", URL_STRING)
        LOGGER.error(ex.message)
    }

    override fun cancelled() {
        LOGGER.debug("Cancelled fetching {}", URL_STRING)
    }
}
