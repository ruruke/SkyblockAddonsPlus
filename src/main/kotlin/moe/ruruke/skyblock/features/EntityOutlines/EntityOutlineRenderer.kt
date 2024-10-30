package moe.ruruke.skyblock.features.EntityOutlines

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.events.RenderEntityOutlineEvent
import moe.ruruke.skyblock.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.logging.log4j.Logger
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Class to handle all entity outlining, including xray and no-xray rendering
 * Features that include entity outlining should subscribe to the [RenderEntityOutlineEvent].
 *
 *
 * See [FeatureItemOutlines] for an example of how to add specific entities based on predicates
 */
class EntityOutlineRenderer {
    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     *
     *
     * Calls to [.shouldRender] are frustum based, rely on partialTicks,
     * and so can't be updated on a per-tick basis without losing information.
     *
     *
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            val mc: Minecraft = Minecraft.getMinecraft()
            if (mc.theWorld != null && shouldRenderEntityOutlines()) {
                // These events need to be called in this specific order for the xray to have priority over the no xray
                // Get all entities to render xray outlines
                val xrayOutlineEvent: RenderEntityOutlineEvent = RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.XRAY, null)
                MinecraftForge.EVENT_BUS.post(xrayOutlineEvent)
                // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
                val noxrayOutlineEvent: RenderEntityOutlineEvent =
                    RenderEntityOutlineEvent(RenderEntityOutlineEvent.Type.NO_XRAY, xrayOutlineEvent.getEntitiesToChooseFrom())
                MinecraftForge.EVENT_BUS.post(noxrayOutlineEvent)
                // Cache the entities for future use
                entityRenderCache.setXrayCache(xrayOutlineEvent.getEntitiesToOutline()!!)
                entityRenderCache.setNoXrayCache(noxrayOutlineEvent.getEntitiesToOutline()!!)
                entityRenderCache.setNoOutlineCache(noxrayOutlineEvent.getEntitiesToChooseFrom()!!)

                if (isCacheEmpty) {
                    if (!emptyLastTick) {
                        mc.renderGlobal.entityOutlineFramebuffer.framebufferClear()
                    }
                    emptyLastTick = true
                } else {
                    emptyLastTick = false
                }
            } else if (!emptyLastTick) {
                entityRenderCache.setXrayCache(null)
                entityRenderCache.setNoXrayCache(null)
                entityRenderCache.setNoOutlineCache(null)
                if (mc.renderGlobal.entityOutlineFramebuffer != null) mc.renderGlobal.entityOutlineFramebuffer.framebufferClear()
                emptyLastTick = true
            }
        }
    }

    private class CachedInfo {
        private var xrayCache: HashMap<Entity, Int>? = null

        fun getXrayCache(): HashMap<Entity, Int>? {
            return xrayCache
        }
        fun setXrayCache(xrayCaches: HashMap<Entity, Int>?){
            xrayCache = xrayCaches
        }

        private var noXrayCache: HashMap<Entity, Int>? = null
        fun getNoXrayCache(): HashMap<Entity, Int>? {
            return noXrayCache
        }
        fun setNoXrayCache(xrayCaches: HashMap<Entity, Int>?){
            noXrayCache = xrayCaches
        }

        private var noOutlineCache: HashSet<Entity>? = null
        fun getNoOutlineCache(): HashSet<Entity>? {
            return noOutlineCache
        }

        fun setNoOutlineCache(_noOutlineCache: HashSet<Entity>?){
            noOutlineCache = _noOutlineCache
        }
    }

    companion object {
        private val logger: Logger = SkyblockAddonsPlus.getLogger()
        private val entityRenderCache = CachedInfo()
        private var stopLookingForOptifine = false
        private var isFastRender: Method? = null
        private var isShaders: Method? = null
        private var isAntialiasing: Method? = null
        private var swapBuffer: Framebuffer? = null

        /**
         * @return a new framebuffer with the size of the main framebuffer
         */
        private fun initSwapBuffer(): Framebuffer {
            val main: Framebuffer = Minecraft.getMinecraft().getFramebuffer()
            val framebuffer: Framebuffer =
                Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true)
            framebuffer.setFramebufferFilter(GL11.GL_NEAREST)
            framebuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
            return framebuffer
        }

        private fun updateFramebufferSize() {
            if (swapBuffer == null) {
                swapBuffer = initSwapBuffer()
            }
            val width: Int = Minecraft.getMinecraft().displayWidth
            val height: Int = Minecraft.getMinecraft().displayHeight
            if (swapBuffer!!.framebufferWidth != width || swapBuffer!!.framebufferHeight != height) {
                swapBuffer!!.createBindFramebuffer(width, height)
            }
            val rg: RenderGlobal = Minecraft.getMinecraft().renderGlobal
            val outlineBuffer: Framebuffer = rg.entityOutlineFramebuffer
            if (outlineBuffer.framebufferWidth != width || outlineBuffer.framebufferHeight != height) {
                outlineBuffer.createBindFramebuffer(width, height)
                rg.entityOutlineShader.createBindFramebuffers(width, height)
            }
        }


        /**
         * Renders xray and no-xray entity outlines.
         *
         * @param camera       the current camera
         * @param partialTicks the progress to the next tick
         * @param x            the camera x position
         * @param y            the camera y position
         * @param z            the camera z position
         */
        fun renderEntityOutlines(camera: ICamera, partialTicks: Float, x: Double, y: Double, z: Double): Boolean {
            val shouldRenderOutlines = shouldRenderEntityOutlines()

            if (shouldRenderOutlines && !isCacheEmpty && MinecraftForgeClient.getRenderPass() == 0) {
                val mc: Minecraft = Minecraft.getMinecraft()
                val renderGlobal: RenderGlobal = mc.renderGlobal
                val renderManager: RenderManager = mc.getRenderManager()

                mc.theWorld.theProfiler.endStartSection("entityOutlines")
                updateFramebufferSize()
                // Clear and bind the outline framebuffer
                renderGlobal.entityOutlineFramebuffer.framebufferClear()
                renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false)

                // Vanilla options
                RenderHelper.disableStandardItemLighting()
                GlStateManager.disableFog()
                mc.getRenderManager().setRenderOutlines(true)

                // SBA options
                DrawUtils.enableOutlineMode()

                // Render x-ray outlines first, ignoring the depth buffer bit
                if (!isXrayCacheEmpty) {
                    // Xray is enabled by disabling depth testing

                    GlStateManager.depthFunc(GL11.GL_ALWAYS)
                    for ((key, value) in entityRenderCache.getXrayCache()!!.entries) {
                        // Test if the entity should render, given the player's camera position
                        if (shouldRender(camera, key, x, y, z)) {
                            try {
                                if (key !is EntityLivingBase) {
                                    DrawUtils.outlineColor(value)
                                }
                                renderManager.renderEntityStatic(key, partialTicks, true)
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                    // Reset depth function
                    GlStateManager.depthFunc(GL11.GL_LEQUAL)
                }
                // Render no-xray outlines second, taking into consideration the depth bit
                if (!isNoXrayCacheEmpty) {
                    if (!isNoOutlineCacheEmpty) {
                        // Render other entities + terrain that may occlude an entity outline into a depth buffer
                        swapBuffer!!.framebufferClear()
                        copyBuffers(mc.getFramebuffer(), swapBuffer!!, GL11.GL_DEPTH_BUFFER_BIT)
                        swapBuffer!!.bindFramebuffer(false)
                        // Copy terrain + other entities depth into outline frame buffer to now switch to no-xray outlines
                        if (entityRenderCache.getNoOutlineCache() != null) {
                            for (entity in entityRenderCache.getNoOutlineCache()!!) {
                                // Test if the entity should render, given the player's instantaneous camera position
                                if (shouldRender(camera, entity, x, y, z)) {
                                    try {
                                        renderManager.renderEntityStatic(entity, partialTicks, true)
                                    } catch (ignored: Exception) {
                                    }
                                }
                            }
                        }

                        // Copy the entire depth buffer of everything that might occlude outline to outline framebuffer
                        copyBuffers(swapBuffer!!, renderGlobal.entityOutlineFramebuffer, GL11.GL_DEPTH_BUFFER_BIT)
                        renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false)
                    } else {
                        copyBuffers(
                            mc.getFramebuffer(),
                            renderGlobal.entityOutlineFramebuffer,
                            GL11.GL_DEPTH_BUFFER_BIT
                        )
                    }

                    // Xray disabled by re-enabling traditional depth testing
                    for ((key, value) in entityRenderCache.getNoXrayCache()!!.entries) {
                        // Test if the entity should render, given the player's instantaneous camera position
                        if (shouldRender(camera, key, x, y, z)) {
                            try {
                                if (key !is EntityLivingBase) {
                                    DrawUtils.outlineColor(value)
                                }
                                renderManager.renderEntityStatic(key, partialTicks, true)
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }

                // SBA options
                DrawUtils.disableOutlineMode()

                // Vanilla options
                RenderHelper.enableStandardItemLighting()
                mc.getRenderManager().setRenderOutlines(false)

                // Load the outline shader
                GlStateManager.depthMask(false)
                renderGlobal.entityOutlineShader.loadShaderGroup(partialTicks)
                GlStateManager.depthMask(true)

                // Reset GL/framebuffers for next render layers
                GlStateManager.enableLighting()
                mc.getFramebuffer().bindFramebuffer(false)
                GlStateManager.enableFog()
                GlStateManager.enableBlend()
                GlStateManager.enableColorMaterial()
                GlStateManager.enableDepth()
                GlStateManager.enableAlpha()
            }

            return !shouldRenderOutlines
        }


        fun getCustomOutlineColor(entity: EntityLivingBase?): Int? {
            if (entityRenderCache.getXrayCache() != null && entityRenderCache.getXrayCache()!!.containsKey(entity!!)) {
                return entityRenderCache.getXrayCache()!!.get(entity)
            }
            if (entityRenderCache.getNoXrayCache() != null && entityRenderCache.getNoXrayCache()!!.containsKey(entity!!)) {
                return entityRenderCache.getNoXrayCache()!!.get(entity)
            }
            return null
        }

        /**
         * Caches optifine settings and determines whether outlines should be rendered
         *
         * @return `true` iff outlines should be rendered
         */
        fun shouldRenderEntityOutlines(): Boolean {
            val mc: Minecraft = Minecraft.getMinecraft()
            val renderGlobal: RenderGlobal = mc.renderGlobal
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

            // Vanilla Conditions
            if (renderGlobal.entityOutlineFramebuffer == null || renderGlobal.entityOutlineShader == null || mc.thePlayer == null) return false

            // Skyblock Conditions
            if (!main.utils!!.isOnSkyblock()) {
                return false
            }

            // Main toggle for outlines features
            if (main.configValues!!.isDisabled(Feature.ENTITY_OUTLINES)) {
                return false
            }

            // Optifine Conditions
            if (!stopLookingForOptifine && isFastRender == null) {
                try {
                    val config = Class.forName("Config")

                    try {
                        isFastRender = config.getMethod("isFastRender")
                        isShaders = config.getMethod("isShaders")
                        isAntialiasing = config.getMethod("isAntialiasing")
                    } catch (ex: Exception) {
                        logger.warn("Couldn't find Optifine methods for entity outlines.")
                        stopLookingForOptifine = true
                    }
                } catch (ex: Exception) {
                    logger.info("Couldn't find Optifine for entity outlines.")
                    stopLookingForOptifine = true
                }
            }

            var isFastRenderValue = false
            var isShadersValue = false
            var isAntialiasingValue = false
            if (isFastRender != null) {
                try {
                    isFastRenderValue = isFastRender!!.invoke(null) as Boolean
                    isShadersValue = isShaders!!.invoke(null) as Boolean
                    isAntialiasingValue = isAntialiasing!!.invoke(null) as Boolean
                } catch (ex: IllegalAccessException) {
                    logger.warn("An error occurred while calling Optifine methods for entity outlines...", ex)
                } catch (ex: InvocationTargetException) {
                    logger.warn("An error occurred while calling Optifine methods for entity outlines...", ex)
                }
            }

            return !isFastRenderValue && !isShadersValue && !isAntialiasingValue
        }

        /**
         * Apply the same rendering standards as in [RenderGlobal.renderEntities] lines 659 to 669
         *
         * @param camera the current camera
         * @param entity the entity to render
         * @param x      the camera x position
         * @param y      the camera y position
         * @param z      the camera z position
         * @return whether the entity should be rendered
         */
        private fun shouldRender(camera: ICamera, entity: Entity, x: Double, y: Double, z: Double): Boolean {
            val mc: Minecraft = Minecraft.getMinecraft()
            //if (considerPass && !entity.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
            //    return false;
            //}
            // Only render the view entity when sleeping or in 3rd person mode mode
            if (entity === mc.getRenderViewEntity() &&
                !((mc.getRenderViewEntity() is EntityLivingBase && (mc.getRenderViewEntity() as EntityLivingBase).isPlayerSleeping()) ||
                        mc.gameSettings.thirdPersonView != 0)
            ) {
                return false
            }
            // Only render if renderManager would render and the world is loaded at the entity
            return mc.theWorld.isBlockLoaded(BlockPos(entity)) && (mc.getRenderManager()
                .shouldRender(entity, camera, x, y, z) || entity.riddenByEntity === mc.thePlayer)
        }

        /**
         * Function that copies a portion of a framebuffer to another framebuffer.
         *
         *
         * Note that this requires GL3.0 to function properly
         *
         *
         * The major use of this function is to copy the depth-buffer portion of the world framebuffer to the entity outline framebuffer.
         * This enables us to perform no-xray outlining on entities, as we can use the world framebuffer's depth testing on the outline frame buffer
         *
         * @param frameToCopy   the framebuffer from which we are copying data
         * @param frameToPaste  the framebuffer onto which we are copying the data
         * @param buffersToCopy the bit mask indicating the sections to copy (see [GL11.GL_DEPTH_BUFFER_BIT], [GL11.GL_COLOR_BUFFER_BIT], [GL11.GL_STENCIL_BUFFER_BIT])
         */
        private fun copyBuffers(frameToCopy: Framebuffer, frameToPaste: Framebuffer, buffersToCopy: Int) {
            if (OpenGlHelper.isFramebufferEnabled()) {
                OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy.framebufferObject)
                OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste.framebufferObject)
                GL30.glBlitFramebuffer(
                    0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                    0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                    buffersToCopy, GL11.GL_NEAREST
                )
            }
        }

        val isCacheEmpty: Boolean
            get() = isXrayCacheEmpty && isNoXrayCacheEmpty

        private val isXrayCacheEmpty: Boolean
            get() = entityRenderCache.getXrayCache() == null || entityRenderCache.getXrayCache()!!.isEmpty()

        private val isNoXrayCacheEmpty: Boolean
            get() = entityRenderCache.getNoXrayCache() == null || entityRenderCache.getNoXrayCache()!!.isEmpty()

        private val isNoOutlineCacheEmpty: Boolean
            get() = entityRenderCache.getNoOutlineCache() == null || entityRenderCache.getNoOutlineCache()!!.isEmpty()

        private var emptyLastTick = false
    }
}
