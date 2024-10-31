package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.asm.utils.ReturnValue

class EntityRendererHook {
    companion object{
        @JvmStatic
        fun onGetNightVisionBrightness(returnValue: ReturnValue<Float?>) {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            if (main.configValues!!.isEnabled(moe.ruruke.skyblock.core.Feature.AVOID_BLINKING_NIGHT_VISION)) {
                returnValue.cancel(1.0f)
            }
        }

        @JvmStatic
        fun onRenderScreenPre() {
            SkyblockAddonsPlus.guiManager!!.render()
        }
    }
}
