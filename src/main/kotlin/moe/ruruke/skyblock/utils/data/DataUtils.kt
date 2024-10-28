package moe.ruruke.skyblock.utils.data

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.exceptions.DataLoadingException
import moe.ruruke.skyblock.tweaker.SkyblockAddonsTransformer
import net.minecraft.crash.CrashReport
import net.minecraft.util.ReportedException
import net.minecraftforge.fml.client.FMLClientHandler
import org.apache.logging.log4j.Logger
import java.lang.String
import java.util.concurrent.ExecutionException


class DataUtils {


    companion object{

        @kotlin.jvm.JvmField
        var useFallbackCDN: Boolean = false
        private val failedRequests: HashMap<RemoteFileRequest<*>, Throwable> = HashMap()
        private val logger: Logger = SkyblockAddonsPlus.getLogger()
        private val remoteRequests: ArrayList<RemoteFileRequest<*>> = ArrayList()

        @kotlin.jvm.JvmField
        val USE_ONLINE_DATA: Boolean = !SkyblockAddonsTransformer.isDeobfuscated || System.getenv().containsKey("FETCH_DATA_ONLINE")

        /**
         * Loads the received online data files into the mod.
         *
         * @see SkyblockAddons.preInit
         */
        fun loadOnlineData() {
            val requestIterator: MutableIterator<RemoteFileRequest<*>> = remoteRequests.iterator()

            while (requestIterator.hasNext()) {
                val request: RemoteFileRequest<*> = requestIterator.next()

                if (!request.isDone) {
                    handleOnlineFileLoadException(
                        request,
                        RuntimeException(
                            String.format(
                                "Request for \"%s\" didn't finish in time for mod init.",
                                getFileNameFromUrlString(request.uRL.toString())
                            )
                        )
                    )
                }

                try {
                    loadOnlineFile(request)
                    requestIterator.remove()
                } catch (e: InterruptedException) {
                    handleOnlineFileLoadException(request, e)
                } catch (e: ExecutionException) {
                    handleOnlineFileLoadException(request, e)
                } catch (e: NullPointerException) {
                    handleOnlineFileLoadException(request, e)
                } catch (e: IllegalArgumentException) {
                    handleOnlineFileLoadException(request, e)
                }
            }
        }



        /**
         * This method handles errors that can occur when reading the online configuration files.
         * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
         * `Throwable` in the console.
         * If the game is initialized, it crashes the game with a crash report containing the file name and the stacktrace
         * of the given `Throwable`.
         *
         * @param request the `RemoteFileRequest` for the file that failed to load
         * @param exception the exception that occurred
         */
        public fun handleOnlineFileLoadException(request: RemoteFileRequest<*>, exception: Throwable?) {
            val url = request.uRL
            val fileName: kotlin.String = getFileNameFromUrlString(url.toString())
            if (exception != null) {
                failedRequests.put(request, exception)
            }

            // The loader encountered a file name it didn't expect.
            if (exception is java.lang.IllegalArgumentException) {
                logger.error(exception.message)
                return
            }

            if (request.isEssential) {
                if (FMLClientHandler.instance().isLoading) {
                    throw DataLoadingException(url, exception)
                } else {
                    // Don't include URL because Fire strips URLs.
                    val crashReport = CrashReport.makeCrashReport(
                        exception, kotlin.String.format(
                            "Loading online data file" +
                                    " at %s",
                            fileName
                        )
                    )
                    throw ReportedException(crashReport)
                }
            } else {
                logger.error("Failed to load \"{}\" from the server. The local copy will be used instead.", fileName)
                if (exception != null) {
                    logger.error(exception.message)
                }
            }
        }


        /**
         * Loads a received online data file into the mod.
         *
         * @param request the `RemoteFileRequest` for the file
         */
        @Throws(ExecutionException::class, InterruptedException::class)
        fun loadOnlineFile(request: RemoteFileRequest<*>) {
            request.load()
        }


        /**
         * Returns the file name from the end of a given URL string.
         * This does not check if the URL has a valid file name at the end.
         *
         * @param url the URL string to get the file name from
         * @return the file name from the end of the URL string
         */
        fun getFileNameFromUrlString(url: kotlin.String): kotlin.String {
            val fileNameIndex = url.lastIndexOf('/') + 1
            val queryParamIndex = url.indexOf('?', fileNameIndex)
            return url.substring(fileNameIndex, if (queryParamIndex > fileNameIndex) queryParamIndex else url.length)
        }
    }
}