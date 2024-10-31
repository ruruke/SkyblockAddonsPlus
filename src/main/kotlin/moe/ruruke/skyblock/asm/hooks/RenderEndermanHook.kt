package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import net.minecraft.util.ResourceLocation


class RenderEndermanHook {
    companion object {
        @JvmStatic
        private val BLANK_ENDERMAN_TEXTURE = ResourceLocation("skyblockaddonsplus", "blankenderman.png")

        @JvmStatic
        fun getEndermanTexture(endermanTexture: ResourceLocation): ResourceLocation {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            val location: moe.ruruke.skyblock.core.Location = main.utils!!.getLocation()

            if (main.utils!!.isOnSkyblock() && (location == moe.ruruke.skyblock.core.Location.DRAGONS_NEST || location == moe.ruruke.skyblock.core.Location.ZEALOT_BRUISER_HIDEOUT || location == moe.ruruke.skyblock.core.Location.VOID_SLATE)
                && NewConfig.isEnabled(Feature.CHANGE_ZEALOT_COLOR)
            ) {
                return BLANK_ENDERMAN_TEXTURE
            }
            return endermanTexture
        }

    }
}
