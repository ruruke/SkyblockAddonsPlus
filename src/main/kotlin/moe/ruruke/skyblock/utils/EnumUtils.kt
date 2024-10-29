package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations.getMessage
import net.minecraft.util.ResourceLocation
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class EnumUtils {
    enum class AnchorPoint(val id: Int) {
        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        BOTTOM_MIDDLE(4);

        fun getX(maxX: Int): Int {
            var x = 0
            when (this) {
                TOP_RIGHT, BOTTOM_RIGHT -> x = maxX
                BOTTOM_MIDDLE -> x = maxX / 2
                TOP_LEFT -> TODO()
                BOTTOM_LEFT -> TODO()
            }
            return x
        }

        fun getY(maxY: Int): Int {
            var y = 0
            when (this) {
                BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_MIDDLE -> y = maxY
                TOP_LEFT -> TODO()
                TOP_RIGHT -> TODO()
            }
            return y
        }

        companion object {
            @Suppress("unused") // Accessed by reflection...
            fun fromId(id: Int): AnchorPoint? {
                for (feature in entries) {
                    if (feature.id == id) {
                        return feature
                    }
                }
                return null
            }
        }
    }

    enum class ButtonType {
        TOGGLE,
        SOLID,
        CHROMA_SLIDER
    }

    enum class BackpackStyle(private val TRANSLATION_KEY: String) {
        GUI("settings.backpackStyles.regular"),
        BOX("settings.backpackStyles.compact");

        val message: String?
            get() = getMessage(TRANSLATION_KEY)

        val nextType: BackpackStyle
            get() {
                var nextType = ordinal + 1
                if (nextType > entries.size - 1) {
                    nextType = 0
                }
                return entries[nextType]
            }
    }

    enum class PowerOrbDisplayStyle(private val TRANSLATION_KEY: String) {
        DETAILED("settings.powerOrbStyle.detailed"),
        COMPACT("settings.powerOrbStyle.compact");

        val message: String?
            get() = getMessage(TRANSLATION_KEY)

        val nextType: PowerOrbDisplayStyle
            get() {
                var nextType = ordinal + 1
                if (nextType > entries.size - 1) {
                    nextType = 0
                }
                return entries[nextType]
            }
    }

    enum class TextStyle(private val TRANSLATION_KEY: String) {
        STYLE_ONE("settings.textStyles.one"),
        STYLE_TWO("settings.textStyles.two");

        val message: String?
            get() = getMessage(TRANSLATION_KEY)

        val nextType: TextStyle
            get() {
                var nextType = ordinal + 1
                if (nextType > entries.size - 1) {
                    nextType = 0
                }
                return entries[nextType]
            }
    }

    enum class GuiTab {
        MAIN, GENERAL_SETTINGS
    }

    /**
     * Settings that modify the behavior of features- without technically being
     * a feature itself.
     *
     *
     * For the equivalent feature (that holds the state) use the ids instead of the enum directly
     * because the enum Feature depends on FeatureSetting, so FeatureSetting can't depend on Feature on creation.
     */
    enum class FeatureSetting {
        COLOR("settings.changeColor", -1),
        GUI_SCALE("settings.guiScale", -1),
        GUI_SCALE_X("settings.guiScaleX", -1),
        GUI_SCALE_Y("settings.guiScaleY", -1),
        ENABLED_IN_OTHER_GAMES("settings.showInOtherGames", -1),
        REPEATING("settings.repeating", -1),
        TEXT_MODE("settings.textMode", -1),
        DRAGONS_NEST_ONLY("settings.dragonsNestOnly", 128),
        USE_VANILLA_TEXTURE("settings.useVanillaTexture", 17),
        BACKPACK_STYLE("settings.backpackStyle", -1),
        SHOW_ONLY_WHEN_HOLDING_SHIFT("settings.showOnlyWhenHoldingShift", 18),
        MAKE_INVENTORY_COLORED("settings.makeBackpackInventoriesColored", 43),
        POWER_ORB_DISPLAY_STYLE("settings.powerOrbDisplayStyle", -1),
        CHANGE_BAR_COLOR_WITH_POTIONS("settings.changeBarColorForPotions", 46),
        ENABLE_MESSAGE_WHEN_ACTION_PREVENTED("settings.enableMessageWhenActionPrevented", -1),
        ENABLE_CAKE_BAG_PREVIEW("settings.showCakeBagPreview", 71),
        ROTATE_MAP("settings.rotateMap", 100),
        CENTER_ROTATION_ON_PLAYER("settings.centerRotationOnYourPlayer", 101),
        MAP_ZOOM("settings.mapZoom", -1),
        COLOUR_BY_RARITY("settings.colorByRarity", -1),
        SHOW_PLAYER_HEADS_ON_MAP("settings.showPlayerHeadsOnMap", 106),
        SHOW_GLOWING_ITEMS_ON_ISLAND("settings.showGlowingItemsOnIsland", 109),
        SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL("settings.skillActionsLeftUntilNextLevel", 115),
        HIDE_WHEN_NOT_IN_CRYPTS("settings.hideWhenNotDoingQuest", 133),
        HIDE_WHEN_NOT_IN_SPIDERS_DEN("settings.hideWhenNotDoingQuest", 134),
        HIDE_WHEN_NOT_IN_CASTLE("settings.hideWhenNotDoingQuest", 135),
        ENABLE_PERSONAL_COMPACTOR_PREVIEW("settings.showPersonalCompactorPreview", 137),
        SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP("settings.showSkillPercentageInstead", 144),
        SHOW_SKILL_XP_GAINED("settings.showSkillXPGained", 145),
        SHOW_SALVAGE_ESSENCES_COUNTER("settings.showSalvageEssencesCounter", 146),
        HEALING_CIRCLE_OPACITY("settings.healingCircleOpacity", 156),
        COOLDOWN_PREDICTION("settings.cooldownPrediction", 164),
        PERFECT_ENCHANT_COLOR("enchants.superTier", 165),
        GREAT_ENCHANT_COLOR("enchants.highTier", 166),
        GOOD_ENCHANT_COLOR("enchants.midTier", 167),
        POOR_ENCHANT_COLOR("enchants.lowTier", 168),
        COMMA_ENCHANT_COLOR("enchants.commas", 171),
        LEVEL_100_LEG_MONKEY("settings.legendaryMonkeyLevel100", 169),
        BIGGER_WAKE("settings.biggerWake", 170),
        HIGHLIGHT_ENCHANTMENTS("settings.highlightSpecialEnchantments", 153),
        HIDE_ENCHANTMENT_LORE("settings.hideEnchantDescription", 176),
        HIDE_GREY_ENCHANTS("settings.hideGreyEnchants", 87),
        ENCHANT_LAYOUT("enchantLayout.title", 0),
        TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR("settings.trevorTheTrapper.trackedEntityProximityIndicator", 173),
        TREVOR_HIGHLIGHT_TRACKED_ENTITY("settings.trevorTheTrapper.highlightTrackedEntity", 174),
        TREVOR_SHOW_QUEST_COOLDOWN("settings.trevorTheTrapper.showQuestCooldown", 175),
        SHOW_FETCHUR_ONLY_IN_DWARVENS("settings.showFetchurOnlyInDwarven", 179),
        SHOW_FETCHUR_ITEM_NAME("settings.showFetchurItemName", 180),
        SHOW_FETCHUR_INVENTORY_OPEN_ONLY("settings.showFetchurInventoryOpenOnly", 181),
        WARN_WHEN_FETCHUR_CHANGES("settings.warnWhenFetchurChanges", 182),
        STOP_ONLY_RAT_SQUEAK("settings.onlyStopRatSqueak", 184),
        SHOW_ENDER_CHEST_PREVIEW("settings.showEnderChestPreview", 185),
        HIDE_WHEN_NOT_IN_END("settings.hideWhenNotDoingQuest", 187),
        HEALTH_PREDICTION("settings.vanillaHealthPrediction", 194),
        DUNGEON_PLAYER_GLOW("settings.outlineDungeonTeammates", 103),
        ITEM_GLOW("settings.glowingDroppedItems", 109),
        ABBREVIATE_SKILL_XP_DENOMINATOR("settings.abbreviateSkillXpDenominator", 198),
        OTHER_DEFENCE_STATS("settings.otherDefenseStats", 199),
        DISABLE_SPIRIT_SCEPTRE_MESSAGES("settings.disableDamageChatMessages", 203),
        OUTBID_ALERT("settings.outbidAlertSound", 206),
        DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME("settings.dontReplaceRomanNumeralsInItemNames", 210),
        RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU("settings.resetSalvagedEssencesAfterLeavingMenu", 214),
        CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD("settings.changeDungeonMapZoomWithKeyboard", 215),
        SHOW_PROFILE_TYPE("settings.showProfileType", 219),
        SHOW_NETHER_FACTION("settings.showNetherFaction", 220),
        ZEALOT_SPAWN_AREAS_ONLY("settings.zealotSpawnAreasOnly", -1),

        DISCORD_RP_STATE(0),
        DISCORD_RP_DETAILS(0),
        ;

        private val FEATURE_EQUIVALENT: Int
        private val TRANSLATION_KEY: String?

        constructor(featureEquivalent: Int) {
            this.TRANSLATION_KEY = null
            FEATURE_EQUIVALENT = featureEquivalent
        }

        constructor(translationKey: String?, featureEquivalent: Int) {
            this.TRANSLATION_KEY = translationKey
            this.FEATURE_EQUIVALENT = featureEquivalent
        }


        val featureEquivalent: Feature?
            get() {
                if (FEATURE_EQUIVALENT == -1) return null

                for (feature in Feature.entries) {
                    if (feature.getId() === FEATURE_EQUIVALENT) {
                        return feature
                    }
                }
                return null
            }

        fun getMessage(vararg variables: String?): String? {
            return if (TRANSLATION_KEY != null) {
                getMessage(
                    TRANSLATION_KEY,
                    *variables as Array<Any?>
                )
            } else {
                null
            }
        }
    }

    enum class FeatureCredit(private val author: String, private val url: String, vararg features: Feature?) {
        // If you make a feature, feel free to add your name here with an associated website of your choice.
        //INVENTIVE_TALENT("InventiveTalent", "inventivetalent.org", Feature.MAGMA_BOSS_TIMER),
        ORCHID_ALLOY(
            "orchidalloy",
            "github.com/orchidalloy",
            Feature.SUMMONING_EYE_ALERT,
            Feature.FISHING_SOUND_INDICATOR,
            Feature.ENCHANTMENT_LORE_PARSING
        ),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", Feature.PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", Feature.DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER(
            "tomocrafter",
            "github.com/tomocrafter",
            Feature.AVOID_BLINKING_NIGHT_VISION,
            Feature.SLAYER_INDICATOR,
            Feature.NO_ARROWS_LEFT_ALERT,
            Feature.BOSS_APPROACH_ALERT
        ),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", Feature.MINION_DISABLE_LOCATION_WARNING),
        COMNIEMEER("comniemeer", "github.com/comniemeer", Feature.JUNGLE_AXE_COOLDOWN),
        KEAGEL(
            "Keagel",
            "github.com/Keagel",  /*, Feature.ONLY_MINE_ORES_DEEP_CAVERNS*/
            Feature.DISABLE_MAGICAL_SOUP_MESSAGES
        ),
        SUPERHIZE("SuperHiZe", "github.com/superhize", Feature.SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER(
            "DidiSkywalker",
            "twitter.com/didiskywalker",
            Feature.ITEM_PICKUP_LOG,
            Feature.HEALTH_UPDATES,
            Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS,  /*Feature.CRAFTING_PATTERNS, */
            Feature.POWER_ORB_STATUS_DISPLAY
        ),

        //GARY("GARY_", "github.com/occanowey", Feature.ONLY_MINE_VALUABLES_NETHER)),
        P0KE("P0ke", "p0ke.dev", Feature.ZEALOT_COUNTER),
        BERISAN("Berisan", "github.com/Berisan", Feature.TAB_EFFECT_TIMERS),
        MYNAMEISJEFF("MyNameIsJeff", "github.com/My-Name-Is-Jeff", Feature.SHOW_BROKEN_FRAGMENTS),
        DJTHEREDSTONER(
            "DJtheRedstoner",
            "github.com/DJtheRedstoner",
            Feature.LEGENDARY_SEA_CREATURE_WARNING,
            Feature.HIDE_SVEN_PUP_NAMETAGS
        ),

        //ANTONIO32A("Antonio32A", "github.com/Antonio32A", Feature.ONLY_BREAK_LOGS_PARK),
        CHARZARD(
            "Charzard4261",
            "github.com/Charzard4261",
            Feature.DISABLE_TELEPORT_PAD_MESSAGES,
            Feature.BAIT_LIST,
            Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE,
            Feature.SHOW_ITEM_DUNGEON_FLOOR,
            Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE,
            Feature.SHOW_RARITY_UPGRADED,
            Feature.REVENANT_SLAYER_TRACKER,
            Feature.TARANTULA_SLAYER_TRACKER,
            Feature.SVEN_SLAYER_TRACKER,
            Feature.DRAGON_STATS_TRACKER,
            Feature.SHOW_PERSONAL_COMPACTOR_PREVIEW,
            Feature.SHOW_STACKING_ENCHANT_PROGRESS,
            Feature.STOP_BONZO_STAFF_SOUNDS,
            Feature.DISABLE_MORT_MESSAGES,
            Feature.DISABLE_BOSS_MESSAGES
        ),
        IHDEVELOPER(
            "iHDeveloper",
            "github.com/iHDeveloper",
            Feature.SHOW_DUNGEON_MILESTONE,
            Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY,
            Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY,
            Feature.DUNGEONS_SECRETS_DISPLAY,
            Feature.SHOW_SALVAGE_ESSENCES_COUNTER,
            Feature.SHOW_SWORD_KILLS,
            Feature.ENCHANTMENTS_HIGHLIGHT
        ),
        TIRELESS_TRAVELER("TirelessTraveler", "github.com/ILikePlayingGames", Feature.DUNGEON_DEATH_COUNTER),
        KAASBROODJU(
            "kaasbroodju",
            "github.com/kaasbroodju",
            Feature.SKILL_PROGRESS_BAR,
            Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP,
            Feature.SHOW_SKILL_XP_GAINED
        ),
        PHOUBE(
            "Phoube",
            "github.com/Phoube",
            Feature.HIDE_OTHER_PLAYERS_PRESENTS,
            Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS,  /*, Feature.ONLY_MINE_ORES_DWARVEN_MINES*/
            Feature.DRILL_FUEL_BAR,
            Feature.DRILL_FUEL_TEXT,
            Feature.FISHING_PARTICLE_OVERLAY,
            Feature.COOLDOWN_PREDICTION,
            Feature.BIGGER_WAKE,
            Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY,
            Feature.TREVOR_SHOW_QUEST_COOLDOWN
        ),
        PEDRO9558(
            "Pedro9558",
            "github.com/Pedro9558",
            Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR,
            Feature.TREVOR_THE_TRAPPER_FEATURES,
            Feature.FETCHUR_TODAY,
            Feature.STOP_RAT_SOUNDS
        ),
        ROBOTHANZO(
            "RobotHanzo",
            "robothanzo.dev",
            Feature.HIDE_SPAWN_POINT_PLAYERS,
            Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES
        ),
        IRONM00N("IRONM00N", "github.com/IRONM00N", Feature.FARM_EVENT_TIMER),
        SKYCATMINEPOKIE("skycatminepokie", "github.com/skycatminepokie", Feature.OUTBID_ALERT_SOUND),
        TIMOLOB("TimoLob", "github.com/TimoLob", Feature.BROOD_MOTHER_ALERT),
        NOPOTHEGAMER("NopoTheGamer", "twitch.tv/nopothegamer", Feature.BAL_BOSS_ALERT),
        CATFACE("CatFace", "github.com/CattoFace", Feature.PLAYER_SYMBOLS_IN_CHAT),
        HANNIBAL2(
            "Hannibal2",
            "github.com/hannibal00212",
            Feature.CRIMSON_ARMOR_ABILITY_STACKS,
            Feature.HIDE_TRUE_DEFENSE
        );

        private val features: EnumSet<Feature>? =
            EnumSet.of(features[0], *features)

        fun getAuthor(): String {
            return "Contrib. $author"
        }

        fun getUrl(): String {
            return "https://$url"
        }

        companion object {
            fun fromFeature(feature: Feature?): FeatureCredit? {
                for (credit in entries) {
                    if (credit.features!!.contains(feature)) return credit
                }
                return null
            }
        }
    }

    enum class DrawType {
        SKELETON_BAR,
        BAR,
        TEXT,
        PICKUP_LOG,
        DEFENCE_ICON,
        REVENANT_PROGRESS,
        POWER_ORB_DISPLAY,
        TICKER,
        BAIT_LIST_DISPLAY,
        TAB_EFFECT_TIMERS,
        DUNGEONS_MAP,
        SLAYER_TRACKERS,
        DRAGON_STATS_TRACKER,
        PROXIMITY_INDICATOR
    }

    enum class Social(resourcePath: String, url: String) {
        YOUTUBE("youtube", "https://www.youtube.com/channel/UCYmE9-052frn0wQwqa6i8_Q"),
        DISCORD("discord", "https://biscuit.codes/discord"),
        GITHUB(
            "github",
            "https://github.com/BiscuitDevelopment/SkyblockAddons"
        ); // Patreon removed due to ending of private betas
        // PATREON("patreon", "https://www.patreon.com/biscuitdev");

        private val resourceLocation = ResourceLocation(
            "skyblockaddons",
            "gui/$resourcePath.png"
        )
        private var url: URI? = null

        init {
            try {
                this.url = URI(url)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
    }

    enum class GUIType {
        MAIN,
        EDIT_LOCATIONS,
        SETTINGS,
        WARP
    }

    enum class ChromaMode(private val TRANSLATION_KEY: String) {
        ALL_SAME_COLOR("settings.chromaModes.allTheSame"),
        FADE("settings.chromaModes.fade");

        val message: String?
            get() = getMessage(TRANSLATION_KEY)

        val nextType: ChromaMode
            get() {
                var nextType = ordinal + 1
                if (nextType > entries.size - 1) {
                    nextType = 0
                }
                return entries[nextType]
            }
    }

    enum class DiscordStatusEntry(private val id: Int) {
        DETAILS(0),
        STATE(1)
    }

    //TODO Fix for Hypixel localization
    enum class SlayerQuest(private val scoreboardName: String) {
        REVENANT_HORROR("Revenant Horror"),
        TARANTULA_BROODFATHER("Tarantula Broodfather"),
        SVEN_PACKMASTER("Sven Packmaster"),
        VOIDGLOOM_SERAPH("Voidgloom Seraph"),
        INFERNO_DEMONLORD("Inferno Demonlord");

        companion object {
            fun fromName(scoreboardName: String): SlayerQuest? {
                for (slayerQuest in entries) {
                    if (slayerQuest.scoreboardName == scoreboardName) {
                        return slayerQuest
                    }
                }

                return null
            }
        }
    }
}
