package moe.ruruke.skyblock.features.healingcircle

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.MathUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11


object HealingCircleManager {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private val healingCircles: MutableSet<HealingCircle> = com.google.common.collect.Sets.newConcurrentHashSet()

    fun addHealingCircleParticle(healingCircleParticle: HealingCircleParticle) {
        var nearbyHealingCircle: HealingCircle? = null
        for (healingCircle in healingCircles) {
            if (healingCircle.hasCachedCenterPoint()) {
                val circleCenter = healingCircle.circleCenter
                if (healingCircleParticle.getPoint().distance(
                        circleCenter.getX(),
                        circleCenter.getY()
                    ) < (HealingCircle.Companion.DIAMETER + 2) / 2f
                ) {
                    nearbyHealingCircle = healingCircle
                    break
                }
            } else {
                if (healingCircleParticle.getPoint()
                        .distance(healingCircle.averageX, healingCircle.averageZ) < HealingCircle.Companion.DIAMETER + 2
                ) {
                    nearbyHealingCircle = healingCircle
                    break
                }
            }
        }

        nearbyHealingCircle?.addPoint(healingCircleParticle)
            ?: healingCircles.add(HealingCircle(healingCircleParticle))
    }

    fun renderHealingCircleOverlays(partialTicks: Float) {
        if (main.utils!!.isOnSkyblock() && main.configValues!!.isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL)) {
            val healingCircleIterator = healingCircles.iterator()
            while (healingCircleIterator.hasNext()) {
                val healingCircle = healingCircleIterator.next()

                healingCircle.removeOldParticles()
                if (System.currentTimeMillis() - healingCircle.getCreation() > 1000 && healingCircle.particlesPerSecond < 10) {
                    healingCircleIterator.remove()
                    continue
                }

                val circleCenter = healingCircle.circleCenter
                if (circleCenter != null && !java.lang.Double.isNaN(circleCenter.getX()) && !java.lang.Double.isNaN(
                        circleCenter.getY()
                    )
                ) {
                    GlStateManager.pushMatrix()
                    GL11.glNormal3f(0.0f, 1.0f, 0.0f)

                    GlStateManager.disableLighting()
                    GlStateManager.depthMask(false)
                    GlStateManager.enableDepth()
                    GlStateManager.enableBlend()
                    GlStateManager.depthFunc(GL11.GL_LEQUAL)
                    GlStateManager.disableCull()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    GlStateManager.enableAlpha()
                    GlStateManager.disableTexture2D()

                    val chroma: Boolean =
                        main.configValues!!.getChromaFeatures().contains(Feature.SHOW_HEALING_CIRCLE_WALL)
                    val color: Int = main.configValues!!.getColor(
                        Feature.SHOW_HEALING_CIRCLE_WALL, ColorUtils.getAlphaIntFromFloat(
                            MathUtils.clamp(
                                main.configValues!!.getHealingCircleOpacity().toFloat(), 0f, 1f
                            )
                        )
                    )
                    DrawUtils.drawCylinder(
                        circleCenter.getX(),
                        0.0,
                        circleCenter.getY(),
                        HealingCircle.Companion.RADIUS,
                        255f,
                        ColorUtils.getDummySkyblockColor(color, chroma)
                    )

                    GlStateManager.enableCull()
                    GlStateManager.enableTexture2D()
                    GlStateManager.enableDepth()
                    GlStateManager.depthMask(true)
                    GlStateManager.enableLighting()
                    GlStateManager.disableBlend()
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                    GlStateManager.popMatrix()
                }
            }
        }
    }
}
