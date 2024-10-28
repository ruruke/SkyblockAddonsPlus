package moe.ruruke.skyblock

import cc.polyfrost.oneconfig.utils.commands.CommandManager
import moe.ruruke.skyblock.command.ExampleCommand
import moe.ruruke.skyblock.config.TestConfig
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
    // Register the config and commands.
    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent?) {
        config = TestConfig()
        CommandManager.INSTANCE.registerCommand(ExampleCommand())
    }

    init {
        instance = this
    }

    companion object {
        // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
        const val MODID: String = "skyblockaddonsplus"
        const val NAME: String = "SkyblockAddonsPlus"
        const val VERSION: String = "1.0.0"
        var instance: SkyblockAddonsPlus? = null
        @Mod.Instance(MODID)
        var INSTANCE: SkyblockAddonsPlus? = null // Adds the instance of the mod, so we can access other variables.
        var config: TestConfig? = null
    }
}
