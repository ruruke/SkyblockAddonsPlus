//package moe.ruruke.skyblock.gui
//
//import moe.ruruke.skyblock.SkyblockAddonsPlus
//import moe.ruruke.skyblock.core.Feature
//import moe.ruruke.skyblock.core.Language
//import moe.ruruke.skyblock.gui.buttons.ButtonArrow
//import moe.ruruke.skyblock.utils.EnumUtils
//import org.apache.logging.log4j.Logger
//import org.lwjgl.input.Keyboard
//import moe.ruruke.skyblock.gui.buttons.*
//import moe.ruruke.skyblock.utils.EnumUtils.*
//import net.minecraft.client.Minecraft
//import net.minecraft.client.gui.GuiScreen
//import java.awt.Color
//import java.io.IOException
//import java.util.*
//
//open class SettingsGui(
//    feature: Feature,
//    var page: Int,
//    val lastPage: Int,
//    lastTab: GuiTab?,
//    settings: List<FeatureSetting?>?
//) :
//    GuiScreen() {
//    val feature: Feature = feature
//    val lastTab: GuiTab? = lastTab
//    val settings: List<FeatureSetting?>? = settings
//    val timeOpened: Long = System.currentTimeMillis()
//    var row: Float = 1f
//    var column: Int = 1
//    var displayCount: Int = 0
//    var closingGui: Boolean = false
//    var reInit: Boolean = false
//
//    override fun initGui() {
//        Keyboard.enableRepeatEvents(true)
//        row = 1f
//        column = 1
//        buttonList.clear()
//        if (feature === Feature.LANGUAGE) {
//            val currentLanguage: Language? = Language.getFromPath(SkyblockAddonsPlus.configValues!!.getLanguage().getPath())
//
//
//            displayCount = findDisplayCount()
//            // Add the buttons for each page.
//            var skip = (page - 1) * displayCount
//
//            var max = page == 1
//            buttonList.add(ButtonArrow(width / 2.0 - 15.0 - 50.0, height - 70.0, main, ButtonArrow.ArrowType.LEFT, max))
//            max = Language.entries.size - skip - displayCount <= 0
//            buttonList.add(ButtonArrow(width / 2.0 - 15.0 + 50.0, height - 70.0, main, ButtonArrow.ArrowType.RIGHT, max))
//
//            for (language in Language.values()) {
//                if (skip == 0) {
//                    if (language === Language.ENGLISH) continue
//                    if (language === Language.CHINESE_TRADITIONAL) {
//                        addLanguageButton(Language.ENGLISH)
//                    }
//                    addLanguageButton(language)
//                } else {
//                    skip--
//                }
//            }
//
//            SkyblockAddonsPlus.configValues
//            !!
//            setLanguage(currentLanguage)
//            DataUtils.loadLocalizedStrings(false)
//        } else {
//            for (setting in settings) {
//                addButton(setting)
//            }
//        }
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
//
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
//            val halfWidth: Int = width / 2
//            val boxWidth = 140
//            val x = halfWidth - 90 - boxWidth
//            var width = halfWidth + 90 + boxWidth
//            width -= x
//            var numSettings = settings!!.size.toFloat()
//            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_STATE)) {
//                if (SkyblockAddonsPlus.configValues);
//                !!
//                getDiscordStatus() === DiscordStatus.CUSTOM
//                numSettings++
//                if (SkyblockAddonsPlus.configValues);
//                !!
//                getDiscordStatus() === DiscordStatus.AUTO_STATUS
//                run {
//                    numSettings++
//                    if (SkyblockAddonsPlus.configValues);
//                    !!
//                    getDiscordAutoDefault() === DiscordStatus.CUSTOM
//                    run {
//                        numSettings++
//                    }
//                }
//                numSettings += 0.4.toFloat()
//            }
//            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_DETAILS)) {
//                if (SkyblockAddonsPlus.configValues);
//                !!
//                getDiscordDetails() === DiscordStatus.CUSTOM
//                numSettings++
//                if (SkyblockAddonsPlus.configValues);
//                !!
//                getDiscordDetails() === DiscordStatus.AUTO_STATUS
//                run {
//                    numSettings++
//                    if (SkyblockAddonsPlus.configValues);
//                    !!
//                    getDiscordAutoDefault() === DiscordStatus.CUSTOM
//                    run {
//                        numSettings++
//                    }
//                }
//                numSettings += 0.4.toFloat()
//            }
//            val height = (getRowHeightSetting(numSettings.toDouble()) - 50).toInt()
//            val y = getRowHeight(1.0).toInt()
//            GlStateManager.enableBlend()
//            if (this !is EnchantmentSettingsGui) {
//                DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4)
//            }
//            drawScaledString(this, Translations.getMessage("settings.settings"), 110, defaultBlue, 1.5, 0)
//        }
//        super.drawScreen(mouseX, mouseY, partialTicks) // Draw buttons.
//    }
//
//    /**
//     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
//     */
//    override fun actionPerformed(abstractButton: GuiButton) {
//        when {
//            abstractButton is ButtonLanguage -> {
//                val language: Language = (abstractButton as ButtonLanguage).getLanguage()
//                DataUtils.loadLocalizedStrings(language, false)
//                main.setKeyBindingDescriptions()
//                returnToGui()
//            }
//            abstractButton is ButtonSwitchTab -> {
//                val tab: ButtonSwitchTab = abstractButton as ButtonSwitchTab
//                mc.displayGuiScreen(SkyblockAddonsGui(1, tab.getTab()))
//            }
//            abstractButton is ButtonOpenColorMenu -> {
//                closingGui = true
//                // Temp fix until feature re-write. Open a color selection panel specific to the color setting
//                val f: Feature = (abstractButton as ButtonOpenColorMenu).feature!!
//                if (f === Feature.ENCHANTMENT_PERFECT_COLOR || f === Feature.ENCHANTMENT_GREAT_COLOR || f === Feature.ENCHANTMENT_GOOD_COLOR || f === Feature.ENCHANTMENT_POOR_COLOR || f === Feature.ENCHANTMENT_COMMA_COLOR) {
//                    mc.displayGuiScreen(ColorSelectionGui(f, EnumUtils.GUIType.SETTINGS, lastTab, lastPage))
//                } else {
//                    mc.displayGuiScreen(ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, lastTab, lastPage))
//                }
//            }
////            abstractButton is ButtonToggleTitle -> {
////                val button: ButtonFeature = abstractButton as ButtonFeature
////                val feature: Feature = button.feature ?: return
////                if (SkyblockAddonsPlus.configValues!!.isDisabled(feature))
////                run {
////                    feature.setEnabled(true)
////                }
////                run {
////                    feature.setEnabled(false)
////                    if (feature === Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
////                        GuiIngameForge.renderArmor = true // The food gets automatically enabled, no need to include it.
////                    } else if (feature === Feature.HIDE_HEALTH_BAR) {
////                        GuiIngameForge.renderHealth = true
////                    } else if (feature === Feature.REPEAT_FULL_INVENTORY_WARNING) {
////                        // Remove queued warnings when the repeat setting is turned off.
////                        main.getScheduler().removeQueuedFullInventoryWarnings()
////                    }
////                }
////            }
////            feature === Feature.SHOW_BACKPACK_PREVIEW -> {
////                SkyblockAddonsPlus.configValues
////                !!
////                setBackpackStyle(SkyblockAddonsPlus.configValues)
////                !!
////                getBackpackStyle().getNextType()
////
////                closingGui = true
////                Minecraft.getMinecraft().displayGuiScreen(SettingsGui(feature, page, lastPage, lastTab, settings))
////                closingGui = false
////            }
////            feature === Feature.POWER_ORB_STATUS_DISPLAY && abstractButton is ButtonSolid -> {
////                SkyblockAddonsPlus.configValues
////                !!
////                setPowerOrbDisplayStyle(SkyblockAddonsPlus.configValues)
////                !!
////                getPowerOrbDisplayStyle().getNextType()
////
////                closingGui = true
////                Minecraft.getMinecraft().displayGuiScreen(SettingsGui(feature, page, lastPage, lastTab, settings))
////                closingGui = false
////            }
//            abstractButton is ButtonArrow -> {
//                val arrow: ButtonArrow = abstractButton as ButtonArrow
//                if (arrow.isNotMax()) {
//                    SkyblockAddonsPlus.utils
//                    !!
//                    setFadingIn(false)
//                    if (arrow.getArrowType() === ButtonArrow.ArrowType.RIGHT) {
//                        closingGui = true
//                        mc.displayGuiScreen(SettingsGui(feature, ++page, lastPage, lastTab, settings))
//                    } else {
//                        closingGui = true
//                        mc.displayGuiScreen(SettingsGui(feature, --page, lastPage, lastTab, settings))
//                    }
//                }
//            }
//        }
//    }
//
//    private fun addLanguageButton(language: Language) {
//        if (displayCount == 0) return
//        val text: String = feature.getMessage()
//        val halfWidth: Int = width / 2
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
//        var halfWidth: Int = width / 2
//        var boxWidth = 100
//        var x = halfWidth - (boxWidth / 2)
//        var y = getRowHeightSetting(row.toDouble())
//        if (setting === EnumUtils.FeatureSetting.COLOR) {
//            buttonList.add(
//                ButtonOpenColorMenu(
//                    x,
//                    y,
//                    100,
//                    20,
//                    Translations.getMessage("settings.changeColor"),
//                    main,
//                    feature
//                )
//            )
//        } else if (setting === EnumUtils.FeatureSetting.GUI_SCALE) {
//            try {
//                buttonList.add(ButtonGuiScale(x, y, 100, 20, main, feature))
//            } catch (e: NumberFormatException) {
//                logger.error(e.message)
//                SkyblockAddonsPlus.utils
//                !!
//                sendMessage(Translations.getMessage("messages.invalidFeatureConfiguration", feature.getMessage()))
//                SkyblockAddonsPlus.configValues
//                !!
//                setGuiScale(feature, ConfigValues.normalizeValueNoStep(1))
//                buttonList.add(ButtonGuiScale(x, y, 100, 20, main, feature))
//            }
//        } else if (setting === EnumUtils.FeatureSetting.GUI_SCALE_X) {
//            buttonList.add(ButtonGuiScale(x, y, 100, 20, main, feature, true))
//        } else if (setting === EnumUtils.FeatureSetting.GUI_SCALE_Y) {
//            buttonList.add(ButtonGuiScale(x, y, 100, 20, main, feature, false))
//        } else if (setting === EnumUtils.FeatureSetting.REPEATING) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//
//            var settingFeature: Feature? = null
//            if (feature === Feature.FULL_INVENTORY_WARNING) {
//                settingFeature = Feature.REPEAT_FULL_INVENTORY_WARNING
//            } else if (feature === Feature.BOSS_APPROACH_ALERT) {
//                settingFeature = Feature.REPEAT_SLAYER_BOSS_WARNING
//            }
//
//            buttonList.add(ButtonToggleTitle(x, y, Translations.getMessage("settings.repeating"), main, settingFeature))
//        } else if (setting === EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//
//            var settingFeature: Feature? = null
//            if (feature === Feature.DARK_AUCTION_TIMER) {
//                settingFeature = Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES
//            } else if (feature === Feature.FARM_EVENT_TIMER) {
//                settingFeature = Feature.SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES
//            } else if (feature === Feature.DROP_CONFIRMATION) {
//                settingFeature = Feature.DOUBLE_DROP_IN_OTHER_GAMES
//            } else if (feature === Feature.OUTBID_ALERT_SOUND) {
//                settingFeature = Feature.OUTBID_ALERT_SOUND_IN_OTHER_GAMES
//            }
//
//            buttonList.add(
//                ButtonToggleTitle(
//                    x,
//                    y,
//                    Translations.getMessage("settings.showInOtherGames"),
//                    main,
//                    settingFeature
//                )
//            )
//        } else if (setting === EnumUtils.FeatureSetting.BACKPACK_STYLE) {
//            boxWidth = 140
//            x = halfWidth - (boxWidth / 2)
//            buttonList.add(ButtonSolid(x, y, 140, 20, Translations.getMessage("settings.backpackStyle"), main, feature))
//        } else if (setting === EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//
//            val settingFeature: Feature? = null
//
//
//            buttonList.add(
//                ButtonToggleTitle(
//                    x,
//                    y,
//                    Translations.getMessage("settings.enableMessageWhenActionPrevented"),
//                    main,
//                    settingFeature
//                )
//            )
//        } else if (setting === EnumUtils.FeatureSetting.POWER_ORB_DISPLAY_STYLE) {
//            boxWidth = 140
//            x = halfWidth - (boxWidth / 2)
//            buttonList.add(
//                ButtonSolid(
//                    x,
//                    y,
//                    140,
//                    20,
//                    Translations.getMessage("settings.powerOrbDisplayStyle"),
//                    main,
//                    feature
//                )
//            )
//        } else if (setting === EnumUtils.FeatureSetting.DISCORD_RP_DETAILS || setting === EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
//            boxWidth = 140
//            x = halfWidth - (boxWidth / 2)
//            var currentStatus: DiscordStatus
//            if (setting === EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
//                currentStatus = SkyblockAddonsPlus.configValues
//                !!
//                getDiscordStatus()
//            } else {
//                currentStatus = SkyblockAddonsPlus.configValues
//                !!
//                getDiscordDetails()
//            }
//
//            buttonList.add(
//                ButtonTextNew(
//                    halfWidth,
//                    y.toInt() - 10,
//                    if (setting === EnumUtils.FeatureSetting.DISCORD_RP_DETAILS) Translations.getMessage("messages.firstStatus") else Translations.getMessage(
//                        "messages.secondStatus"
//                    ),
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
//                    Arrays.asList(DiscordStatus.values()),
//                    currentStatus.ordinal()
//                ) { index ->
//                    val selectedStatus: DiscordStatus = DiscordStatus.values().get(index)
//                    if (setting === EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
//                        main.getDiscordRPCManager().setStateLine(selectedStatus)
//                        SkyblockAddonsPlus.configValues
//                        !!
//                        setDiscordStatus(selectedStatus)
//                    } else {
//                        main.getDiscordRPCManager().setDetailsLine(selectedStatus)
//                        SkyblockAddonsPlus.configValues
//                        !!
//                        setDiscordDetails(selectedStatus)
//                    }
//                    this@SettingsGui.reInit = true
//                })
//
////            if (currentStatus === DiscordStatus.AUTO_STATUS) {
////                row++
////                row += 0.4.toFloat()
////                x = halfWidth - (boxWidth / 2)
////                y = getRowHeightSetting(row.toDouble())
////
////                buttonList.add(
////                    ButtonTextNew(
////                        halfWidth,
////                        y.toInt() - 10,
////                        Translations.getMessage("messages.fallbackStatus"),
////                        true,
////                        -0x1
////                    )
////                )
////                currentStatus = SkyblockAddonsPlus.configValues
////                !!
////                getDiscordAutoDefault()
////                buttonList.add(
////                    ButtonSelect(
////                        x,
////                        y.toInt(),
////                        boxWidth,
////                        20,
////                        Arrays.asList(DiscordStatus.values()),
////                        currentStatus.ordinal()
////                    ) { index ->
////                        val selectedStatus: DiscordStatus = DiscordStatus.values().get(index)
////                        SkyblockAddonsPlus.configValues
////                        !!
////                        setDiscordAutoDefault(selectedStatus)
////                        this@SettingsGui.reInit = true
////                    })
////            }
//
//            //TODO:
////            if (currentStatus === DiscordStatus.CUSTOM) {
////                row++
////                halfWidth = width / 2
////                boxWidth = 200
////                x = halfWidth - (boxWidth / 2)
////                y = getRowHeightSetting(row.toDouble())
////
////                var discordStatusEntry: DiscordStatusEntry = EnumUtils.DiscordStatusEntry.DETAILS
////                if (setting === EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
////                    discordStatusEntry = EnumUtils.DiscordStatusEntry.STATE
////                }
////                val finalDiscordStatusEntry: DiscordStatusEntry = discordStatusEntry
////                val inputField: ButtonInputFieldWrapper =
////                    ButtonInputFieldWrapper(x, y.toInt(), 200, 20, SkyblockAddonsPlus.configValues)
////                !!
////                TODO(
////                    """
////                    |Cannot convert element
////                    |With text:
////                    |getCustomStatus(discordStatusEntry),
////                    |                        null, 100, false, updatedValue -> SkyblockAddonsPlus.configValues
////                    """.trimMargin()
////                )
////                !!
////                setCustomStatus(finalDiscordStatusEntry, updatedValue)
////
////                buttonList.add(inputField)
////            }
//
//            row += 0.4.toFloat()
//        } else if (setting === EnumUtils.FeatureSetting.MAP_ZOOM) {
//            // For clarity
//            boxWidth = 100 // Default size and stuff.
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(
//                ButtonSlider(
//                    x,
//                    y,
//                    100,
//                    20,
//                    DungeonMapManager.getDenormalizedMapZoom(),
//                    DungeonMapManager.MIN_ZOOM,
//                    DungeonMapManager.MAX_ZOOM,
//                    0.1f,
//                    object : OnSliderChangeCallback() {
//                        override fun sliderUpdated(value: Float) {
//                            DungeonMapManager.setMapZoom(value)
//                        }
//                    }).setPrefix("Map Zoom: ")
//            )
//        } else if (setting === EnumUtils.FeatureSetting.COLOUR_BY_RARITY) {
//            boxWidth = 31
//            x = halfWidth - boxWidth / 2
//            y = this.getRowHeightSetting(row.toDouble())
//            var settingFeature: Feature? = null
//            if (this.feature === Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE) {
//                settingFeature = Feature.BASE_STAT_BOOST_COLOR_BY_RARITY
//            } else if (feature === Feature.REVENANT_SLAYER_TRACKER) {
//                settingFeature = Feature.REVENANT_COLOR_BY_RARITY
//            } else if (feature === Feature.TARANTULA_SLAYER_TRACKER) {
//                settingFeature = Feature.TARANTULA_COLOR_BY_RARITY
//            } else if (feature === Feature.SVEN_SLAYER_TRACKER) {
//                settingFeature = Feature.SVEN_COLOR_BY_RARITY
//            } else if (feature === Feature.VOIDGLOOM_SLAYER_TRACKER) {
//                settingFeature = Feature.ENDERMAN_COLOR_BY_RARITY
//            } else if (feature === Feature.DRAGON_STATS_TRACKER) {
//                settingFeature = Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY
//            }
//
//            buttonList.add(
//                ButtonToggleTitle(
//                    x,
//                    y,
//                    Translations.getMessage("settings.colorByRarity"),
//                    main,
//                    settingFeature
//                )
//            )
//        } else if (setting === EnumUtils.FeatureSetting.TEXT_MODE) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//
//            var settingFeature: Feature? = null
//            if (feature === Feature.REVENANT_SLAYER_TRACKER) {
//                settingFeature = Feature.REVENANT_TEXT_MODE
//            } else if (feature === Feature.TARANTULA_SLAYER_TRACKER) {
//                settingFeature = Feature.TARANTULA_TEXT_MODE
//            } else if (feature === Feature.SVEN_SLAYER_TRACKER) {
//                settingFeature = Feature.SVEN_TEXT_MODE
//            } else if (feature === Feature.VOIDGLOOM_SLAYER_TRACKER) {
//                settingFeature = Feature.ENDERMAN_TEXT_MODE
//            } else if (feature === Feature.DRAGON_STATS_TRACKER_TEXT_MODE) {
//                settingFeature = Feature.DRAGON_STATS_TRACKER_TEXT_MODE
//            }
//
//            buttonList.add(ButtonToggleTitle(x, y, Translations.getMessage("settings.textMode"), main, settingFeature))
//        } else if (setting === EnumUtils.FeatureSetting.ZEALOT_SPAWN_AREAS_ONLY) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//
//            var settingFeature: Feature? = null
//            if (feature === Feature.ZEALOT_COUNTER) {
//                settingFeature = Feature.ZEALOT_COUNTER_ZEALOT_SPAWN_AREAS_ONLY
//            } else if (feature === Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
//                settingFeature = Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_ZEALOT_SPAWN_AREAS_ONLY
//            } else if (feature === Feature.SHOW_TOTAL_ZEALOT_COUNT) {
//                settingFeature = Feature.SHOW_TOTAL_ZEALOT_COUNT_ZEALOT_SPAWN_AREAS_ONLY
//            } else if (feature === Feature.SHOW_SUMMONING_EYE_COUNT) {
//                settingFeature = Feature.SHOW_SUMMONING_EYE_COUNT_ZEALOT_SPAWN_AREAS_ONLY
//            }
//
//            buttonList.add(ButtonToggleTitle(x.toFloat(), y, setting.getMessage()!!, main, settingFeature))
//        } else if (setting === EnumUtils.FeatureSetting.DISABLE_SPIRIT_SCEPTRE_MESSAGES) {
//            boxWidth = 31
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//
//            buttonList.add(ButtonToggleTitle(x, y, setting.getMessage(), main, Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES))
//        } else if (setting === EnumUtils.FeatureSetting.HEALING_CIRCLE_OPACITY) {
//            boxWidth = 150
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(NewButtonSlider(x, y, boxWidth, 20, SkyblockAddonsPlus.configValues))
//            !!
//            TODO(
//                """
//                |Cannot convert element
//                |With text:
//                |getHealingCircleOpacity().getValue(), 0, 1, 0.01F,
//                |                    updatedValue -> SkyblockAddonsPlus.configValues
//                """.trimMargin()
//            )
//            !!
//            getHealingCircleOpacity().setValue(updatedValue)
//            setPrefix("Healing Circle Opacity: ")
//        } else if (setting === EnumUtils.FeatureSetting.TREVOR_SHOW_QUEST_COOLDOWN) {
//            boxWidth = 31 // Default size and stuff.
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(ButtonToggleTitle(x, y, setting.getMessage(), main, setting.getFeatureEquivalent()))
//            row += .1.toFloat()
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(
//                ButtonTextNew(
//                    halfWidth,
//                    y.toInt() + 15,
//                    Translations.getMessage("settings.trevorTheTrapper.showQuestCooldownDescription"),
//                    true,
//                    ColorCode.GRAY.getColor()
//                )
//            )
//            row += 0.4.toFloat()
//        } else if (setting === EnumUtils.FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY && feature === Feature.ENTITY_OUTLINES) {
//            boxWidth = 31 // Default size and stuff.
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(ButtonToggleTitle(x, y, setting.getMessage(), main, setting.getFeatureEquivalent()))
//            row += .4.toFloat()
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(
//                ButtonTextNew(
//                    halfWidth,
//                    y.toInt() + 15,
//                    Translations.getMessage("messages.entityOutlinesRequirement"),
//                    true,
//                    ColorCode.GRAY.getColor()
//                )
//            )
//            row += .4.toFloat()
//        } else {
//            boxWidth = 31 // Default size and stuff.
//            x = halfWidth - (boxWidth / 2)
//            y = getRowHeightSetting(row.toDouble())
//            buttonList.add(ButtonToggleTitle(x, y, setting.getMessage(), main, setting.getFeatureEquivalent()))
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
//        main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab)
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
//
//    companion object {
//        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
//        val logger: Logger = SkyblockAddons.getLogger()
//    }
//}
