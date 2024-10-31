package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.SkyblockColor
import moe.ruruke.skyblock.utils.draw.DrawStateFontRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer

class FontRendererHook {
    companion object {
        @JvmStatic
        private val CHROMA_COLOR: SkyblockColor = SkyblockColor(-0x1).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA)
        @JvmStatic
        private val DRAW_CHROMA: DrawStateFontRenderer = DrawStateFontRenderer(CHROMA_COLOR)
        @JvmStatic
        private val CHROMA_COLOR_SHADOW: SkyblockColor =
            SkyblockColor(-0xaaaaab).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA)
        @JvmStatic
        private val DRAW_CHROMA_SHADOW: DrawStateFontRenderer = DrawStateFontRenderer(CHROMA_COLOR_SHADOW)
        @JvmStatic
        private val stringsWithChroma = MaxSizeHashMap<String?, Boolean?>(1000)

        @JvmStatic
        private var currentDrawState: DrawStateFontRenderer? = null
        @JvmStatic
        private var modInitialized = false

        @JvmStatic
        fun changeTextColor() {
            if (shouldRenderChroma() && currentDrawState != null && currentDrawState!!.shouldManuallyRecolorFont()) {
                val fontRenderer: FontRenderer = Minecraft.getMinecraft().fontRendererObj
                currentDrawState!!.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY)
            }
        }


        @JvmStatic
        fun setupFeatureFont(feature: Feature?) {
            if (SkyblockAddonsPlus.configValues!!.getChromaMode() === EnumUtils.ChromaMode.FADE &&
                SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(feature)
            ) {
                DRAW_CHROMA.setupMulticolorFeature(SkyblockAddonsPlus.configValues!!.getGuiScale(feature!!))
                DRAW_CHROMA_SHADOW.setupMulticolorFeature(
                    SkyblockAddonsPlus.configValues!!.getGuiScale(feature)
                )
            }
        }

        @JvmStatic
        fun endFeatureFont() {
            DRAW_CHROMA.endMulticolorFeature()
            DRAW_CHROMA_SHADOW.endMulticolorFeature()
        }

        /**
         * Called in patcher code to stop patcher optimization and do vanilla render
         * @param s string to render
         * @return true to override
         */
        @JvmStatic
        fun shouldOverridePatcher(s: String): Boolean {
            if (shouldRenderChroma()) {
                //return chromaStrings.get(s) == null || chromaStrings.get(s);
                if (stringsWithChroma[s] != null) {
                    return stringsWithChroma[s]!!
                }
                // Check if there is a "§z" colorcode in the string and cache it
                var hasChroma = false
                var i = 0
                while (i < s.length) {
                    if (s[i] == '§') {
                        i++
                        if (i < s.length && (s[i] == 'z' || s[i] == 'Z')) {
                            hasChroma = true
                            break
                        }
                    }
                    i++
                }
                stringsWithChroma[s] = hasChroma
                return hasChroma
            } else {
                return false
            }
        }


        /**
         * Called to save the current shader state
         */
        @JvmStatic
        fun beginRenderString(shadow: Boolean) {
            if (shouldRenderChroma()) {
                val alpha: Float = Minecraft.getMinecraft().fontRendererObj.alpha
                if (shadow) {
                    currentDrawState = DRAW_CHROMA_SHADOW
                    CHROMA_COLOR_SHADOW.setColor((255 * alpha).toInt() shl 24 or 0x555555)
                } else {
                    currentDrawState = DRAW_CHROMA
                    CHROMA_COLOR.setColor((255 * alpha).toInt() shl 24 or 0xFFFFFF)
                }

                currentDrawState!!.loadFeatureColorEnv()
            }
        }

        /**
         * Called to restore the saved chroma state
         */
        @JvmStatic
        fun restoreChromaState() {
            if (shouldRenderChroma()) {
                currentDrawState!!.restoreColorEnv()
            }
        }

        /**
         * Called to turn chroma on
         */
        @JvmStatic
        fun toggleChromaOn() {
            if (shouldRenderChroma()) {
                currentDrawState!!.newColorEnv().bindActualColor()
            }
        }

        /**
         * Called to turn chroma off after the full string has been rendered (before returning)
         */
        @JvmStatic
        fun endRenderString() {
            if (shouldRenderChroma()) {
                currentDrawState!!.endColorEnv()
            }
        }

        /**
         * Called by [SkyblockAddons.postInit]
         */
        @JvmStatic
        fun onModInitialized() {
            modInitialized = true
        }

        /**
         * Returns whether the methods for rendering chroma text should be run. They should be run only while the mod is
         * fully initialized and the player is playing Skyblock.
         *
         * @return `true` when the mod is fully initialized and the player is in Skyblock, `false` otherwise
         */
        @JvmStatic
        private fun shouldRenderChroma(): Boolean {
            return modInitialized && SkyblockAddonsPlus.utils!!.isOnSkyblock()
        }

        /**
         * HashMap with upper limit on storage size. Used to enforce the font renderer cache not getting too large over time
         */
        class MaxSizeHashMap<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>() {
            override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
                return size > maxSize
            }
        }
    }
}
