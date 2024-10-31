package moe.ruruke.skyblock.utils.data


import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.newScheduler
import moe.ruruke.skyblock.core.Language
import moe.ruruke.skyblock.core.OnlineData
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.core.enchantedItemBlacklist.EnchantedItemLists
import moe.ruruke.skyblock.core.enchantedItemBlacklist.EnchantedItemPlacementBlocker
import moe.ruruke.skyblock.core.seacreatures.SeaCreature
import moe.ruruke.skyblock.core.seacreatures.SeaCreatureManager
import moe.ruruke.skyblock.exceptions.DataLoadingException
import moe.ruruke.skyblock.features.cooldowns.CooldownManager
import moe.ruruke.skyblock.features.enchants.EnchantManager
import moe.ruruke.skyblock.misc.scheduler.ScheduledTask
import moe.ruruke.skyblock.misc.scheduler.SkyblockRunnable
import moe.ruruke.skyblock.tweaker.SkyblockAddonsTransformer
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.Utils
import moe.ruruke.skyblock.utils.skyblockdata.CompactorItem
import moe.ruruke.skyblock.utils.skyblockdata.ContainerData
import net.minecraft.crash.CrashReport
import net.minecraft.event.ClickEvent
import net.minecraft.util.*
import net.minecraftforge.fml.client.FMLClientHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.NoConnectionReuseStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.FutureRequestExecutionService
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpRequestFutureTask
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory


/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
object DataUtils {
    private val gson: Gson = SkyblockAddonsPlus.getGson()

    private val logger: Logger = SkyblockAddonsPlus.getLogger()

    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private val requestConfig: RequestConfig = RequestConfig.custom()
        .setConnectTimeout(120 * 1000)
        .setConnectionRequestTimeout(120 * 1000)
        .setSocketTimeout(30 * 1000).build()

    private val connectionManager = PoolingHttpClientConnectionManager()

    private val httpClient: CloseableHttpClient = HttpClientBuilder.create()
        .setUserAgent(Utils.USER_AGENT)
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(connectionManager)
        .setConnectionReuseStrategy(NoConnectionReuseStrategy())
        .setRetryHandler(RequestRetryHandler()).build()

    private val threadFactory: ThreadFactory = ThreadFactoryBuilder().setNameFormat("SBA DataUtils Thread %d")
        .setUncaughtExceptionHandler(UncaughtFetchExceptionHandler()).build()

    private val executorService: ExecutorService = Executors.newCachedThreadPool(
        threadFactory
    )

    private val futureRequestExecutionService = FutureRequestExecutionService(httpClient, executorService)

    private val remoteRequests = ArrayList<RemoteFileRequest<*>>()

    private val httpRequestFutureTasks = ArrayList<HttpRequestFutureTask<*>>()
    fun getHttpRequestFutureTasks(): ArrayList<HttpRequestFutureTask<*>> {
        return httpRequestFutureTasks
    }

    private val failedRequests = HashMap<RemoteFileRequest<*>?, Throwable?>()
    fun getFailedRequests(): HashMap<RemoteFileRequest<*>?, Throwable?> {
        return failedRequests
    }

    /**
     * Main CDN doesn't work for some users.
     * Use fallback CDN if a request fails twice or user is in China or Hong Kong.
     */
    var useFallbackCDN: Boolean = false

    // Whether the failed requests error was shown in chat, used to make it show only once per session
    private var failureMessageShown = false

    /**
     * The mod uses the online data files if this is `true` and local data if this is `false`.
     * This is set to `true` if the mod is running in production or if it's running in a dev environment that has
     * the environment variable `FETCH_DATA_ONLINE`.
     */
    val USE_ONLINE_DATA: Boolean = !SkyblockAddonsTransformer.isDeobfuscated() ||
            System.getenv().containsKey("FETCH_DATA_ONLINE")

    private var path: String? = null

//    private var localizedStringsRequest: LocalizedStringsRequest? = null

    private var languageLoadingTask: ScheduledTask? = null

    init {
        val country = Locale.getDefault().country
        if (country == "CN" || country == "HK") {
            useFallbackCDN = true
        }
        connectionManager.maxTotal = 5
        connectionManager.defaultMaxPerRoute = 5
        // Disable online fetching due to EOL
        // registerRemoteRequests();
    }

    //TODO: Migrate all data file loading to this class
    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used, unless the environment variable "FETCH_DATA_ONLINE" is present.
     */
    fun readLocalAndFetchOnline() {
        readLocalFileData()

        if (USE_ONLINE_DATA) {
            fetchFromOnline()
        } else {
//            SkyblockAddons.getInstance().getUpdater().checkForUpdate()
        }
    }

    /**
     * Reads local json files before pulling from online
     */
    fun readLocalFileData() {
        // Online Data

        path = "/data.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    main.setOnlineData(
                        gson.fromJson(
                            inputStreamReader,
                            OnlineData::class.java
                        )
                    )
                }
            }
        } catch (ex: java.lang.Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Localized Strings
        loadLocalizedStrings(false)

        // Enchanted Item Blacklist
        path = "/enchantedItemLists.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    EnchantedItemPlacementBlocker.setItemLists(
                        gson.fromJson(
                            inputStreamReader,
                            EnchantedItemLists::class.java
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Containers
        path = "/containers.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    ItemUtils.setContainers(
                        gson.fromJson(
                            inputStreamReader,
                            object : TypeToken<HashMap<String?, ContainerData?>?>() {}.type
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Compactor Items
        path = "/compactorItems.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    ItemUtils.setCompactorItems(
                        gson.fromJson(
                            inputStreamReader,
                            object : TypeToken<HashMap<String?, CompactorItem?>?>() {}.type
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Sea Creatures
        path = "/seaCreatures.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    SeaCreatureManager.instance.setSeaCreatures(
                        gson.fromJson(inputStreamReader, object : TypeToken<Map<String?, SeaCreature?>?>() {}.type)
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Enchantment data
        path = "/enchants.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    EnchantManager.setEnchants(
                        gson.fromJson(
                            inputStreamReader,
                            object : TypeToken<EnchantManager.Enchants?>() {}.type
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Cooldown Data
        path = "/cooldowns.json"
        try {
            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    CooldownManager.setItemCooldowns(
                        gson.fromJson(
                            inputStreamReader,
                            object : TypeToken<HashMap<String?, Int?>?>() {
                            }.type
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        // Skill xp Data
        //TODO:
//        path = "/skillXp.json"
//        try {
//            DataUtils::class.java.getResourceAsStream(path).use { inputStream ->
//                InputStreamReader(
//                    Objects.requireNonNull<InputStream>(inputStream),
//                    StandardCharsets.UTF_8
//                ).use { inputStreamReader ->
//                    main.getSkillXpManager().initialize(
//                        gson.fromJson(
//                            inputStreamReader,
//                            SkillXpManager.JsonInput::class.java
//                        )
//                    )
//                }
//            }
//        } catch (ex: Exception) {
//            handleLocalFileReadException(path, ex)
//        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private fun fetchFromOnline() {
        for (request in remoteRequests) {
            request.execute(futureRequestExecutionService)
        }

        if (useFallbackCDN) {
            logger.warn("Could not reach main CDN. Some resources were fetched from fallback CDN.")
        }
    }

    /**
     * Loads the received online data files into the mod.
     *
     * @see SkyblockAddons.preInit
     */
    fun loadOnlineData() {
        val requestIterator = remoteRequests.iterator()

        while (requestIterator.hasNext()) {
            val request = requestIterator.next()

            if (!request.isDone()) {
                handleOnlineFileLoadException(
                    request,
                    RuntimeException(
                        String.format(
                            "Request for \"%s\" didn't finish in time for mod init.",
                            getFileNameFromUrlString(request.uRL)
                        )
                    )
                )
            }

            try {
                loadOnlineFile(request)
                requestIterator.remove()
            } catch (e: InterruptedException) {
                handleOnlineFileLoadException(Objects.requireNonNull(request), e)
            } catch (e: ExecutionException) {
                handleOnlineFileLoadException(Objects.requireNonNull(request), e)
            } catch (e: NullPointerException) {
                handleOnlineFileLoadException(Objects.requireNonNull(request), e)
            } catch (e: IllegalArgumentException) {
                handleOnlineFileLoadException(Objects.requireNonNull(request), e)
            }
        }
    }

    /**
     * Loads a received online data file into the mod.
     *
     * @param request the `RemoteFileRequest` for the file
     */
    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadOnlineFile(request: RemoteFileRequest<*>?) {
        request!!.load()
    }

    /**
     * Loads the localized strings for the current [Language] set in the mod settings with the choice of loading
     * only local strings or local and online strings.
     *
     * @param loadOnlineStrings Loads local and online strings if `true`, loads only local strings if `false`
     */
    @JvmStatic
    fun loadLocalizedStrings(loadOnlineStrings: Boolean) {
        loadLocalizedStrings(main.configValues!!.getLanguage(), loadOnlineStrings)
    }

    /**
     * Loads the localized strings for the given [Language] with the choice of loading only local strings or local
     * and online strings. Languages are handled separately from other files because they may need to be loaded multiple
     * times in-game instead of just on startup. Online strings will never be loaded for English, regardless of the value
     * of `loadOnlineStrings`.
     *
     * @param language the `Language` to load strings for
     * @param loadOnlineStrings Loads local and online strings if `true`, loads only local strings if `false`,
     * does not override [DataUtils.USE_ONLINE_DATA]
     */
    fun loadLocalizedStrings(language: Language, loadOnlineStrings: Boolean) {
        // logger.info("Loading localized strings for " + language.name() + "...");

        path = ("lang/" + language.getPath()).toString() + ".json"
        try {
            DataUtils::class.java.classLoader.getResourceAsStream(path).use { inputStream ->
                InputStreamReader(
                    Objects.requireNonNull<InputStream>(inputStream),
                    StandardCharsets.UTF_8
                ).use { inputStreamReader ->
                    main.configValues!!.setLanguageConfig(
                        gson.fromJson(
                            inputStreamReader,
                            JsonObject::class.java
                        )
                    )
                    main.configValues!!.setLanguage(language)
                }
            }
        } catch (ex: Exception) {
            handleLocalFileReadException(path, ex)
        }

        if (USE_ONLINE_DATA && loadOnlineStrings && language !== Language.ENGLISH) {
            //TODO:
//            if (localizedStringsRequest != null) {
//                val futureTask: HttpRequestFutureTask<JsonObject?>? = localizedStringsRequest!!.getFutureTask()
//                if (!futureTask!!.isDone) {
//                    futureTask.cancel(false)
//                }
//            } else if (languageLoadingTask != null) {
//                languageLoadingTask!!.cancel()
//            }
//
//            localizedStringsRequest = LocalizedStringsRequest(language)
//            localizedStringsRequest!!.execute(futureRequestExecutionService)
//            languageLoadingTask = newScheduler!!.scheduleLimitedRepeatingTask(object : SkyblockRunnable() {
//                override fun run() {
//                    if (localizedStringsRequest != null) {
//                        if (localizedStringsRequest!!.isDone()) {
//                            try {
//                                loadOnlineFile(localizedStringsRequest)
//                            } catch (e: InterruptedException) {
//                                handleOnlineFileLoadException(Objects.requireNonNull(localizedStringsRequest), e)
//                            } catch (e: ExecutionException) {
//                                handleOnlineFileLoadException(Objects.requireNonNull(localizedStringsRequest), e)
//                            } catch (e: java.lang.NullPointerException) {
//                                handleOnlineFileLoadException(Objects.requireNonNull(localizedStringsRequest), e)
//                            } catch (e: java.lang.IllegalArgumentException) {
//                                handleOnlineFileLoadException(Objects.requireNonNull(localizedStringsRequest), e)
//                            }
//                            cancel()
//                        }
//                    } else {
//                        cancel()
//                    }
//                }
//            }, 10, 20, 8)
        }

        // logger.info("Finished loading localized strings.");
    }

    // TODO: Shut it down and restart it as needed?
    /**
     * Shuts down [DataUtils.futureRequestExecutionService] and the underlying `ExecutorService` and
     * `ClosableHttpClient`.
     */
    fun shutdownExecutorService() {
        try {
            futureRequestExecutionService.close()
            logger.debug("Executor service shut down.")
        } catch (e: IOException) {
            logger.error("Failed to shut down executor service.", e)
        }
    }

    /**
     * Displays a message when the player first joins Skyblock asking them to report failed requests to our Discord server.
     */
    fun onSkyblockJoined() {
        if (!failureMessageShown && !failedRequests.isEmpty()) {
            val errorMessageBuilder = StringBuilder("Failed Requests:\n")

            for ((key, value) in failedRequests) {
                errorMessageBuilder.append(key!!.uRL).append("\n")
                errorMessageBuilder.append(value.toString()).append("\n")
            }

            val failureMessageComponent = ChatComponentText(
                Translations.getMessage(
                    "messages.fileFetchFailed", ("${EnumChatFormatting.AQUA}${SkyblockAddonsPlus.NAME}${EnumChatFormatting.RED}"),
                    failedRequests.size
                )
            )
            val buttonRowComponent = ChatComponentText(
                ("[" +
                        Translations.getMessage("messages.copy")).toString() + "]"
            ).setChatStyle(
                ChatStyle().setColor(EnumChatFormatting.WHITE).setBold(true).setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND, String.format(
                            "/sba internal copy %s",
                            errorMessageBuilder
                        )
                    )
                )
            )
            buttonRowComponent.appendText("  ")
            buttonRowComponent.appendSibling(
                ChatComponentText("[Discord]").setChatStyle(
                    ChatStyle()
                        .setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek"))
                )
            )
            failureMessageComponent.appendText("\n").appendSibling(buttonRowComponent)

            main.utils!!.sendMessage(failureMessageComponent, false)
            failureMessageShown = true
        }
    }

    /**
     * Returns the file name from the end of a given URL string.
     * This does not check if the URL has a valid file name at the end.
     *
     * @param url the URL string to get the file name from
     * @return the file name from the end of the URL string
     */
    fun getFileNameFromUrlString(url: String?): String {
        val fileNameIndex = url!!.lastIndexOf('/') + 1
        val queryParamIndex = url.indexOf('?', fileNameIndex)
        return url.substring(fileNameIndex, if (queryParamIndex > fileNameIndex) queryParamIndex else url.length)
    }

    private fun registerRemoteRequests() {

        //TODO:
//        remoteRequests.add(OnlineDataRequest())
//        /*        if (SkyblockAddons.getInstance().configValues!!.getLanguage() != Language.ENGLISH) {
//            remoteRequests.add(new LocalizedStringsRequest(SkyblockAddons.getInstance().configValues!!.getLanguage()));
//        }*/
//        remoteRequests.add(EnchantedItemListsRequest())
//        remoteRequests.add(ContainersRequest())
//        remoteRequests.add(CompactorItemsRequest())
//        remoteRequests.add(SeaCreaturesRequest())
//        remoteRequests.add(EnchantmentsRequest())
//        remoteRequests.add(CooldownsRequest())
//        remoteRequests.add(SkillXpRequest())
    }

    /**
     * This method handles errors that can occur when reading the local configuration files.
     * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
     * `Throwable` in the console.
     * If the game is initialized, it crashes the game with a crash report containing the file path and the stacktrace
     * of the given `Throwable`.
     *
     * @param filePath the path to the file that caused the exception
     * @param exception the exception that occurred
     */
    private fun handleLocalFileReadException(filePath: String?, exception: Throwable) {
        if (FMLClientHandler.instance().isLoading) {
            throw DataLoadingException(filePath, exception)
        } else {
            val crashReport = CrashReport.makeCrashReport(
                exception, String.format(
                    "Loading data file at %s",
                    filePath
                )
            )
            throw ReportedException(crashReport)
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
    private fun handleOnlineFileLoadException(request: RemoteFileRequest<*>?, exception: Throwable?) {
        val url = request!!.uRL
        val fileName = getFileNameFromUrlString(url)
        failedRequests[request] = exception

        // The loader encountered a file name it didn't expect.
        if (exception is IllegalArgumentException) {
            logger.error(exception.message)
            return
        }

        if (request.isEssential) {
            if (FMLClientHandler.instance().isLoading) {
                throw DataLoadingException(url, exception)
            } else {
                // Don't include URL because Fire strips URLs.
                val crashReport = CrashReport.makeCrashReport(
                    exception, String.format(
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
}
