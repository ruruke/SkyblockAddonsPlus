package moe.ruruke.skyblock.gui

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.gui.buttons.ButtonColorBox
import moe.ruruke.skyblock.gui.buttons.NewButtonSlider
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.ColorUtils
import moe.ruruke.skyblock.utils.EnumUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException

class ColorSelectionGui internal constructor(
    feature: Feature, lastGUI: EnumUtils.GUIType, lastTab: EnumUtils.GuiTab?,
    private val lastPage: Int
) :
    GuiScreen() {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    private var COLOR_PICKER_IMAGE: BufferedImage? = null

    // The feature that this color is for.
    private val feature: Feature = feature

    // Previous pages for when they return.
    private val lastGUI: EnumUtils.GUIType = lastGUI

    private val lastTab: EnumUtils.GuiTab? = lastTab

    private var imageX = 0
    private var imageY = 0

    private var hexColorField: GuiTextField? = null

    private var chromaCheckbox: CheckBox? = null

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature The feature that this color is for.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     */
    init {
        try {
            COLOR_PICKER_IMAGE = TextureUtil.readBufferedImage(
                Minecraft.getMinecraft().getResourceManager().getResource(
                    COLOR_PICKER
                ).getInputStream()
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initGui() {
        chromaCheckbox = CheckBox(mc, width / 2 + 88, 170, 12, Translations.getMessage("messages.chroma"), false)

        chromaCheckbox!!.setValue(main.configValues!!.getChromaFeatures().contains(feature))


        chromaCheckbox!!.setOnToggleListener { value ->
            SkyblockAddonsPlus.configValues!!.setChroma(feature, value)
            this@ColorSelectionGui.removeChromaButtons()
            if (value) {
                this@ColorSelectionGui.addChromaButtons()
            }
        }

        hexColorField = GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, width / 2 + 110 - 50, 220, 100, 15)
        hexColorField!!.setMaxStringLength(7)
        hexColorField!!.setFocused(true)

        // Set the current color in the text box after creating it.
        setTextBoxHex(SkyblockAddonsPlus.configValues!!.getColor(feature))


        if (feature.getGuiFeatureData()!!.isColorsRestricted()) {
            // This creates the 16 buttons for all the color codes.

            var collumn = 1
            var x: Int = width / 2 - 160
            var y = 120

            for (colorCode in ColorCode.entries) {
                if (colorCode.isFormat() || colorCode === ColorCode.RESET) continue

                buttonList.add(ButtonColorBox(x, y, colorCode))

                if (collumn < 6) { // 6 buttons per row.
                    collumn++ // Go to the next collumn once the 6 are over.
                    x += ButtonColorBox.WIDTH + 15 // 15 spacing.
                } else {
                    y += ButtonColorBox.HEIGHT + 20 // Go to next row.
                    collumn = 1 // Reset the collumn.
                    x = width / 2 - 160 // Reset the x vlue.
                }
            }
        }

        if (main.configValues!!.getChromaFeatures().contains(feature) && !feature.getGuiFeatureData()!!.isColorsRestricted()) {
            addChromaButtons()
        }

        Keyboard.enableRepeatEvents(true)

        super.initGui()
    }
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Draw background and default text.
        val startColor = Color(0, 0, 0, 128).rgb
        val endColor = Color(0, 0, 0, 192).rgb
        drawGradientRect(0, 0, width, height, startColor, endColor)
        SkyblockAddonsGui.Companion.drawDefaultTitleText(this, 255)

        val defaultBlue: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue(255)

        if (feature.getGuiFeatureData() != null) {
            if (feature.getGuiFeatureData()!!.isColorsRestricted()) {
                SkyblockAddonsGui.drawScaledString(
                    this, Translations.getMessage("messages.chooseAColor"), 90,
                    defaultBlue, 1.5, 0
                )
            } else {
                val pickerWidth = COLOR_PICKER_IMAGE?.width ?: 300
                val pickerHeight = COLOR_PICKER_IMAGE?.height ?: 300

                imageX = width / 2 - 200
                imageY = 90
                if (main.configValues!!.getChromaFeatures().contains(feature)) { // Fade out color picker if chroma enabled
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 0.7F);
                    GlStateManager.enableBlend();
                } else {
                    GlStateManager.color(1f, 1f, 1f, 1f);
                }

                // Draw the color picker with no scaling so the size is the exact same.
                mc.getTextureManager().bindTexture(COLOR_PICKER)
                Gui.drawModalRectWithCustomSizedTexture(
                    imageX,
                    imageY,
                    0f,
                    0f,
                    pickerWidth,
                    pickerHeight,
                    pickerWidth.toFloat(),
                    pickerHeight.toFloat()
                )

                SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("messages.selectedColor"), 120, defaultBlue, 1.5, 75)

                Gui.drawRect(width / 2 + 90, 140, width / 2 + 130, 160, SkyblockAddonsPlus.configValues!!.getColor(feature))


                if (chromaCheckbox != null) chromaCheckbox!!.draw()

                if (!SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(feature))
                run {
                    // Disabled cause chroma is enabled
                    SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("messages.setHexColor"), 200, defaultBlue, 1.5, 75)
                    hexColorField!!.drawTextBox()
                }

                if (SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(feature));
                run {
                    SkyblockAddonsGui.drawScaledString(
                        this,
                        Translations.getMessage("settings.chromaSpeed"),
                        170 + 25,
                        defaultBlue,
                        1.0,
                        110
                    )
                    SkyblockAddonsGui.drawScaledString(
                        this,
                        Translations.getMessage("settings.chromaFadeWidth"),
                        170 + 35 + 25,
                        defaultBlue,
                        1.0,
                        110
                    )
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!feature.getGuiFeatureData()!!.isColorsRestricted() && !SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(feature))
        run {
            val xPixel = mouseX - imageX
            val yPixel = mouseY - imageY
            if( COLOR_PICKER_IMAGE == null ) return
            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE!!.width && yPixel > 0 && yPixel < COLOR_PICKER_IMAGE!!.height) {
                // Get the color of the clicked pixel.

                val selectedColor = COLOR_PICKER_IMAGE!!.getRGB(xPixel, yPixel)

                // Choose this color.
                if (ColorUtils.getAlpha(selectedColor) === 255) {
                    SkyblockAddonsPlus.configValues!!.setColor(feature, selectedColor)
                    setTextBoxHex(selectedColor)

                    SkyblockAddonsPlus.utils!!.playSound("gui.button.press", 0.25, 1.0)
                }
            }
            hexColorField!!.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (chromaCheckbox != null) chromaCheckbox!!.onMouseClick(mouseX, mouseY, mouseButton)

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun setTextBoxHex(color: Int) {
        hexColorField!!.setText(
            java.lang.String.format(
                "#%02x%02x%02x",
                ColorUtils.getRed(color),
                ColorUtils.getGreen(color),
                ColorUtils.getBlue(color)
            )
        )
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (hexColorField!!.isFocused()) {
            hexColorField!!.textboxKeyTyped(typedChar, keyCode)

            var text: String = hexColorField!!.getText()
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1)
            }

            if (text.length == 6) {
                val typedColor: Int
                try {
                    typedColor = text.toInt(16) // Try to read the hex value and put it in an integer.
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace() // This just means it wasn't in the format of a hex number- that's fine!
                    return
                }

                SkyblockAddonsPlus.configValues!!.setColor(feature, typedColor)
            }
        }
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        if (button is ButtonColorBox) {
            val colorBox: ButtonColorBox = button as ButtonColorBox
            SkyblockAddonsPlus.configValues!!.setChroma(feature, colorBox.getColor() === ColorCode.CHROMA)
            SkyblockAddonsPlus.configValues!!.setColor(feature, colorBox.getColor().getColor())
            this.mc.displayGuiScreen(null)
        }

        super.actionPerformed(button)
    }

    override fun updateScreen() {
        hexColorField!!.updateCursorCounter()

        super.updateScreen()
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)

        // Hardcode until feature refactor...
        //TODO:
//        if (feature === Feature.ENCHANTMENT_PERFECT_COLOR || feature === Feature.ENCHANTMENT_GREAT_COLOR || feature === Feature.ENCHANTMENT_GOOD_COLOR || feature === Feature.ENCHANTMENT_POOR_COLOR || feature === Feature.ENCHANTMENT_COMMA_COLOR) {
//            main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, Feature.ENCHANTMENT_LORE_PARSING)
//        } else {
//            main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, feature)
//        }
    }

    private fun removeChromaButtons() {
        this.buttonList.removeIf { button: GuiButton? -> button is NewButtonSlider }
    }

    private fun addChromaButtons() {
        //TODO: Chroma滅べ
        //buttonList.add(NewButtonSlider((width / 2 + 76).toDouble(), (170 + 35).toDouble(), 70.0, 15.0, SkyblockAddonsPlus.configValues!! getChromaSpeed().setValue(updatedValue)
//        buttonList.add(NewButtonSlider((width / 2 + 76).toDouble(), (170 + 35 + 35).toDouble(), 70, 15, SkyblockAddonsPlus.configValues!!.getChromaSize().setValue(updatedValue)))
    }

    companion object {
        private val COLOR_PICKER = ResourceLocation("skyblockaddonsplus", "gui/colorpicker.png")
    }
}
