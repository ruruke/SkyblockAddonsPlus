package moe.ruruke.skyblock

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import moe.ruruke.skyblock.command.SkyblockAddonsPlusCommand
import moe.ruruke.skyblock.config.ConfigValues
import moe.ruruke.skyblock.config.TestConfig
import moe.ruruke.skyblock.core.OnlineData
import moe.ruruke.skyblock.utils.SkyblockAddonsMessageFactory
import moe.ruruke.skyblock.utils.Utils
import moe.ruruke.skyblock.utils.data.DataUtils
import moe.ruruke.skyblock.utils.gson.GsonInitializableTypeAdapter
import moe.ruruke.skyblock.utils.gson.PatternAdapter
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.regex.Pattern


/**
 * The entrypoint of the Example Mod that initializes it.
 *
 * @see Mod
 *
 * @see InitializationEvent
 */
@Mod(modid = SkyblockAddonsPlus.MODID, name = SkyblockAddonsPlus.NAME, version = SkyblockAddonsPlus.VERSION)
class SkyblockAddonsPlus() {

    companion object {
        // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
        const val MODID: String = "skyblockaddonsplus"
        const val NAME: String = "SkyblockAddonsPlus"
        const val VERSION: String = "1.0.0"

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
        var instance: SkyblockAddonsPlus? = null
        var onlineData: OnlineData? = null

        @Mod.Instance(MODID)
        var INSTANCE: SkyblockAddonsPlus? = null // Adds the instance of the mod, so we can access other variables.
        var config: TestConfig? = null

        fun getLogger(): Logger {
            val fullClassName = Throwable().stackTrace[1].className
            val simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1)

            return LogManager.getLogger(fullClassName, SkyblockAddonsMessageFactory(simpleClassName))
        }
        fun getGson(): Gson {
            return GSON
        }

    }
    // Register the config and commands.
    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent?) {
        config = TestConfig()
        ClientCommandHandler.instance.registerCommand(SkyblockAddonsPlusCommand())
        if (DataUtils.USE_ONLINE_DATA) {
            DataUtils.loadOnlineData();
        }

//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
    }

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        configValues = ConfigValues(e.suggestedConfigurationFile)
//        persistentValuesManager = PersistentValuesManager(e.modConfigurationDirectory)
        configValues!!.loadValues()
//        DataUtils.readLocalAndFetchOnline()
//        persistentValuesManager.loadValues()
    }
    init {
        instance = this
        utils = Utils()
    }
}
