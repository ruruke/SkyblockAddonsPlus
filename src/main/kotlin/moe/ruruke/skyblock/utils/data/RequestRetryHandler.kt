package moe.ruruke.skyblock.utils.data

import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.protocol.HttpContext
import java.io.IOException

/**
 * This is a basic `HttpRequestRetryHandler` implementation that allows each request to be retried twice after the
 * first failure.
 */
class RequestRetryHandler : HttpRequestRetryHandler {
    override fun retryRequest(exception: IOException, executionCount: Int, context: HttpContext): Boolean {
        if (executionCount >= MAX_RETRY_COUNT) {
            DataUtils.useFallbackCDN = true
        }

        return executionCount <= MAX_RETRY_COUNT
    }

    companion object {
        private const val MAX_RETRY_COUNT = 2
    }
}
