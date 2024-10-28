package moe.ruruke.skyblock

import moe.ruruke.skyblock.command.SkyblockAddonsPlusCommand
import moe.ruruke.skyblock.config.TestConfig
import moe.ruruke.skyblock.utils.Utils
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent


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

        var utils: Utils? = null;
        var instance: SkyblockAddonsPlus? = null

        @Mod.Instance(MODID)
        var INSTANCE: SkyblockAddonsPlus? = null // Adds the instance of the mod, so we can access other variables.
        var config: TestConfig? = null
    }
    // Register the config and commands.
    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent?) {
        config = TestConfig()
        ClientCommandHandler.instance.registerCommand(SkyblockAddonsPlusCommand())
//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
//        CommandManager.INSTANCE.registerCommand(ExampleCommand())
    }

    init {
        instance = this
        utils = Utils()
    }
}
