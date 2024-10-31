package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.ColorUtils


class ModelEndermanHook {
    companion object {
        @JvmStatic
        fun setEndermanColor() {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            val location: moe.ruruke.skyblock.core.Location = main.utils!!.getLocation()
            if (main.utils!!.isOnSkyblock() && (location == moe.ruruke.skyblock.core.Location.DRAGONS_NEST || location == moe.ruruke.skyblock.core.Location.ZEALOT_BRUISER_HIDEOUT || location == moe.ruruke.skyblock.core.Location.VOID_SLATE)
                && NewConfig.isEnabled(moe.ruruke.skyblock.core.Feature.CHANGE_ZEALOT_COLOR)
            ) {
                val color: Int = main.configValues!!.getColor(Feature.CHANGE_ZEALOT_COLOR)
                ColorUtils.bindColor(color)
            }
        }
    }
}
