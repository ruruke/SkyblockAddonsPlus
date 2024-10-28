package moe.ruruke.skyblock.utils.data

import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getGson
import org.apache.http.HttpResponse
import org.apache.http.client.HttpResponseException
import org.apache.http.client.ResponseHandler
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * This is a [ResponseHandler] that returns an object deserialized to the given type from a JSON response if the
 * response was successful (code 200). If the response has any other error code, [HttpResponseException] is thrown.
 *
 * @param <T> the type to deserialize the JSON to
</T> */
class JSONResponseHandler<T>
/**
 * Creates a new `JSONResponseHandler` with the [Type] to deserialize the response to.
 *
 * @param type the `Type` to deserialize the response to
 */(private val type: Type) : ResponseHandler<T?> {
    @Throws(IOException::class)
    override fun handleResponse(response: HttpResponse): T? {
        val status = response.statusLine.statusCode
        val entity = response.entity

        if (status == 200) {
            return if (entity != null) {
                gson.fromJson(
                    EntityUtils.toString(entity, StandardCharsets.UTF_8),
                    type
                )
            } else {
                null
            }
        } else {
            EntityUtils.consume(entity)
            throw HttpResponseException(status, "Unexpected response status: $status")
        }
    }

    companion object {
        private val gson = getGson()
    }
}
