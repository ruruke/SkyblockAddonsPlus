package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.shader.ShaderManager
import net.minecraft.client.renderer.Tessellator

class WorldVertexBufferUploaderHook {
    companion object {
        @JvmStatic
        fun onRenderWorldRendererBuffer(): Boolean {
//        if (true) return false;
            val canceled: Boolean = ShaderManager.instance.onRenderWorldRendererBuffer()
            if (canceled) {
                Tessellator.getInstance().worldRenderer.reset()
            }
            return canceled
        }
    }
}
