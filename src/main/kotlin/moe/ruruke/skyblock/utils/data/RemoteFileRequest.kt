package moe.ruruke.skyblock.utils.data

import cc.polyfrost.oneconfig.libs.checker.nullness.qual.NonNull
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.exceptions.LoadingException
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.client.FutureRequestExecutionService
import org.apache.http.impl.client.HttpRequestFutureTask
import java.net.URI
import java.util.concurrent.ExecutionException


open class RemoteFileRequest<T>(
    requestPath: @NonNull String?, responseHandler: @NonNull ResponseHandler<T>?,
    essential: Boolean, usingCustomUrl: Boolean
) {
    val uRL: String?
    private val RESPONSE_HANDLER: ResponseHandler<T>?
    private val FETCH_CALLBACK: FutureCallback<T>
    val isEssential: Boolean
    fun isDone(): Boolean {
        return futureTask!!.isDone
    }

    private var futureTask: HttpRequestFutureTask<T>?

    fun getFutureTask(): HttpRequestFutureTask<T>? {
        return futureTask
    }

    constructor(requestPath: @NonNull String?, responseHandler: @NonNull ResponseHandler<T>?) : this(
        requestPath,
        responseHandler,
        false
    )

    constructor(
        requestPath: @NonNull String?, responseHandler: @NonNull ResponseHandler<T>?,
        essential: Boolean
    ) : this(requestPath, responseHandler, essential, false)

    init {
        uRL = if (usingCustomUrl) requestPath else versionedCDNBaseURL + requestPath
        RESPONSE_HANDLER = responseHandler
        FETCH_CALLBACK = DataFetchCallback(URI.create(uRL))
        isEssential = essential
        futureTask = null
    }

    fun execute(executionService: @NonNull FutureRequestExecutionService?) {
        futureTask = executionService!!.execute(HttpGet(uRL), null, RESPONSE_HANDLER, FETCH_CALLBACK)
    }

    @Throws(InterruptedException::class, ExecutionException::class, RuntimeException::class)
    open fun load() {
        throw LoadingException(
            String.format(
                "Loading method not implemented for file %s",
                uRL!!.substring(uRL.lastIndexOf(('/'.code + 1).toChar()))
            ), RuntimeException()
        )
    }

    @get:Throws(
        InterruptedException::class,
        ExecutionException::class,
        RuntimeException::class
    )
    protected val result: T
        get() = futureTask!!.get()



    companion object {
        protected const val NO_DATA_RECEIVED_ERROR: String = "No data received for get request to \"%s\""
        private val versionedCDNBaseURL: String
            get() = java.lang.String.format(
                if (DataUtils.useFallbackCDN) DataConstants.FALLBACK_CDN_BASE_URL else DataConstants.CDN_BASE_URL,
                SkyblockAddonsPlus.VERSION.substring(0, StringUtils.ordinalIndexOf(SkyblockAddonsPlus.VERSION, ".", 2))
            )
    }
}