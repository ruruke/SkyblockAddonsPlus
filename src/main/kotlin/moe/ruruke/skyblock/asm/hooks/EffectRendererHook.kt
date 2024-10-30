package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.OverlayEffectRenderer
import moe.ruruke.skyblock.features.fishParticles.FishParticleManager
import moe.ruruke.skyblock.features.healingcircle.HealingCircleManager
import moe.ruruke.skyblock.features.healingcircle.HealingCircleParticle
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityAuraFX
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.particle.EntityFishWakeFX
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

object EffectRendererHook {
    private val effectRenderers: MutableSet<OverlayEffectRenderer> = HashSet<OverlayEffectRenderer>()

    @Suppress("unused")
    fun onAddParticle(entity: EntityFX) {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        val mc: Minecraft = Minecraft.getMinecraft()
        val player: EntityPlayer = mc.thePlayer

        if (main.utils!!.isOnSkyblock()) {
            if (main.utils!!.isInDungeon() && main.configValues!!
                    .isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL) && entity is EntityAuraFX && entity.posY % 1 == 0.0
            ) {
                HealingCircleManager.addHealingCircleParticle(HealingCircleParticle(entity.posX, entity.posZ))
            } else if (player != null && player.fishEntity != null && main.configValues!!
                    .isEnabled(Feature.FISHING_PARTICLE_OVERLAY) && entity is EntityFishWakeFX
            ) {
                FishParticleManager.onFishWakeSpawn(entity as EntityFishWakeFX)
            }
        }
    }

    /**
     * Called every frame directly after particle rendering to overlay modified particles to the screen.
     * @param partialTicks a float in [0, 1) indicating the progress to the next tick
     */
    @Suppress("unused")
    fun renderParticleOverlays(partialTicks: Float) {
        val info = OverlayInfo(partialTicks)

        for (renderer in effectRenderers) {
            renderer.renderOverlayParticles(info)
        }
    }


    /**
     * Called from [OverlayEffectRenderer] during object initialization to render the registered particles every frame.
     * @param renderer the attached renderer
     */
    fun registerOverlay(renderer: OverlayEffectRenderer) {
        effectRenderers.add(renderer)
    }

    class OverlayInfo(private val partialTicks: Float) {
        private val rotationX: Float = ActiveRenderInfo.getRotationX()
        fun getRotationX(): Float {
            return rotationX
        }
        private val rotationZ: Float = ActiveRenderInfo.getRotationZ()
        fun getRotationZ(): Float {
            return rotationZ
        }
        private val rotationYZ: Float = ActiveRenderInfo.getRotationYZ()
        fun getRotationYZ(): Float {
            return rotationYZ
        }
        private val rotationXY: Float = ActiveRenderInfo.getRotationXY()
        fun getRotationXY(): Float {
            return rotationXY
        }
        private val rotationXZ: Float = ActiveRenderInfo.getRotationXZ()
        fun getRotationXZ(): Float {
            return rotationXZ
        }
        private val renderer: TextureManager = Minecraft.getMinecraft().effectRenderer.renderer
        fun getRenderer(): TextureManager {
            return renderer
        }
        private val worldRenderer: WorldRenderer = Tessellator.getInstance().getWorldRenderer()
        fun getWorldRenderer(): WorldRenderer {
            return worldRenderer
        }
        private val renderViewEntity: Entity = Minecraft.getMinecraft().getRenderViewEntity()
        fun getRenderViewEntity(): Entity {
            return renderViewEntity
        }
        fun getPartialTicks(): Float {
            return partialTicks
        }
    }
}
