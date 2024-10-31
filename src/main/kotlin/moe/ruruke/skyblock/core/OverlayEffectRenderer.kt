package moe.ruruke.skyblock.core

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.hooks.EffectRendererHook
import moe.ruruke.skyblock.utils.SkyblockColor
import moe.ruruke.skyblock.utils.draw.DrawState3D
import net.minecraft.client.particle.EffectRenderer
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation

/**
 * This class is made to extend the functionality of [EffectRenderer] to integrate Skyblock Addons features and logic.
 * The class is meant to provide the foundation (super class) for individual features that want to integrate particle/effect rendering into their logic.
 * See TODO for an example implementation.
 * The end goal of this module is to embrace a feature-driven decentralized design, while providing easy access to particle/effect rendering.
 */
open class OverlayEffectRenderer {
    private var overlayParticles: Array<Array<MutableSet<EntityFX>?>> = emptyArray();
    protected var feature: Feature? = null

    private fun initParticles() {
        this.overlayParticles = Array(4) { arrayOfNulls(2) }
        for (i in 0..3) {
            for (j in 0..1) {
                overlayParticles[i][j] = HashSet()
            }
        }
    }

    init {
        initParticles()
        EffectRendererHook.registerOverlay(this)
    }

    fun addParticle(particle: EntityFX?) {
        if (particle == null) {
            return
        }
        val i: Int = particle.getFXLayer()
        val j: Int = if (particle.getAlpha() != 1.0f) 0 else 1

        if (overlayParticles.get(i).get(j)!!.size >= 100) {
            val itr: MutableIterator<EntityFX> = overlayParticles.get(i).get(j)!!.iterator()
            itr.next()
            itr.remove()
        }
        overlayParticles.get(i).get(j)!!.add(particle)
    }

    fun clearParticles() {
        for (i in 0..3) {
            for (j in 0..1) {
                overlayParticles.get(i).get(j)!!.clear()
            }
        }
    }

    /**
     * Whether we should render the overlay on this frame. Return `true` to render
     */
    open fun shouldRenderOverlay(): Boolean {
        return SkyblockAddonsPlus.utils!!.isOnSkyblock()
    }


    /**
     * Setup the overlay render environment. Called once in [.renderOverlayParticles], before all rendering occurs.
     * Override this in a subclass to set up the render environment for a given feature. Defaults to using the previous color.
     */
    open fun setupRenderEnvironment() {
        if (feature != null) {
            DRAW_PARTICLE.setColor(SkyblockAddonsPlus.configValues!!.getSkyblockColor(feature!!))
                .newColorEnv()
        }
    }


    /**
     * End the render environment. Called once in [.renderOverlayParticles], after all rendering occurs.
     */
    open fun endRenderEnvironment() {
        DRAW_PARTICLE.endColorEnv()
    }


    /**
     * Called directly before rendering an effect
     * @param effect the effect about to be rendered
     */
    open fun setupRenderEffect(effect: EntityFX?) {
    }


    /**
     * Called directly after rendering an effect
     * @param effect the effect that was just rendered
     */
    open fun endRenderEffect(effect: EntityFX?) {
    }


    /**
     * Main method to render particles
     * @param info setup information used to render the particle overlay
     */
    fun renderOverlayParticles(info: EffectRendererHook.Companion.OverlayInfo) {
        if (!shouldRenderOverlay()) {
            return
        }
        val partialTicks: Float = info.getPartialTicks()
        val rotationX: Float = info.getRotationX()
        val rotationZ: Float = info.getRotationZ()
        val rotationYZ: Float = info.getRotationYZ()
        val rotationXY: Float = info.getRotationXY()
        val rotationXZ: Float = info.getRotationXZ()
        val renderer: TextureManager = info.getRenderer()
        val entity: Entity = info.getRenderViewEntity()
        val worldRenderer: WorldRenderer = info.getWorldRenderer()

        val particleTextures: ResourceLocation = EffectRenderer.particleTextures

        setupRenderEnvironment()

        for (i in 0..2) {
            for (j in 0..1) {
                overlayParticles.get(i).get(j)!!.removeIf { entityFX: EntityFX -> entityFX.isDead }
                if (!overlayParticles.get(i).get(j)!!.isEmpty()) {
                    GlStateManager.depthMask(j == 1)
                    if (i == 1) {
                        renderer.bindTexture(TextureMap.locationBlocksTexture)
                    } else {
                        renderer.bindTexture(particleTextures)
                    }

                    GlStateManager.enableColorMaterial()
                    for (effect: EntityFX in overlayParticles.get(i).get(j)!!) {
                        try {
                            // Set up the outline color
                            DRAW_PARTICLE.beginWorldRenderer()
                                .bindColor(effect.posX.toFloat(), effect.posY.toFloat(), effect.posZ.toFloat())

                            setupRenderEffect(effect)
                            effect.renderParticle(
                                worldRenderer,
                                entity,
                                partialTicks,
                                rotationX,
                                rotationXZ,
                                rotationZ,
                                rotationYZ,
                                rotationXY
                            )
                            endRenderEffect(effect)

                            // Send vertices to the GPU
                            DRAW_PARTICLE.draw()
                        } catch (ex: Throwable) {
                            //logger.warn("Couldn't render outline for effect " + effect.toString() + ".");
                            //logger.catching(ex); // Just move on to the next entity...
                        }
                    }

                    GlStateManager.disableColorMaterial()
                }
            }
        }
        endRenderEnvironment()
    }

    companion object {
        protected var DRAW_PARTICLE: DrawState3D =
            DrawState3D(SkyblockColor(-0x1), 7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP, true, true)
    }
}
