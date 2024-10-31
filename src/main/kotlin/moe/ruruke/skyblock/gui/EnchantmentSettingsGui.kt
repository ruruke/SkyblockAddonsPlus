//package moe.ruruke.skyblock.gui
//
//import moe.ruruke.skyblock.utils.EnumUtils.*
//import moe.ruruke.skyblock.core.Feature
//import org.lwjgl.input.Keyboard
//import java.awt.Color
//import java.io.IOException
//import java.util.*
//import kotlin.math.max
//
//class EnchantmentSettingsGui(
//    feature: Feature,
//    page: Int,
//    lastPage: Int,
//    lastTab: GuiTab?,
//    settings: List<FeatureSetting?>
//) :
//    SettingsGui(feature, page, lastPage, lastTab, settings) {
//    private val ENCHANT_COLORING: List<FeatureSetting> = Arrays.asList<FeatureSetting>(
//        FeatureSetting.HIGHLIGHT_ENCHANTMENTS,
//        FeatureSetting.PERFECT_ENCHANT_COLOR, FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR,
//        FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR
//    )
//    private val ORGANIZATION: List<FeatureSetting> = Arrays.asList<FeatureSetting>(
//        FeatureSetting.ENCHANT_LAYOUT,
//        FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS
//    )
//
//
//    private var maxPage = 1
//
//    init {
//        for (setting in settings) {
//            if (!(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
//                maxPage = 2
//                break
//            }
//        }
//    }
//
//    override fun initGui() {
//        Keyboard.enableRepeatEvents(true)
//        row = 1f
//        column = 1
//        buttonList.clear()
//        for (setting in settings!!) {
//            if (page == 0) {
//                if (ORGANIZATION.contains(setting)) {
//                    addButton(setting)
//                }
//            }
//            if (page == 1) {
//                if (ENCHANT_COLORING.contains(setting)) {
//                    addButton(setting)
//                }
//            } else if (page == 2 &&
//                !(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))
//            ) {
//                addButton(setting)
//            }
//        }
//        buttonList.add(
//            ButtonArrow(
//                width / 2 - 15 - 150,
//                height - 70,
//                SettingsGui.Companion.main,
//                ButtonArrow.ArrowType.LEFT,
//                page == 0
//            )
//        )
//        buttonList.add(
//            ButtonArrow(
//                width / 2 - 15 + 150,
//                height - 70,
//                SettingsGui.Companion.main,
//                ButtonArrow.ArrowType.RIGHT,
//                page == maxPage
//            )
//        )
//    }
//
//
//    private fun findDisplayCount(): Int {
//        val maxX: Int = ScaledResolution(mc).getScaledHeight() - 70 - 25
//        var displayCount = 0
//        for (row in 1..98) {
//            if (getRowHeight(row.toDouble()) < maxX) {
//                displayCount += 3
//            } else {
//                return displayCount
//            }
//        }
//        return displayCount
//    }
//
//    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
//        if (this.reInit) {
//            this.reInit = false
//            this.initGui()
//        }
//        val timeSinceOpen = System.currentTimeMillis() - timeOpened
//        var alphaMultiplier: Float // This all calculates the alpha for the fade-in effect.
//        alphaMultiplier = 0.5f
//        if (SkyblockAddonsPlus.utils);
//        !!
//        isFadingIn()
//        run {
//            val fadeMilis = 500
//            if (timeSinceOpen <= fadeMilis) {
//                alphaMultiplier = timeSinceOpen.toFloat() / (fadeMilis * 2)
//            }
//        }
//        var alpha = (255 * alphaMultiplier).toInt() // Alpha of the text will increase from 0 to 127 over 500ms.
//
//        val startColor = Color(0, 0, 0, (alpha * 0.5).toInt()).rgb
//        val endColor = Color(0, 0, 0, alpha).rgb
//        drawGradientRect(0, 0, width, height, startColor, endColor)
//        GlStateManager.enableBlend()
//
//        if (alpha < 4) alpha = 4 // Text under 4 alpha appear 100% transparent for some reason o.O
//
//        val defaultBlue: Int = SkyblockAddonsPlus.utils
//        !!
//        getDefaultBlue(alpha * 2)
//
//        SkyblockAddonsGui.Companion.drawDefaultTitleText(this, alpha * 2)
//
//        if (feature !== Feature.LANGUAGE) {
//            val halfWidth = width / 2
//            val boxWidth = 140
//            val x = halfWidth - 90 - boxWidth
//            var width = halfWidth + 90 + boxWidth
//            width -= x
//            val numSettings = if (page == 0) {
//                ORGANIZATION.size.toFloat()
//            } else if (page == 1) {
//                ENCHANT_COLORING.size.toFloat()
//            } else {
//                max((settings!!.size - ORGANIZATION.size - ENCHANT_COLORING.size).toDouble(), 1.0).toFloat()
//            }
//            val height = (getRowHeightSetting(numSettings.toDouble()) - 50).toInt()
//            val y = getRowHeight(1.0).toInt()
//            GlStateManager.enableBlend()
//            DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4)
//
//            drawScaledString(this, Translations.getMessage("settings.settings"), 110, defaultBlue, 1.5, 0)
//        }
//        super.drawScreen(mouseX, mouseY, partialTicks) // Draw buttons.
//    }
//
//    /**
//     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
//     */
//    override fun actionPerformed(abstractButton: GuiButton) {
//        if (abstractButton is ButtonLanguage) {
//            val language: Language = (abstractButton as ButtonLanguage).getLanguage()
//            SkyblockAddonsPlus.configValues
//            !!
//            setLanguage(language)
//            DataUtils.loadLocalizedStrings(false)
//            SettingsGui.Companion.main.setKeyBindingDescriptions()
//            returnToGui()
//        } else if (abstractButton is ButtonSwitchTab) {
//            val tab: ButtonSwitchTab = abstractButton as ButtonSwitchTab
//            mc.displayGuiScreen(SkyblockAddonsGui(1, tab.getTab()))
//        } else if (abstractButton is ButtonOpenColorMenu) {
//            closingGui = true
//            // Temp fix until feature re-write. Open a color selection panel specific to the color setting
//            val f: Feature = (abstractButton as ButtonOpenColorMenu).feature
//            if (f === Feature.ENCHANTMENT_PERFECT_COLOR || f === Feature.ENCHANTMENT_GREAT_COLOR || f === Feature.ENCHANTMENT_GOOD_COLOR || f === Feature.ENCHANTMENT_POOR_COLOR || f === Feature.ENCHANTMENT_COMMA_COLOR) {
//                mc.displayGuiScreen(ColorSelectionGui(f, EnumUtils.GUIType.SETTINGS, lastTab, page))
//            } else {
//                mc.displayGuiScreen(ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, lastTab, lastPage))
//            }
//        } else if (abstractButton is ButtonToggleTitle) {
//            val button: ButtonFeature = abstractButton as ButtonFeature
//            val feature: Feature = button.feature ?: return
//            feature.setEnabled(!SkyblockAddonsPlus.configValues)
//            !!
//            isEnabled(feature)
//        } else if (abstractButton is ButtonArrow) {
//            val arrow: ButtonArrow = abstractButton as ButtonArrow
//            if (arrow.isNotMax()) {
//                SkyblockAddonsPlus.utils
//                !!
//                setFadingIn(false)
//                if (arrow.getArrowType() === ButtonArrow.ArrowType.RIGHT) {
//                    closingGui = true
//                    mc.displayGuiScreen(EnchantmentSettingsGui(feature, ++page, lastPage, lastTab, settings!!))
//                } else {
//                    closingGui = true
//                    mc.displayGuiScreen(EnchantmentSettingsGui(feature, --page, lastPage, lastTab, settings!!))
//                }
//            }
//        }
//    }
//
//    private fun addLanguageButton(language: Language) {
//        if (displayCount == 0) return
//        val text: String = feature.getMessage()
//        val halfWidth = width / 2
//        val boxWidth = 140
//        var x = 0
//        if (column == 1) {
//            x = halfWidth - 90 - boxWidth
//        } else if (column == 2) {
//            x = halfWidth - (boxWidth / 2)
//        } else if (column == 3) {
//            x = halfWidth + 90
//        }
//        val y = getRowHeight(row.toDouble())
//        buttonList.add(ButtonLanguage(x, y, text, language))
//        column++
//        if (column > 3) {
//            column = 1
//            row++
//        }
//        displayCount--
//    }
//
//    private fun addButton(setting: FeatureSetting) {
//        val halfWidth = width / 2
//        var boxWidth = 100
//        var x = halfWidth - (boxWidth / 2)
//        var y = getRowHeightSetting(row.toDouble())
//        if (setting === FeatureSetting.COLOR) {
//            buttonList.add(
//                ButtonOpenColorMenu(
//                    x,
//                    y,
//                    100,
//                    20,
//                    Translations.getMessage("settings.changeColor"),
//                    SettingsGui.Companion.main,
//                    feature
//                )
//            )
//            // Temp hardcode until feature rewrite
//        } else if (setting === FeatureSetting.PERFECT_ENCHANT_COLOR || setting === FeatureSetting.GREAT_ENCHANT_COLOR || setting === FeatureSetting.GOOD_ENCHANT_COLOR || setting === FeatureSetting.POOR_ENCHANT_COLOR || setting === FeatureSetting.COMMA_ENCHANT_COLOR) {
//            buttonList.add(
//                ButtonOpenColorMenu(
//                    x,
//                    y,
//                    100,
//                    20,
//                    setting.getMessage(),
//                    SettingsGui.Companion.main,
//                    setting.getFeatureEquivalent()
//                )
//            )
//        } else if (setting === FeatureSetting.ENCHANT_LAYOUT) {
//            boxWidth = 140
//            x = halfWidth - (boxWidth / 2)
//            val currentStatus: EnchantListLayout = SkyblockAddonsPlus.configValues
//            !!
//            getEnchantLayout()
//
//            buttonList.add(
//                ButtonTextNew(
//                    halfWidth,
//                    y.toInt() - 10,
//                    Translations.getMessage("enchantLayout.title"),
//                    true,
//                    -0x1
//                )
//            )
//            buttonList.add(
//                ButtonSelect(
//                    x,
//                    y.toInt(),
//                    boxWidth,
//                    20,
//                    Arrays.asList(EnchantListLayout.values()),
//                    currentStatus.ordinal()
//                ) { index ->
//                    val enchantLayout: EnchantListLayout = EnchantListLayout.values().get(index)
//                    SkyblockAddonsPlus.configValues
//                    !!
//                    setEnchantLayout(enchantLayout)
//                    reInit = true
//                })
//
//            row += 0.4.toFloat()
//        } else {
//            boxWidth = 31 // Default size and stuff.
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(
//                ButtonToggleTitle(
//                    x,
//                    y,
//                    setting.getMessage(),
//                    SettingsGui.Companion.main,
//                    setting.getFeatureEquivalent()
//                )
//            )
//        }
//        row++
//    }
//
//    // Each row is spaced 0.08 apart, starting at 0.17.
//    private fun getRowHeight(row: Double): Double {
//        var row = row
//        row--
//        return 95 + (row * 30) //height*(0.18+(row*0.08));
//    }
//
//    private fun getRowHeightSetting(row: Double): Double {
//        var row = row
//        row--
//        return 140 + (row * 35) //height*(0.18+(row*0.08));
//    }
//
//    override fun onGuiClosed() {
//        if (!closingGui) {
//            returnToGui()
//        }
//        Keyboard.enableRepeatEvents(false)
//    }
//
//    private fun returnToGui() {
//        closingGui = true
//        SettingsGui.Companion.main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab)
//    }
//
//    @Throws(IOException::class)
//    override fun keyTyped(typedChar: Char, keyCode: Int) {
//        super.keyTyped(typedChar, keyCode)
//        ButtonInputFieldWrapper.callKeyTyped(buttonList, typedChar, keyCode)
//    }
//
//    @Throws(IOException::class)
//    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
//        super.mouseClicked(mouseX, mouseY, mouseButton)
//    }
//
//    override fun updateScreen() {
//        super.updateScreen()
//        ButtonInputFieldWrapper.callUpdateScreen(buttonList)
//    }
//}
