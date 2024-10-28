package moe.ruruke.skyblock.command

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import moe.ruruke.skyblock.SkyblockAddonsPlus

/**
 * An example command implementing the Command api of OneConfig.
 * Registered in ExampleMod.java with `CommandManager.INSTANCE.registerCommand(new ExampleCommand());`
 *
 * @see Command
 *
 * @see Main
 *
 * @see SkyblockAddonsPlus
 */
@Command(value = SkyblockAddonsPlus.MODID, description = "Access the " + SkyblockAddonsPlus.NAME + " GUI.")
class ExampleCommand {
    @Main
    private fun handle() {
        SkyblockAddonsPlus.config!!.openGui()
    }
}