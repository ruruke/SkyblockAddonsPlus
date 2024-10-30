package moe.ruruke.skyblock.features.fishParticles

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.OverlayEffectRenderer
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

class FishParticleOverlay : OverlayEffectRenderer() {
    private var biggerWakeCache = false

    init {
        feature = Feature.FISHING_PARTICLE_OVERLAY
    }

    /**
     * @return `true` iff the fishing particle overlay is enabled.
     */
    override fun shouldRenderOverlay(): Boolean {
        return super.shouldRenderOverlay() && SkyblockAddonsPlus.configValues!!.isEnabled(feature!!)
    }

    /**
     * Setup the fishing particle overlay render environment.
     */
    override fun setupRenderEnvironment() {
        super.setupRenderEnvironment()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        biggerWakeCache = SkyblockAddonsPlus.configValues!!.isEnabled(Feature.BIGGER_WAKE)
    }

    /**
     * End the fishing particle overlay render environment.
     */
    override fun endRenderEnvironment() {
        super.endRenderEnvironment()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
    }

    /**
     * Setup the render environment for a fish particle.
     * @param effect the fish particle effect to be rendered
     */
    override fun setupRenderEffect(effect: EntityFX?) {
        if (biggerWakeCache) {
            effect!!.particleScale *= 2f
            effect!!.posY += .1
            effect!!.prevPosY += .1
        }
    }

    /**
     * End the render environment for a fish particle.
     * @param effect the effect that was just rendered
     */
    override fun endRenderEffect(effect: EntityFX?) {
        if (biggerWakeCache) {
            effect!!.particleScale /= 2f
            effect!!.posY -= .1
            effect!!.prevPosY -= .1
        }
    }
}
