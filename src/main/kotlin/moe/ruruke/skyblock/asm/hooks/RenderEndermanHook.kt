package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus


object RenderEndermanHook {
    private val BLANK_ENDERMAN_TEXTURE = net.minecraft.util.ResourceLocation("skyblockaddons", "blankenderman.png")

    fun getEndermanTexture(endermanTexture: net.minecraft.util.ResourceLocation): net.minecraft.util.ResourceLocation {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val location: moe.ruruke.skyblock.core.Location = main.utils!!.getLocation()
        if (main.utils!!
                .isOnSkyblock() && (location == moe.ruruke.skyblock.core.Location.DRAGONS_NEST || location == moe.ruruke.skyblock.core.Location.ZEALOT_BRUISER_HIDEOUT || location == moe.ruruke.skyblock.core.Location.VOID_SLATE) && main.configValues!!
                .isEnabled(moe.ruruke.skyblock.core.Feature.CHANGE_ZEALOT_COLOR)
        ) {
            return BLANK_ENDERMAN_TEXTURE
        }
        return endermanTexture
    }
}
