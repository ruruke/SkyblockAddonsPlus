package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer

object TileEntityEnderChestRendererHook {
    private val BLANK_ENDERCHEST = net.minecraft.util.ResourceLocation("skyblockaddons", "blankenderchest.png")

    fun bindTexture(
        tileEntityEnderChestRenderer: TileEntityEnderChestRenderer,
        enderChestTexture: net.minecraft.util.ResourceLocation?
    ) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

        if (main.utils!!.isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.configValues!!
                .isEnabled(moe.ruruke.skyblock.core.Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
            (main.utils!!.getLocation() === moe.ruruke.skyblock.core.Location.THE_END || main.utils!!
                .getLocation() === moe.ruruke.skyblock.core.Location.DRAGONS_NEST)
        ) {
            tileEntityEnderChestRenderer.bindTexture(BLANK_ENDERCHEST)
        } else {
            tileEntityEnderChestRenderer.bindTexture(enderChestTexture)
        }
    }

    fun setEnderchestColor() {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        if (main.utils!!.isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.configValues!!
                .isEnabled(moe.ruruke.skyblock.core.Feature.MAKE_ENDERCHESTS_GREEN_IN_END) && main.utils!!
                .getLocation() === moe.ruruke.skyblock.core.Location.DRAGONS_NEST
        ) {
            val color: Int =
                main.configValues!!.getColor(moe.ruruke.skyblock.core.Feature.MAKE_ENDERCHESTS_GREEN_IN_END)
            if (color == ColorCode.GREEN.getColor()) {
                GlStateManager.color(0f, 1f, 0f) // classic lime green
            } else {
                moe.ruruke.skyblock.utils.ColorUtils.bindColor(color)
            }
        }
    }
}
