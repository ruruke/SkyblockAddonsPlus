package moe.ruruke.skyblock

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.events.event.TimerUpdateEvent
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import moe.ruruke.skyblock.command.SkyblockAddonsPlusCommand
import moe.ruruke.skyblock.config.ConfigValues
import moe.ruruke.skyblock.config.NewConfigValue
import moe.ruruke.skyblock.core.OnlineData
import moe.ruruke.skyblock.gui.listeners.PlayerListener
import moe.ruruke.skyblock.misc.scheduler.NewScheduler
import moe.ruruke.skyblock.misc.scheduler.Scheduler
import moe.ruruke.skyblock.utils.SkyblockAddonsMessageFactory
import moe.ruruke.skyblock.utils.Utils
import moe.ruruke.skyblock.utils.data.DataUtils
import moe.ruruke.skyblock.utils.gson.GsonInitializableTypeAdapter
import moe.ruruke.skyblock.utils.gson.PatternAdapter
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


/**
 * The entrypoint of the Example Mod that initializes it.
 *
 * @see Mod
 *
 * @see InitializationEvent
 */
@Mod(modid = SkyblockAddonsPlus.MODID, name = SkyblockAddonsPlus.NAME,clientSideOnly = true,  version = SkyblockAddonsPlus.VERSION)
class SkyblockAddonsPlus() {

    companion object {
        // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
        const val MODID: String = "skyblockaddonsplus"
        const val NAME: String = "SkyblockAddonsPlus"
        const val VERSION: String = "1.0.0"
        private var elapsedPartialTicks: Float = 0f
        fun getTimer(): Float {
            return elapsedPartialTicks;
        }
        private var playerListener: PlayerListener? = null
        @kotlin.jvm.JvmField
        var registeredFeatureIDs: MutableSet<Int> = HashSet()
        var configValues: ConfigValues? = null
        private val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(GsonInitializableTypeAdapter())
            .registerTypeAdapter(Pattern::class.java, PatternAdapter())
            .create()
        var utils: Utils? = null;
//        private val inventoryUtils: InventoryUtils? = null
        var instance: Companion = this
        private var onlineData: OnlineData? = null

        var config: NewConfigValue? = null

        @Mod.Instance(MODID)
        var INSTANCE: SkyblockAddonsPlus? = null // Adds the instance of the mod, so we can access other variables.


        var scheduler: Scheduler? = null
        var newScheduler: NewScheduler? = null
        val THREAD_EXECUTOR: ThreadPoolExecutor = ThreadPoolExecutor(
            0, 1, 60L, TimeUnit.SECONDS,
            LinkedBlockingDeque(), ThreadFactoryBuilder().setNameFormat(NAME + " - #%d").build()
        )
        fun getNewConfigValue(): NewConfigValue {
            return config!!;
        }
        fun getOnlineData(): OnlineData? {
            return onlineData;
        }
        fun setOnlineData(_onlineData: OnlineData) {
            onlineData =  _onlineData
        }
        fun getLogger(): Logger {
            val fullClassName = Throwable().stackTrace[1].className
            val simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1)
            return LogManager.getLogger(fullClassName, SkyblockAddonsMessageFactory(simpleClassName))
        }
        fun getGson(): Gson {
            return GSON
        }


        fun runAsync(runnable: Runnable?) {
            THREAD_EXECUTOR.execute(runnable)
        }

    }
    // Register the config and commands.
    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent?) {

        config = NewConfigValue()
        ClientCommandHandler.instance.registerCommand(SkyblockAddonsPlusCommand())
        if (DataUtils.USE_ONLINE_DATA) {
            DataUtils.loadOnlineData();
        }
        MinecraftForge.EVENT_BUS.register(playerListener);

//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
    }

    @Subscribe
    private fun onTick(event: TimerUpdateEvent) { // the parameter type specifies what event you are subscribing to
        elapsedPartialTicks = event.timer.elapsedPartialTicks
    }
    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        EventManager.INSTANCE.register(this);
        configValues = ConfigValues(e.suggestedConfigurationFile)
//        persistentValuesManager = PersistentValuesManager(e.modConfigurationDirectory)
//        configValues!!.loadValues()
//        DataUtils.readLocalAndFetchOnline()
//        persistentValuesManager.loadValues()
    }

    init {
        playerListener = PlayerListener()
        scheduler = Scheduler()
        newScheduler = NewScheduler()
        utils = Utils()
    }
}
