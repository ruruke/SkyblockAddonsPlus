package moe.ruruke.skyblock.gui.buttons

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Language
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.data.DataUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import org.apache.logging.log4j.Logger
import java.awt.Color

class ButtonLanguage(x: Double, y: Double, buttonText: String?, language: Language) :
    GuiButton(0, x.toInt(), y.toInt(), buttonText) {
    private val language: Language = language
    private val languageName: String

    private var flagResourceExceptionTriggered: Boolean

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    init {
        DataUtils.loadLocalizedStrings(language, false)
        this.languageName = Translations.getMessage("language")!!
        this.width = 140
        this.height = 25
        flagResourceExceptionTriggered = false
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            DrawUtils.drawRect(
                xPosition.toDouble(),
                yPosition.toDouble(),
                width.toDouble(),
                height.toDouble(),
                ColorUtils.getDummySkyblockColor(28, 29, 41, 230),
                4
            )

            GlStateManager.color(1f, 1f, 1f, 1f)
            try {
                mc.getTextureManager().bindTexture(language.getResourceLocation())
                DrawUtils.drawModalRectWithCustomSizedTexture(
                    (xPosition + width - 32).toFloat(),
                    yPosition.toFloat(),
                    0f,
                    0f,
                    30f,
                    26f,
                    30f,
                    26f,
                    true
                )
            } catch (ex: Exception) {
                if (!flagResourceExceptionTriggered) {
                    flagResourceExceptionTriggered = true
                    logger.catching(ex)
                }
            }

            hovered =
                mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height

            var fontColor: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue(255)
            if (hovered) {
                fontColor = Color(255, 255, 160, 255).rgb
            }
            drawString(mc.fontRendererObj, languageName, xPosition + 5, yPosition + 10, fontColor)
        }
    }

    fun getLanguage(): Language {
        return language
    }

    companion object {
        private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        private val logger: Logger = main.getLogger()
    }
}
