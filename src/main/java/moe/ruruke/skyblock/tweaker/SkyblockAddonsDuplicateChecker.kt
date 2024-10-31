package moe.ruruke.skyblock.tweaker

import net.minecraftforge.fml.relauncher.FMLRelaunchLog
import net.minecraftforge.fml.relauncher.IFMLCallHook
import org.apache.logging.log4j.Level

/**
 * This class contains a series of checks that need to be run before the SkyblockAddons transformers are used.
 * This check only works with mod version 1.6.0+ since it only works with versions with this check implemented.
 * In earlier versions, their coremod loads first and gets called twice for reasons I don't really understand.
 * This check will never run in that circumstance.
 */
class SkyblockAddonsDuplicateChecker : IFMLCallHook {
    /**
     * Checks that need to run after the transformers are initialized but before the transformers are used
     */
    override fun call(): Void? {
        logDebug("Searching for duplicate SkyblockAddons installations...")

        try {
            val coremodList: List<Any> = SkyblockAddonsLoadingPlugin.coremodList!!

            val nameField = coremodList[0].javaClass.getField("name")
            var coreFound = false

            nameField.isAccessible = true

            for (coreMod in coremodList) {
                val name = nameField[coreMod] as String

                if (name == SkyblockAddonsLoadingPlugin::class.java.simpleName) {
                    if (!coreFound) {
                        coreFound = true
                    } else {
                        throw RuntimeException(
                            "Launch failed because a duplicate installation of SkyblockAddons was found." +
                                    " Please remove it and restart Minecraft!"
                        )
                    }
                }
            }

            nameField.isAccessible = false

            logDebug("No duplicate installations were found")
        } catch (ex: ReflectiveOperationException) {
            log(Level.ERROR, ex, "An error occurred while checking for duplicate SkyblockAddons installations!")
            // It's okay, this is just for duplicate detection anyways...
        }

        return null
    }

    /**
     * This method writes a message to the game log at the given log level, along with a `Throwable`, if provided.
     * The mod name and class name are added to the beginning of the message if the mod is running in a production
     * environment since the Minecraft client does not log this info.
     *
     * @param level the log level to write to
     * @param message the message
     */
    private fun log(level: Level, throwable: Throwable?, message: String) {
        val loggerName = "SkyblockAddons/" + javaClass.simpleName

        if (throwable != null) {
            FMLRelaunchLog.log(
                loggerName,
                level,
                throwable,
                (if (SkyblockAddonsTransformer.isDeobfuscated()) "" else "[$loggerName] ") + message
            )
        } else {
            FMLRelaunchLog.log(
                loggerName,
                level,
                (if (SkyblockAddonsTransformer.isDeobfuscated()) "" else "[$loggerName] ") + message
            )
        }
    }

    /**
     * This method writes a message to the game log at the `DEBUG` level.
     * The mod name and class name are added to the beginning of the message if the mod is running in a production
     * environment since the Minecraft client does not log this info.
     *
     * @param message the message
     */
    private fun logDebug(message: String) {
        log(Level.DEBUG, null, message)
    }

    override fun injectData(data: Map<String, Any>) {
    }
}
