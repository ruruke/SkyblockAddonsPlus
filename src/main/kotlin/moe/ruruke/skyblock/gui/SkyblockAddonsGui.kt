package moe.ruruke.skyblock.gui

import moe.ruruke.skyblock.gui.buttons.ButtonModify
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.gui.buttons.*
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.DrawUtils
import moe.ruruke.skyblock.utils.EnumUtils
import moe.ruruke.skyblock.utils.EnumUtils.*
import moe.ruruke.skyblock.utils.objects.IntPair
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.GuiIngameForge
import org.apache.commons.lang3.StringUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.util.*

class SkyblockAddonsGui(page: Int, tab: GuiTab) : GuiScreen() {
    private var featureSearchBar: GuiTextField? = null
    private val tab: GuiTab = tab
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
    private var page: Int
    private var row = 1
    private var collumn = 1
    private var displayCount = 0

    private val timeOpened = System.currentTimeMillis()

    private var cancelClose = false

    /**
     * The main gui, opened with /sba.
     */
    init {
        this.page = page
    }

    override fun initGui() {
        row = 1
        collumn = 1
        displayCount = findDisplayCount()
        addLanguageButton()
        addEditLocationsButton()
        addFeaturedBanner()
        addGeneralSettingsButton()

        if (featureSearchBar == null) {
            featureSearchBar = GuiTextField(2, this.fontRendererObj, width / 2 - 220, 69, 120, 15)
            featureSearchBar!!.setMaxStringLength(500)
            featureSearchBar!!.setFocused(true)

            if (searchString != null) {
                featureSearchBar!!.setText(searchString)
            }
        } else {
            featureSearchBar!!.xPosition = width / 2 - 220
        }

        // Add the buttons for each page.
        val features: TreeSet<Feature> = sortedSetOf()//sortedMapOf(Comparator.comparing<Any, Any>(Feature::getMessage))//TreeSet<Feature>(Comparator.comparing<Any, Any>(Feature::getMessage))
        //TODO:
//        for (feature in if (tab !== EnumUtils.GuiTab.GENERAL_SETTINGS) Sets.newHashSet(Feature.values()) else Feature.getGeneralTabFeatures()) {
//            if ((feature.isActualFeature() || tab === EnumUtils.GuiTab.GENERAL_SETTINGS) && !SkyblockAddonsPlus.configValues);
//            !!
//            isRemoteDisabled(feature)
//            run { // Don't add disabled features yet
//                if (matchesSearch(feature.getMessage())) { // Matches search.
//                    features.add(feature)
//                } else { // If a sub-setting matches the search show it up in the results as well.
//                    for (setting in feature.getSettings()) {
//                        try {
//                            if (matchesSearch(setting.getMessage())) {
//                                features.add(feature)
//                            }
//                        } catch (ignored: Exception) {
//                        } // Hit a message that probably needs variables to fill in, just skip it.
//                    }
//                }
//            }
//        }
//
//        if (tab !== EnumUtils.GuiTab.GENERAL_SETTINGS) {
//            for (feature in Feature.values()) if (SkyblockAddonsPlus.configValues);
//            !!
//            isRemoteDisabled(feature) && matchesSearch(feature.getMessage())
//            features.add(feature) // add disabled features at the end
//        }

        var skip = (page - 1) * displayCount

        var max = page == 1
        buttonList.add(ButtonArrow(width / 2.0 - 15.0 - 50.0, height - 70.0, main, ButtonArrow.ArrowType.LEFT, max))
        max = features.size - skip - displayCount <= 0
        buttonList.add(ButtonArrow(width / 2.0 - 15.0 + 50.0, height - 70.0, main, ButtonArrow.ArrowType.RIGHT, max))

        buttonList.add(ButtonSocial(width / 2.0 + 200.0, 30.0, main, EnumUtils.Social.YOUTUBE))
        buttonList.add(ButtonSocial(width / 2.0 + 175.0, 30.0, main, EnumUtils.Social.DISCORD))
        buttonList.add(ButtonSocial(width / 2.0 + 150.0, 30.0, main, EnumUtils.Social.GITHUB))

        // buttonList.add(new ButtonSocial(width / 2 + 125, 30, main, EnumUtils.Social.PATREON));
        for (feature in features) {
            if (skip == 0) {
                if (feature === Feature.TEXT_STYLE || feature === Feature.WARNING_TIME || feature === Feature.CHROMA_MODE || feature === Feature.TURN_ALL_FEATURES_CHROMA) {
                    addButton(feature, EnumUtils.ButtonType.SOLID)
                } else if (feature === Feature.CHROMA_SPEED || feature === Feature.CHROMA_SIZE || feature === Feature.CHROMA_SATURATION || feature === Feature.CHROMA_BRIGHTNESS) {
                    addButton(feature, EnumUtils.ButtonType.CHROMA_SLIDER)
                } else {
                    addButton(feature, EnumUtils.ButtonType.TOGGLE)
                }
            } else {
                skip--
            }
        }
        Keyboard.enableRepeatEvents(true)
    }

    private fun matchesSearch(textToSearch: String): Boolean {
        var textToSearch = textToSearch
        val searchBarText: String = featureSearchBar!!.getText()
        if (searchBarText == null || searchBarText.isEmpty()) return true

        val searchTerms =
            searchBarText.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        textToSearch = textToSearch.lowercase(Locale.getDefault())

        for (searchTerm in searchTerms) {
            if (!textToSearch.contains(searchTerm)) {
                return false
            }
        }

        return true
    }

    private fun findDisplayCount(): Int {
        val maxX: Int = ScaledResolution(mc).getScaledHeight() - 70 - 50
        var displayCount = 0
        for (row in 1..98) {
            if (getRowHeight(row.toDouble()) < maxX) {
                displayCount += 3
            } else {
                return displayCount
            }
        }
        return displayCount
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
//        tooltipFeature = null;
        val timeSinceOpen = System.currentTimeMillis() - timeOpened
        var alphaMultiplier: Float // This all calculates the alpha for the fade-in effect.
        alphaMultiplier = 0.5f
        if (SkyblockAddonsPlus.utils!!.isFadingIn())
        run {
            val fadeMilis = 500
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = timeSinceOpen.toFloat() / (fadeMilis * 2)
            }
        }
        var alpha = (255 * alphaMultiplier).toInt() // Alpha of the text will increase from 0 to 127 over 500ms.

        val startColor = Color(0, 0, 0, (alpha * 0.5).toInt()).rgb
        val endColor = Color(0, 0, 0, alpha).rgb
        drawGradientRect(0, 0, width, height, startColor, endColor)
        GlStateManager.enableBlend()

        if (alpha < 4) alpha = 4 // Text under 4 alpha appear 100% transparent for some reason o.O


        drawDefaultTitleText(this, alpha * 2)

        featureSearchBar!!.drawTextBox()
        if (StringUtils.isEmpty(featureSearchBar!!.getText())) {
            Minecraft.getMinecraft().fontRendererObj.drawString(
                Translations.getMessage("messages.searchFeatures"),
                featureSearchBar!!.xPosition + 4,
                featureSearchBar!!.yPosition + 3,
                ColorCode.DARK_GRAY.getColor()
            )
        }

        super.drawScreen(mouseX, mouseY, partialTicks) // Draw buttons.
    }

    /**
     * Code to perform the button toggles, openings of other guis/pages, and language changes.
     */
    override fun actionPerformed(abstractButton: GuiButton) {
        if (abstractButton is ButtonFeature) {
            val feature: Feature = (abstractButton as ButtonFeature).feature!!
            if (abstractButton is ButtonSettings) {
                SkyblockAddonsPlus.utils!!.setFadingIn(false)
                if ((abstractButton as ButtonSettings).feature === Feature.ENCHANTMENT_LORE_PARSING) {
                    //TODO: Disabned Log
//                    Minecraft.getMinecraft()
//                        .displayGuiScreen(EnchantmentSettingsGui(feature, 0, page, tab, feature.getSettings()))
                } else {
                    //TODO: Disabned Log
//                    Minecraft.getMinecraft().displayGuiScreen(SettingsGui(feature, 1, page, tab, feature.getSettings()))
                }
                return
            }
            if (feature === Feature.LANGUAGE) {
                //TODO: Disabned Log
//                SkyblockAddonsPlus.utils!!.setFadingIn(false)
//                Minecraft.getMinecraft().displayGuiScreen(SettingsGui(Feature.LANGUAGE, 1, page, tab, null))
            } else if (feature === Feature.EDIT_LOCATIONS) {
                //TODO: Disabned Log
//                SkyblockAddonsPlus.utils!!.setFadingIn(false)
//                Minecraft.getMinecraft().displayGuiScreen(LocationEditGui(page, tab))
            } else if (feature === Feature.GENERAL_SETTINGS) {
                if (tab === EnumUtils.GuiTab.GENERAL_SETTINGS) {
                    SkyblockAddonsPlus.utils!!.setFadingIn(false)
                    Minecraft.getMinecraft().displayGuiScreen(SkyblockAddonsGui(1, EnumUtils.GuiTab.MAIN))
                } else {
                    SkyblockAddonsPlus.utils!!.setFadingIn(false)
                    Minecraft.getMinecraft().displayGuiScreen(SkyblockAddonsGui(1, EnumUtils.GuiTab.GENERAL_SETTINGS))
                }
            } else if (abstractButton is ButtonToggle) {
                if (NewConfig.isRemoteDisabled(feature)) return
                if (NewConfig.isDisabled(feature))
                run {
                    feature.setEnabled(true)
                    if (feature === Feature.DISCORD_RPC && SkyblockAddonsPlus.utils!!.isOnSkyblock())
                    run {
                        //TODO Disabled
//                        main.getDiscordRPCManager().start()
                    }
                    if (feature === Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT) {
                        Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT.setEnabled(true)
                    }
                }
                run {
                    feature.setEnabled(false)
                    if (feature === Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                        GuiIngameForge.renderArmor = true // The food gets automatically enabled, no need to include it.
                    } else if (feature === Feature.HIDE_HEALTH_BAR) {
                        GuiIngameForge.renderHealth = true
                    } else if (feature === Feature.FULL_INVENTORY_WARNING) {
                        main.inventoryUtils!!.setInventoryWarningShown(false)
                        main.scheduler!!.removeQueuedFullInventoryWarnings()
                    } else if (feature === Feature.DISCORD_RPC) {
                        //TODO: DISABLED LOG.
//                        main.getDiscordRPCManager().stop()
                    } else if (feature === Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT) {
                        Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.setEnabled(true)
                    }
                }
                (abstractButton as ButtonToggle).onClick()
            } else if (abstractButton is ButtonSolid) {
                if (feature === Feature.TEXT_STYLE) {
                    SkyblockAddonsPlus.configValues!!.setTextStyle(SkyblockAddonsPlus.configValues!!.getTextStyle().getNextType())

                    cancelClose = true
                    Minecraft.getMinecraft().displayGuiScreen(SkyblockAddonsGui(page, tab))
                    cancelClose = false
                } else if (feature === Feature.CHROMA_MODE) {
                    //TODO:
//                    SkyblockAddonsPlus.configValues!!.setChromaMode(SkyblockAddonsPlus.configValues)!!.getChromaMode().getNextType()
//
//                    cancelClose = true
//                    Minecraft.getMinecraft().displayGuiScreen(SkyblockAddonsGui(page, tab))
//                    cancelClose = false
                } else if (feature === Feature.TURN_ALL_FEATURES_CHROMA) {
                    //TODO:
//                    var enable = false
//
//                    for (loopFeature in Feature.values()) {
//                        if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData()!!.getDefaultColor() != null
//                        ) {
//                            if (!SkyblockAddonsPlus.configValues!!.getChromaFeatures().contains(loopFeature))
//                            run {
//                                enable = true
//                                break
//                            }
//                        }
//                    }
//
//                    for (loopFeature in Feature.values()) {
//                        if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData()!!
//                                .getDefaultColor() != null
//                        ) {
//                            if (enable) {
//                                SkyblockAddonsPlus.configValues
//                                !!
//                                getChromaFeatures().add(loopFeature)
//                            } else {
//                                SkyblockAddonsPlus.configValues
//                                !!
//                                getChromaFeatures().remove(loopFeature)
//                            }
//                        }
//                    }
                }
            } else if (abstractButton is ButtonModify) {
                if (feature === Feature.ADD) {
                    if (SkyblockAddonsPlus.configValues!!.getWarningSeconds() < 99)
                    run {
                        SkyblockAddonsPlus.configValues!!.setWarningSeconds(SkyblockAddonsPlus.configValues!!.getWarningSeconds() + 1)
                    }
                } else {
                    if (SkyblockAddonsPlus.configValues!!.getWarningSeconds() > 1)
                    run {
                        SkyblockAddonsPlus.configValues!!.setWarningSeconds(SkyblockAddonsPlus.configValues!!.getWarningSeconds() - 1)
                    }
                }
            } else if (abstractButton is ButtonCredit) {
                if (SkyblockAddonsPlus.configValues!!.isRemoteDisabled(feature))
                return
                val credit: FeatureCredit = (abstractButton as ButtonCredit).getCredit()
                try {
                    Desktop.getDesktop().browse(URI(credit.getUrl()))
                } catch (ignored: Exception) {
                }
            }
        } else if (abstractButton is ButtonArrow) {
            val arrow: ButtonArrow = abstractButton as ButtonArrow
            if (arrow.isNotMax) {
                SkyblockAddonsPlus.utils!!.setFadingIn(false)
                if (arrow.getArrowType() === ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(SkyblockAddonsGui(++page, tab))
                } else {
                    mc.displayGuiScreen(SkyblockAddonsGui(--page, tab))
                }
            }
        } else if (abstractButton is ButtonSwitchTab) {
            val tab: ButtonSwitchTab = abstractButton as ButtonSwitchTab
            if (tab.getTab() !== this.tab) {
                SkyblockAddonsPlus.utils!!.setFadingIn(false)
                mc.displayGuiScreen(SkyblockAddonsGui(1, tab.getTab()))
            }
        } else if (abstractButton is ButtonSocial) {
            val social: Social = (abstractButton as ButtonSocial).getSocial()
            try {
//                Desktop.getDesktop().browse(social.getUrl())
            } catch (ignored: Exception) {
            }
        } else if (abstractButton is ButtonBanner) {
            try {
                Desktop.getDesktop().browse(URI(main.getOnlineData()!!.getBannerLink()))
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * Adds a button, limiting its width and setting the correct position.
     */
    private fun addButton(feature: Feature, buttonType: ButtonType) {
        if (displayCount == 0) return
        val text: String = feature.getMessage()!!
        val halfWidth: Int = width / 2
        val boxWidth = 140
        val boxHeight = 50
        var x = 0
        if (collumn == 1) {
            x = halfWidth - 90 - boxWidth
        } else if (collumn == 2) {
            x = halfWidth - (boxWidth / 2)
        } else if (collumn == 3) {
            x = halfWidth + 90
        }
        val y = getRowHeight(row.toDouble())
        if (buttonType === EnumUtils.ButtonType.TOGGLE) {
            val button: ButtonNormal = ButtonNormal(x.toDouble(), y.toDouble(), text, main, feature)
            buttonList.add(button)

            val credit: FeatureCredit = EnumUtils.FeatureCredit.fromFeature(feature)!!
            if (credit != null) {
                val coords: IntPair = button.getCreditsCoords(credit)
                buttonList.add(
                    ButtonCredit(
                        coords.getX().toDouble(),
                        coords.getY().toDouble(),
                        text,
                        credit,
                        feature,
                        button.isMultilineButton
                    )
                )
            }

            if (feature.getSettings().size > 0) {
                buttonList.add(ButtonSettings(x + boxWidth - 33.0, y + boxHeight - 20.0, text, main, feature))
            }
            buttonList.add(ButtonToggle(x + 40.0, y + boxHeight - 18, main, feature))
        } else if (buttonType === ButtonType.SOLID) {
            buttonList.add(ButtonNormal(x.toDouble(), y, text, main, feature))

            if (feature === Feature.TEXT_STYLE || feature === Feature.CHROMA_MODE || feature === Feature.TURN_ALL_FEATURES_CHROMA) {
                buttonList.add(ButtonSolid(x + 10.0, y + boxHeight - 23, 120, 15, "", main, feature))
            } else if (feature === Feature.WARNING_TIME) {
                val solidButtonX = x + (boxWidth / 2) - 17
                buttonList.add(
                    ButtonModify(
                        (solidButtonX - 20).toDouble(),
                        y + boxHeight - 23,
                        15,
                        15,
                        "+",
                        main,
                        Feature.ADD
                    )
                )
                buttonList.add(ButtonSolid(solidButtonX.toDouble(), y + boxHeight - 23, 35, 15, "", main, feature))
                buttonList.add(
                    ButtonModify(
                        (solidButtonX + 35 + 5).toDouble(),
                        y + boxHeight - 23,
                        15,
                        15,
                        "-",
                        main,
                        Feature.SUBTRACT
                    )
                )
            }
        } else if (buttonType === EnumUtils.ButtonType.CHROMA_SLIDER) {
            buttonList.add(ButtonNormal(x.toDouble(), y, text, main, feature))

//            if (feature === Feature.CHROMA_SPEED) {
//                //TODO:
//                buttonList.add(NewButtonSlider(x + 35.0, y + boxHeight - 23, 70, 15, SkyblockAddonsPlus.configValues.!! .getChromaSpeed().setValue(value)
//            } else if (feature === Feature.CHROMA_SIZE) {
//                buttonList.add(NewButtonSlider(x + 35, y + boxHeight - 23, 70, 15, SkyblockAddonsPlus.configValues))
//                !!
//                TODO(
//                    """
//                    |Cannot convert element
//                    |With text:
//                    |getChromaSize().floatValue(),
//                    |                        1, 100, 1, value -> SkyblockAddonsPlus.configValues
//                    """.trimMargin()
//                )
//                !!
//                getChromaSize().setValue(value)
//            }
//            else if (feature === Feature.CHROMA_BRIGHTNESS) {
//                buttonList.add(NewButtonSlider(x + 35, y + boxHeight - 23, 70, 15, SkyblockAddonsPlus.configValues))!!.getChromaBrightness().setValue(value)
//            } else if (feature === Feature.CHROMA_SATURATION) {
//                buttonList.add(NewButtonSlider(x + 35, y + boxHeight - 23, 70, 15, SkyblockAddonsPlus.configValues)).!!.getChromaSaturation().setValue(value)
//            }
        }

        if (feature.isNew) {
            buttonList.add(ButtonNewTag(x + boxWidth - 15, y.toInt() + boxHeight - 10))
        }

        collumn++
        if (collumn > 3) {
            collumn = 1
            row++
        }
        displayCount--
    }

    private fun addLanguageButton() {
        val halfWidth: Int = width / 2
        val boxWidth = 140
        val boxHeight = 50
        val x = halfWidth + 90
        val y = getRowHeight(displayCount / 3.0 + 1)
        buttonList.add(
            ButtonNormal(
                x.toDouble(),
                y,
                boxWidth,
                boxHeight,
                Translations.getMessage("languageText") + Feature.LANGUAGE.getMessage(),
                main,
                Feature.LANGUAGE
            )
        )
    }

    private fun addEditLocationsButton() {
        val halfWidth: Int = width / 2
        val boxWidth = 140
        val boxHeight = 50
        val x = halfWidth - 90 - boxWidth
        val y = getRowHeight(displayCount / 3.0 + 1)
        buttonList.add(
            ButtonNormal(
                x.toDouble(),
                y,
                boxWidth,
                boxHeight,
                Feature.EDIT_LOCATIONS.getMessage(),
                main,
                Feature.EDIT_LOCATIONS
            )
        )
    }

    private fun addGeneralSettingsButton() {
        val halfWidth: Int = width / 2
        val boxWidth = 140
        val boxHeight = 15
        val x = halfWidth + 90
        val y = getRowHeight(1.0) - 25
        buttonList.add(
            ButtonNormal(
                x.toDouble(),
                y,
                boxWidth,
                boxHeight,
                Translations.getMessage("settings.tab.generalSettings"),
                main,
                Feature.GENERAL_SETTINGS
            )
        )
    }


    private fun addFeaturedBanner() {
        if (main.getOnlineData()!!.getBannerImageURL() != null) {
            val halfWidth: Int = width / 2
            buttonList.add(ButtonBanner(halfWidth - 170.0, 15.0))
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        if (featureSearchBar!!.isFocused()) {
            featureSearchBar!!.textboxKeyTyped(typedChar, keyCode)
            searchString = featureSearchBar!!.getText()

            SkyblockAddonsPlus.utils!!.setFadingIn(false)
            buttonList.clear()

            page = 1
            initGui()
        }
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        featureSearchBar!!.mouseClicked(mouseX, mouseY, mouseButton)
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private fun getRowHeight(row: Double): Double {
        var row = row
        row--
        return 95 + (row * 60) //height*(0.18+(row*0.08));
    }

    /**
     * Save the config when exiting.
     */
    override fun onGuiClosed() {
        if (!cancelClose) {
            if (tab === EnumUtils.GuiTab.GENERAL_SETTINGS) {
                //TODO:
//                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN)
            }
            SkyblockAddonsPlus.configValues!!.saveConfig()
            Keyboard.enableRepeatEvents(false)
        }
    }

    override fun updateScreen() {
        super.updateScreen()
        featureSearchBar!!.updateCursorCounter()
    }

    override fun onResize(mcIn: Minecraft, w: Int, h: Int) {
        super.onResize(mcIn, w, h)
        SkyblockAddonsPlus.utils!!.setFadingIn(false)
    }

    companion object {
        val LOGO: ResourceLocation = ResourceLocation("skyblockaddons", "logo.png")
        val LOGO_GLOW: ResourceLocation = ResourceLocation("skyblockaddons", "logoglow.png")

        const val BUTTON_MAX_WIDTH: Int = 140

        private var searchString: String? = null

        /**
         * Draws the default text at the top at bottoms of the GUI.
         * @param gui The gui to draw the text on.
         */
        fun drawDefaultTitleText(gui: GuiScreen, alpha: Int) {
            val defaultBlue: Int = SkyblockAddonsPlus.utils!!.getDefaultBlue(alpha)

            val height = 85
            val width = height * 2
            val scaledResolution: ScaledResolution = ScaledResolution(Minecraft.getMinecraft())

            val textureManager: TextureManager = Minecraft.getMinecraft().getTextureManager()

            SkyblockAddonsPlus.utils!!.enableStandardGLOptions()
            textureManager.bindTexture(LOGO)
            DrawUtils.drawModalRectWithCustomSizedTexture(
                scaledResolution.getScaledWidth() / 2f - width / 2f,
                5f,
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                width.toFloat(),
                height.toFloat(),
                true
            )

            val animationMillis = 4000
            var glowAlpha = (System.currentTimeMillis() % animationMillis).toFloat()
            glowAlpha = if (glowAlpha > animationMillis / 2f) {
                (animationMillis - glowAlpha) / (animationMillis / 2f)
            } else {
                glowAlpha / (animationMillis / 2f)
            }

            GlStateManager.color(1f, 1f, 1f, glowAlpha)
            textureManager.bindTexture(LOGO_GLOW)
            DrawUtils.drawModalRectWithCustomSizedTexture(
                scaledResolution.getScaledWidth() / 2f - width / 2f,
                5f,
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                width.toFloat(),
                height.toFloat(),
                true
            )

            GlStateManager.color(1f, 1f, 1f, 1f)
            val version = ("v" + SkyblockAddonsPlus.VERSION.replace("beta", "b")).toString() + " by Biscut"
            drawScaledString(
                gui,
                version,
                55,
                defaultBlue,
                1.3,
                170 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(version),
                false
            )

            if (gui is SkyblockAddonsGui) {
                drawScaledString(
                    gui,
                    "Special Credits: InventiveTalent - Magma Boss Timer API",
                    gui.height - 22,
                    defaultBlue,
                    1.0,
                    0
                )
            }

            SkyblockAddonsPlus.utils!!.restoreGLOptions()
        }

        /**
         * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
         *
         * @param text The text to draw.
         * @param y The y level to draw the text/
         * @param color The text color.
         * @param scale The scale to draw the text.
         * @param xOffset The offset from the center x that the text should be drawn at.
         */
        @JvmOverloads
        fun drawScaledString(
            guiScreen: GuiScreen,
            text: String?,
            y: Int,
            color: Int,
            scale: Double,
            xOffset: Int,
            centered: Boolean = true
        ) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(scale, scale, 1.0)
            if (centered) {
                DrawUtils.drawCenteredText(
                    text, (Math.round(guiScreen.width.toFloat() / 2 / scale) + xOffset).toFloat(),
                    Math.round(y.toFloat() / scale).toFloat(), color
                )
            } else {
                Minecraft.getMinecraft().fontRendererObj.drawString(
                    text, (Math.round(guiScreen.width.toFloat() / 2 / scale) + xOffset).toFloat(),
                    Math.round(y.toFloat() / scale).toFloat(), color, true
                )
            }
            GlStateManager.popMatrix()
        }
    }
}
