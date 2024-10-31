package moe.ruruke.skyblock.asm.hooks

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.item.ItemStack


class RenderItemHook {
    companion object {
        @JvmStatic
        private val BLANK = net.minecraft.util.ResourceLocation("skyblockaddons", "blank.png")

        @JvmStatic
        fun renderToxicArrowPoisonEffect(model: IBakedModel, stack: ItemStack) {
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

            if (main.utils!!.isOnSkyblock() && main.configValues!!
                    .isEnabled(moe.ruruke.skyblock.core.Feature.TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON)
                && main.inventoryUtils
                !!.isUsingToxicArrowPoison() && net.minecraft.init.Items.bow == stack.getItem() && main.utils!!
                    .itemIsInHotbar(stack)
            ) {
                val textureManager: TextureManager = Minecraft.getMinecraft().getTextureManager()

                GlStateManager.depthMask(false)
                GlStateManager.depthFunc(514)
                GlStateManager.disableLighting()
                GlStateManager.blendFunc(768, 1)
                textureManager.bindTexture(BLANK)
                GlStateManager.matrixMode(5890)

                GlStateManager.pushMatrix()

                Minecraft.getMinecraft().getRenderItem().renderModel(model, 0x201cba41)
                GlStateManager.popMatrix()

                GlStateManager.matrixMode(5888)
                GlStateManager.blendFunc(770, 771)
                GlStateManager.enableLighting()
                GlStateManager.depthFunc(515)
                GlStateManager.depthMask(true)
                textureManager.bindTexture(TextureMap.locationBlocksTexture)
            }
        }
    }
}
