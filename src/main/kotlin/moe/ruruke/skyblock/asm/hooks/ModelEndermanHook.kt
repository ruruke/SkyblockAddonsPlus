package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus


object ModelEndermanHook {
    fun setEndermanColor() {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val location: moe.ruruke.skyblock.core.Location = main.utils!!.getLocation()
        if (main.utils!!
                .isOnSkyblock() && (location == moe.ruruke.skyblock.core.Location.DRAGONS_NEST || location == moe.ruruke.skyblock.core.Location.ZEALOT_BRUISER_HIDEOUT || location == moe.ruruke.skyblock.core.Location.VOID_SLATE) && main.configValues!!
                .isEnabled(moe.ruruke.skyblock.core.Feature.CHANGE_ZEALOT_COLOR)
        ) {
            val color: Int = main.configValues!!.getColor(moe.ruruke.skyblock.core.Feature.CHANGE_ZEALOT_COLOR)
            moe.ruruke.skyblock.utils.ColorUtils.bindColor(color)
        }
    }
}
