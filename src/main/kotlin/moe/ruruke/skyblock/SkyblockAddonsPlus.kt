package moe.ruruke.skyblock

import cc.polyfrost.oneconfig.events.EventManager
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import moe.ruruke.skyblock.command.SkyblockAddonsPlusCommand
import moe.ruruke.skyblock.config.ConfigValues
import moe.ruruke.skyblock.config.NewConfigValue
import moe.ruruke.skyblock.config.PersistentValuesManager
import moe.ruruke.skyblock.core.OnlineData
import moe.ruruke.skyblock.core.SkillXpManager
import moe.ruruke.skyblock.features.EntityOutlines.EntityOutlineRenderer
import moe.ruruke.skyblock.features.EntityOutlines.FeatureItemOutlines
import moe.ruruke.skyblock.listeners.GuiScreenListener
import moe.ruruke.skyblock.listeners.PlayerListener
import moe.ruruke.skyblock.listeners.RenderListener
import moe.ruruke.skyblock.misc.SkyblockKeyBinding
import moe.ruruke.skyblock.misc.scheduler.NewScheduler
import moe.ruruke.skyblock.misc.scheduler.Scheduler
import moe.ruruke.skyblock.newgui.GuiManager
import moe.ruruke.skyblock.utils.InventoryUtils
import moe.ruruke.skyblock.utils.SkyblockAddonsMessageFactory
import moe.ruruke.skyblock.utils.Utils
import moe.ruruke.skyblock.utils.data.DataUtils
import moe.ruruke.skyblock.utils.gson.GsonInitializableTypeAdapter
import moe.ruruke.skyblock.utils.gson.PatternAdapter
import net.minecraft.client.settings.KeyBinding
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
        private val keyBindings: MutableList<SkyblockKeyBinding> = LinkedList()
        private var elapsedPartialTicks: Float = 0f
        fun getTimer(): Float {
            return elapsedPartialTicks;
        }
        private var playerListener: PlayerListener = PlayerListener()
        private var guiScreenListener: GuiScreenListener? = null
        @kotlin.jvm.JvmField
        var registeredFeatureIDs: MutableSet<Int> = HashSet()
        var configValues: ConfigValues? = null

        private val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapterFactory(GsonInitializableTypeAdapter())
            .registerTypeAdapter(Pattern::class.java, PatternAdapter())
            .create()
        var utils: Utils? = null;
        var instance: Companion = this
        private var onlineData: OnlineData? = null

        var config: NewConfigValue? = null

        @Mod.Instance(MODID)
        var INSTANCE: SkyblockAddonsPlus? = null // Adds the instance of the mod, so we can access other variables.
        private var usingLabymod = false
        private var usingOofModv1 = false
        private var usingPatcher = false
        fun isUsingLabyMod(): Boolean {
            return usingLabymod
        }
        fun isUsingOofModv1(): Boolean {
            return usingOofModv1
        }

        fun isUsingPatcher(): Boolean {
            return usingPatcher
        }

        var scheduler: Scheduler? = null
        var newScheduler: NewScheduler? = null
        var inventoryUtils: InventoryUtils? = null
        var skillXpManager: SkillXpManager? = null
        var guiManager: GuiManager? = null
        var renderListener: RenderListener? = null
        val THREAD_EXECUTOR: ThreadPoolExecutor = ThreadPoolExecutor(
            0, 1, 60L, TimeUnit.SECONDS,
            LinkedBlockingDeque(), ThreadFactoryBuilder().setNameFormat(NAME + " - #%d").build()
        )
        fun getNewConfigValue(): NewConfigValue {
            return config!!;
        }
        fun getInventoryUtil(): InventoryUtils {
            return inventoryUtils!!
        }
        fun getOnlineData(): OnlineData? {
            return onlineData;
        }
        fun getPlayerListener(): PlayerListener {
            return playerListener!!;
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

        var persistentValuesManager: PersistentValuesManager? = null
        fun runAsync(runnable: Runnable?) {
            THREAD_EXECUTOR.execute(runnable)
        }
        fun getDeveloperCopyNBTKey(): SkyblockKeyBinding {
            return keyBindings.get(6)
        }

        fun getOpenSettingsKey(): KeyBinding {
            return keyBindings.get(0).getKeyBinding()
        }

        fun getOpenEditLocationsKey(): KeyBinding {
            return keyBindings.get(1).getKeyBinding()
        }

        fun getLockSlotKey(): KeyBinding {
            return keyBindings.get(2).getKeyBinding()
        }

        fun getFreezeBackpackKey(): KeyBinding {
            return keyBindings.get(3).getKeyBinding()
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

//        MinecraftForge.EVENT_BUS.register(NetworkListener())
        MinecraftForge.EVENT_BUS.register(playerListener)
        MinecraftForge.EVENT_BUS.register(guiScreenListener)
        MinecraftForge.EVENT_BUS.register(renderListener)
        MinecraftForge.EVENT_BUS.register(scheduler)
        MinecraftForge.EVENT_BUS.register(newScheduler)
        MinecraftForge.EVENT_BUS.register(FeatureItemOutlines())
//        MinecraftForge.EVENT_BUS.register(FeatureDungeonTeammateOutlines())
        MinecraftForge.EVENT_BUS.register(EntityOutlineRenderer())
//        MinecraftForge.EVENT_BUS.register(FeatureTrackerQuest())
//        (Minecraft.getMinecraft().resourceManager as SimpleReloadableResourceManager).registerReloadListener(
//            resourceManagerReloadListener
//        )

        //TODO: バグってる。 
//        Collections.addAll(
//            keyBindings, SkyblockKeyBinding("open_settings", Keyboard.KEY_NONE, "settings.settings"),
//            SkyblockKeyBinding("edit_gui", Keyboard.KEY_NONE, "settings.editLocations"),
//            SkyblockKeyBinding("lock_slot", Keyboard.KEY_L, "settings.lockSlot"),
//            SkyblockKeyBinding("freeze_backpack", Keyboard.KEY_F, "settings.freezeBackpackPreview"),
//            SkyblockKeyBinding("increase_dungeon_map_zoom", Keyboard.KEY_EQUALS, "keyBindings.increaseDungeonMapZoom"),
//            SkyblockKeyBinding(
//                "decrease_dungeon_map_zoom",
//                Keyboard.KEY_SUBTRACT,
//                "keyBindings.decreaseDungeonMapZoom"
//            ),
////            SkyblockKeyBinding("copy_NBT", developerModeKey, "keyBindings.developerCopyNBT")
//        )
        usingLabymod = utils!!.isModLoaded("labymod");
        usingOofModv1 = utils!!.isModLoaded("refractionoof", "1.0");
        usingPatcher = utils!!.isModLoaded("patcher");
    }

//    @Subscribe
//    private fun onTick(event: TimerUpdateEvent) { // the parameter type specifies what event you are subscribing to
//        elapsedPartialTicks = event.timer.elapsedPartialTicks
//    }
    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        EventManager.INSTANCE.register(this);
        configValues = ConfigValues(e.suggestedConfigurationFile)
        persistentValuesManager = PersistentValuesManager(e.modConfigurationDirectory)
//        configValues!!.loadValues()
        DataUtils.readLocalAndFetchOnline()
//        persistentValuesManager.loadValues()
    }

    init {
        playerListener = PlayerListener()
        renderListener = RenderListener()
        guiScreenListener = GuiScreenListener()
        skillXpManager = SkillXpManager()
        inventoryUtils = InventoryUtils()
        utils = Utils()
        scheduler = Scheduler()
        newScheduler = NewScheduler()
//        dungeonManager = DungeonManager() TODO:
        guiManager = GuiManager()
        skillXpManager = SkillXpManager()
    }
}
