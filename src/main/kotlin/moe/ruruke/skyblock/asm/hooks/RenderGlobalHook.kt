package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.features.EntityOutlines.EntityOutlineRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.util.BlockPos
import java.lang.reflect.Method

class RenderGlobalHook {
    companion object {
        private const val stopLookingForOptifine = false

        @JvmStatic
        private val isFastRender: Method? = null
        @JvmStatic
        private val isShaders: Method? = null
        @JvmStatic
        private val isAntialiasing: Method? = null

        @JvmStatic
        private val logger = SkyblockAddonsPlus.getLogger()

        @JvmStatic
        fun shouldRenderSkyblockItemOutlines(): Boolean {
            return EntityOutlineRenderer.shouldRenderEntityOutlines()
        }

        @JvmStatic
        fun afterFramebufferDraw() {
            GlStateManager.enableDepth()
        }

        @JvmStatic
        fun blockRenderingSkyblockItemOutlines(
            camera: ICamera?,
            partialTicks: Float,
            x: Double,
            y: Double,
            z: Double
        ): Boolean {
            return EntityOutlineRenderer.renderEntityOutlines(camera!!, partialTicks, x, y, z)
        }

        @JvmStatic
        fun onAddBlockBreakParticle(breakerId: Int, pos: BlockPos, progress: Int) {
            //TODO:
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            // On public islands, hypixel sends a progress = 10 update once it registers the start of block breaking
            if (breakerId == 0 && main.utils!!.getLocation() !== Location.ISLAND &&
                pos == MinecraftHook.prevClickBlock && progress == 10
            ) {
                //System.out.println(progress);
                MinecraftHook.startMineTime = System.currentTimeMillis()
            }
        }
    }
}
