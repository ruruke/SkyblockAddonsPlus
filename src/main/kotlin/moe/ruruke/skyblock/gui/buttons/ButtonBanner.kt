package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Logger
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ButtonBanner(x: Double, y: Double) : GuiButton(0, x.toInt(), y.toInt(), "") {
    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        if (!grabbedBanner) {
            grabbedBanner = true
            bannerImage = null
            banner = null

            SkyblockAddonsPlus.runAsync {
                try {
                    val url =
                        URL(main.getOnlineData()!!.getBannerImageURL())
                    val connection = url.openConnection() as HttpURLConnection
                    connection.readTimeout = 5000
                    connection.addRequestProperty("User-Agent", Utils.USER_AGENT)

                    bannerImage = TextureUtil.readBufferedImage(connection.inputStream)

                    connection.disconnect()

                    this.width = bannerImage!!.width
                    this.height = bannerImage!!.height
                } catch (ex: IOException) {
                    logger.warn(
                        "Couldn't grab main menu banner image from URL, falling back to local banner.",
                        ex
                    )
                }
            }
        }

        xPosition -= WIDTH / 2

        if (bannerImage != null) {
            this.width = bannerImage!!.width
            this.height = bannerImage!!.height
        }
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (bannerImage != null && banner == null) { // This means it was just loaded from the URL above.
            banner = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                "banner", DynamicTexture(
                    bannerImage
                )
            )
        }

        if (banner != null) { // Could have not been loaded yet.
            var alphaMultiplier = 1f
            if (SkyblockAddonsPlus.utils!!.isFadingIn()) {
                val timeSinceOpen = System.currentTimeMillis() - timeOpened
                val fadeMilis = 500
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = timeSinceOpen.toFloat() / fadeMilis
                }
            }

            val scale = WIDTH.toFloat() / bannerImage!!.width // max width

            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition +
                    WIDTH && mouseY < yPosition + bannerImage!!.height * scale
            GlStateManager.enableBlend()

            if (hovered) {
                GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 1)
            } else {
                GlStateManager.color(1f, 1f, 1f, alphaMultiplier * 0.8f)
            }

            mc.getTextureManager().bindTexture(banner)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, 1f)
            Gui.drawModalRectWithCustomSizedTexture(
                Math.round(xPosition / scale),
                Math.round(yPosition / scale), 0f, 0f, width, height, width.toFloat(), height.toFloat()
            )
            GlStateManager.popMatrix()
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return hovered
    }

    companion object {
        private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        private val logger: Logger = main.getLogger()
        private const val WIDTH = 130

        private var banner: ResourceLocation? = null
        private var bannerImage: BufferedImage? = null

        private var grabbedBanner = false
    }
}
