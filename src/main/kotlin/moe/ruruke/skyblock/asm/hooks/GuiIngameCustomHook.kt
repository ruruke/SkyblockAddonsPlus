package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature

/**
 * Alternative hooks for the labymod custom gui, to disable specific bars.
 */
class GuiIngameCustomHook {
    companion object {
        @JvmStatic
        fun shouldRenderArmor(): Boolean {
            return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR)
        }
        @JvmStatic
        fun shouldRenderHealth(): Boolean {
            return shouldRender(Feature.HIDE_HEALTH_BAR)
        }
        @JvmStatic
        fun shouldRenderFood(): Boolean {
            return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR)
        }
        @JvmStatic
        fun shouldRenderMountHealth(): Boolean {
            return shouldRender(Feature.HIDE_PET_HEALTH_BAR)
        }
        @JvmStatic
        fun shouldRender(feature: Feature?): Boolean {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (!main.utils!!.isOnSkyblock()) {
                return true
            }
            return !main.configValues!!.isEnabled(feature!! )
        }
    }
}
