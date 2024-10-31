package moe.ruruke.skyblock.gui.buttons


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.gui.SkyblockAddonsGui
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.objects.IntPair
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

class ButtonNormal(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    buttonText: String?,
    main: SkyblockAddonsPlus.Companion,
    feature: Feature?
) :
    ButtonFeature(0, x.toInt(), y.toInt(), buttonText, feature) {
    private val main: SkyblockAddonsPlus.Companion = main

    // Used to calculate the transparency when fading in.
    private val timeOpened = System.currentTimeMillis()

    /**
     * Create a button for toggling a feature on or off. This includes all the [Feature]s that have a proper ID.
     */
    constructor(
        x: Double,
        y: Double,
        buttonText: String?,
        main: SkyblockAddonsPlus.Companion,
        feature: Feature?
    ) : this(
        x.toInt().toDouble(), y.toInt().toDouble(), 140, 50, buttonText, main, feature
    )

    init {
        this.feature = feature
        this.width = width
        this.height = height
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            var alpha: Int
            var alphaMultiplier = 1f
            if (SkyblockAddonsPlus.utils!!.isFadingIn()) {
                val timeSinceOpen = System.currentTimeMillis() - timeOpened
                val fadeMilis = 500
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = timeSinceOpen.toFloat() / fadeMilis
                }
                alpha = (255 * alphaMultiplier).toInt()
            } else {
                alpha = 255
            }
            hovered =
                mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height
            if (alpha < 4) alpha = 4
            var fontColor: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue(alpha)
            if (SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature)) {
                fontColor = Color(60, 60, 60).rgb
            }
            GlStateManager.enableBlend()
            GlStateManager.color(1f, 1f, 1f, 0.7f)
            if (SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature)) {
                GlStateManager.color(0.3f, 0.3f, 0.3f, 0.7f)
            }
            DrawUtils.drawRect(
                xPosition.toDouble(),
                yPosition.toDouble(),
                width.toDouble(),
                height.toDouble(),
                ColorUtils.getDummySkyblockColor(27, 29, 41, 230),
                4
            )

            val creditFeature: EnumUtils.FeatureCredit? = EnumUtils.FeatureCredit.fromFeature(feature)

            // Wrap the feature name into 2 lines.
            var wrappedString: Array<String> = SkyblockAddonsPlus.utils!!.wrapSplitText(displayString, 28)
            if (wrappedString.size > 2) { // If it makes more than 2 lines,
                val lastLineString = StringBuilder() // combine all the last
                for (i in 1 until wrappedString.size) { // lines and combine them
                    lastLineString.append(wrappedString[i]) // back into the second line.
                    if (i != wrappedString.size - 1) {
                        lastLineString.append(" ")
                    }
                }

                wrappedString = arrayOf(wrappedString[0], lastLineString.toString())
            }

            val textX = xPosition + width / 2
            var textY = yPosition

            val multiline = wrappedString.size > 1

            for (i in wrappedString.indices) {
                val line = wrappedString[i]

                var scale = 1f
                val stringWidth: Int = mc.fontRendererObj.getStringWidth(line)
                var widthLimit: Float = (SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10).toFloat()
                if (feature === Feature.WARNING_TIME) {
                    widthLimit = 90f
                }
                if (stringWidth > widthLimit) {
                    scale = 1 / (stringWidth / widthLimit)
                }
                if (feature === Feature.GENERAL_SETTINGS) textY -= 5

                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 1f)
                var offset = 9
                if (creditFeature != null) offset -= 4
                offset =
                    (offset + (10 - 10 * scale)).toInt() // If the scale is small gotta move it down a bit or else its too mushed with the above line.
                DrawUtils.drawCenteredText(line, (textX / scale), (textY / scale) + offset, fontColor)
                GlStateManager.popMatrix()

                // If its not the last line, add to the Y.
                if (multiline && i == 0) {
                    textY += 10
                }
            }

            if (creditFeature != null) {
                var scale = 0.8f
                if (multiline) { // If its 2 lines the credits have to be smaller.
                    scale = 0.6f
                }
                var creditsY = (textY / scale) + 23
                if (multiline) {
                    creditsY += 3f // Since its smaller the scale is wierd to move it down a tiny bit.
                }

                GlStateManager.pushMatrix()
                GlStateManager.scale(scale, scale, 1f)
                DrawUtils.drawCenteredText(creditFeature.getAuthor(), (textX / scale), creditsY, fontColor)
                GlStateManager.disableBlend()
                GlStateManager.popMatrix()
            }

            if (feature === Feature.LANGUAGE) {
                GlStateManager.color(1f, 1f, 1f, 1f)
                try {
                    mc.getTextureManager()
                        .bindTexture(SkyblockAddonsPlus.configValues!!.getLanguage().getResourceLocation())
                    if (SkyblockAddonsPlus.utils!!.isHalloween()) {
                        mc.getTextureManager().bindTexture(ResourceLocation("skyblockaddonsplus", "flags/halloween.png"))
                    }
                    DrawUtils.drawModalRectWithCustomSizedTexture(
                        xPosition + width / 2f - 20,
                        yPosition + 20f,
                        0f,
                        0f,
                        38f,
                        30f,
                        38f,
                        30f,
                        true
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else if (feature === Feature.EDIT_LOCATIONS) {
                GlStateManager.color(1f, 1f, 1f, 1f)
                try {
                    mc.getTextureManager().bindTexture(ResourceLocation("skyblockaddonsplus", "gui/move.png"))
                    DrawUtils.drawModalRectWithCustomSizedTexture(
                        xPosition + width / 2f - 12,
                        yPosition + 22f,
                        0f,
                        0f,
                        25f,
                        25f,
                        25f,
                        25f,
                        true
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            if (SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature)) {
                drawCenteredString(
                    mc.fontRendererObj,
                    Translations.getMessage("messages.featureDisabled"),
                    textX,
                    textY + 6,
                    SkyblockAddonsPlus.utils!!.getDefaultBlue(alpha)
                )
            }
        }
    }

    fun getCreditsCoords(credit: EnumUtils.FeatureCredit): IntPair {
        val wrappedString: Array<String> = SkyblockAddonsPlus.utils!!.wrapSplitText(displayString, 28)
        val multiLine = wrappedString.size > 1

        var scale = 0.8f
        if (multiLine) { // If its 2 lines the credits have to be smaller.
            scale = 0.6f
        }

        var y =
            ((yPosition / scale) + (if (multiLine) 30 else 21)).toInt() // If its a smaller scale, you gotta move it down more.

        if (multiLine) { // When there's multiple lines the second line is moved 10px down.
            y += 10
        }

        val x: Int =
            ((xPosition + width / 2) / scale).toInt() - Minecraft.getMinecraft().fontRendererObj.getStringWidth(credit.getAuthor()) / 2 - 17
        return IntPair(x, y)
    }

    val isMultilineButton: Boolean
        get() {
            val wrappedString: Array<String> = SkyblockAddonsPlus.utils!!.wrapSplitText(displayString, 28)
            return wrappedString.size > 1
        }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        if (feature === Feature.LANGUAGE || feature === Feature.EDIT_LOCATIONS || feature === Feature.GENERAL_SETTINGS) {
            super.playPressSound(soundHandlerIn)
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        if (feature === Feature.LANGUAGE || feature === Feature.EDIT_LOCATIONS || feature === Feature.GENERAL_SETTINGS) {
            return super.mousePressed(mc, mouseX, mouseY)
        }
        return false
    }
}
