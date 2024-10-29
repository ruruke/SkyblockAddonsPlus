package moe.ruruke.skyblock.utils

import lombok.Getter
import lombok.Setter
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getLogger
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.core.*
import org.apache.commons.lang3.StringUtils
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Class to parse action bar messages and get stats and other info out of them.
 * Parses things like health, defense, mana, skill xp, item ability tickers and
 * if they are displayed else where by SBA, removes them from the action bar.
 *
 *
 * Action bars can take many shapes, but they're always divided into sections separated by 3 or more spaces
 * (usually 5, zombie tickers by 4, race timer by 12, trials of fire by 3).
 * Here are some examples:
 *
 *
 * Normal:                     §c1390/1390❤     §a720§a❈ Defense     §b183/171✎ Mana§r
 * Normal with Skill XP:       §c1390/1390❤     §3+10.9 Combat (313,937.1/600,000)     §b183/171✎ Mana§r
 * Zombie Sword:               §c1390/1390❤     §a725§a❈ Defense     §b175/233✎ Mana    §a§lⓩⓩⓩⓩ§2§l§r
 * Zombie Sword with Skill XP: §c1390/1390❤     §3+10.9 Combat (313,948/600,000)     §b187/233✎ Mana    §a§lⓩⓩⓩⓩ§2§l§r
 * Normal with Wand:           §c1390/1390❤+§c30▅     §a724§a❈ Defense     §b97/171✎ Mana§r
 * Normal with Absorption:     §61181/1161❤     §a593§a❈ Defense     §b550/550✎ Mana§r
 * Normal with Absorp + Wand:  §61181/1161❤+§c20▆     §a593§a❈ Defense     §b501/550✎ Mana§r
 * End Race:                   §d§lTHE END RACE §e00:52.370            §b147/147✎ Mana§r
 * Woods Race:                 §A§LWOODS RACING §e00:31.520            §b147/147✎ Mana§r
 * Trials of Fire:             §c1078/1078❤   §610 DPS   §c1 second     §b421/421✎ Mana§r
 * Soulflow:                   §b421/421✎ §3100ʬ
 * Tethered + Alignment:      §a1039§a❈ Defense§a |||§a§l  T3!
 * Five stages of healing wand:     §62151/1851❤+§c120▆
 * §62151/1851❤+§c120▅
 * §62151/1851❤+§c120▄
 * §62151/1851❤+§c120▃
 * §62151/1851❤+§c120▂
 * §62151/1851❤+§c120▁
 *
 *
 * To add something new to parse, add an else-if case in [.parseActionBar] to call a method that
 * parses information from that section.
 */
@Getter
class ActionBarParser {
    /**
     * The amount of usable tickers or -1 if none are in the action bar.
     */
    private var tickers = -1

    /**
     * The total amount of possible tickers or 0 if none are in the action bar.
     */
    private var maxTickers = 0

    @Setter
    private val lastSecondHealth = -1f

    @Setter
    private val healthUpdate: Float? = null

    @Setter
    private val lastHealthUpdate: Long = 0

    private var currentSkillXP = 0f
    private var totalSkillXP = 0
    private var percent = 0f
    private var healthLock = false
    private var otherDefense: String? = null

    /** The skill section that was parsed from the last action bar message  */
    private var lastParsedSkillSection = ""

    /** The string that was displayed on the skill progress display for the last action bar message  */
    private var lastSkillProgressString: String? = null

    /** The skill type parsed from the last action bar message  */
    private var lastSkillType: SkillType? = null

    private val stringsToRemove = LinkedList<String>()

    /**
     * Parses the stats out of an action bar message and returns a new action bar message without the parsed stats
     * to display instead.
     * Looks for Health, Defense, Mana, Skill XP and parses and uses the stats accordingly.
     * Only removes the stats from the new action bar when their separate display features are enabled.
     *
     * @param actionBar Formatted action bar message
     * @return New action bar without parsed stats.
     */
    fun parseActionBar(actionBar: String): String {
        // First split the action bar into sections
        val splitMessage = actionBar.split(" {3,}".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        // This list holds the text of unused sections that aren't displayed anywhere else in SBA
        // so they can keep being displayed in the action bar
        val unusedSections: MutableList<String> = LinkedList()
        stringsToRemove.clear()

        // health and mana section methods determine if prediction can be disabled, so enable both at first
        //TODO:
//        main.getRenderListener().setPredictMana(true)
//        main.getRenderListener().setPredictHealth(true)
        // set ticker to -1 so the GUI element doesn't get displayed while they're not displayed in the action bar
        tickers = -1

        // If the action bar is displaying player stats and the defense section is absent, the player's defense is zero.
        if (actionBar.contains("❤") && !actionBar.contains("❈") && splitMessage.size == 2) {
            setAttribute(Attribute.DEFENCE, 0f)
        }

        for (section in splitMessage) {
            try {
                val sectionReturn = parseSection(section)
                if (sectionReturn != null) {
                    // can either return a string to keep displaying in the action bar
                    // or null to not display them anymore
                    unusedSections.add(sectionReturn)
                } else {
                    // Remove via callback
                    stringsToRemove.add(section)
                }
            } catch (ex: Exception) {
                unusedSections.add(section)
            }
        }

        // Finally, display all unused sections separated by 5 spaces again
        return java.lang.String.join(StringUtils.repeat(" ", 5), unusedSections)
    }

    /**
     * Parses a single section of the action bar.
     *
     * @param section Section to parse
     * @return Text to keep displaying or null
     */
    private fun parseSection(section: String): String? {
        var section = section
        val stripColoring = TextUtils.stripColor(section)
        var convertMag: String

        try {
            convertMag = TextUtils.convertMagnitudes(stripColoring)

            // Format for overflow mana is a bit different. Splitstats must parse out overflow first before getting numbers
            if (section.contains("ʬ")) {
                convertMag = convertMag.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
            val numbersOnly = TextUtils.getNumbersOnly(convertMag).trim() // keeps numbers and slashes
            val splitStats = numbersOnly.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (section.contains("❤")) {
                // cutting the crimson stack information out
                section = parseCrimsonArmorAbilityStack(section)

                // Fixing health when glare damage (from magma boss in crimson isle) is displayed.
                // Glare damage stays in the action bar normally
                if (section.endsWith("ಠ")) {
                    if (section.contains("Glare Damage")) {
                        section = section.split(Pattern.quote("§6 ").toRegex())
                            .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    }
                }

                // ❤ indicates a health section
                return parseHealth(section)
            } else if (section.contains("❈")) {
                // ❈ indicates a defense section
                return parseDefense(section)
            } else if (section.endsWith("§f❂ True Defense")) {
                return parseTrueDefence(section)
            } else if (section.contains("✎")) {
                return parseMana(section)
            } else if (section.contains("(")) {
                return parseSkill(convertMag)
            } else if (section.contains("Ⓞ") || section.contains("ⓩ")) {
                return parseTickers(section)
            } else if (section.contains("Drill")) {
                return parseDrill(section, splitStats)
            }
        } catch (e: ParseException) {
            logger.error("The section \"$section\" will be skipped due to an error during number parsing.")
            logger.error("Failed to parse number at offset " + e.errorOffset + " in string \"" + e.message + "\".", e)
            return section
        }

        return section
    }

    private fun parseTrueDefence(section: String): String? {
        return if (configValues!!.isEnabled(Feature.HIDE_TRUE_DEFENSE)) null else section
    }

    private fun parseCrimsonArmorAbilityStack(section: String): String {
        var section = section
        for (crimsonArmorAbilityStack in CrimsonArmorAbilityStack.values()) {
            crimsonArmorAbilityStack.setCurrentValue(0)
        }

        var runs = 0
        out@ while (section.contains("  ")) {
            runs++
            if (runs == 5) break

            if (section.endsWith("§r")) {
                section = section.substring(0, section.length - 2)
            }

            for (crimsonArmorAbilityStack in CrimsonArmorAbilityStack.values()) {
                val stackSymbol: String = crimsonArmorAbilityStack.getSymbol()

                if (section.endsWith(stackSymbol)) {
                    val split = section.split("§6".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    var stack = split[split.size - 1]
                    val remove = "§6$stack"
                    if (stack.contains("§l")) {
                        stack = stack.substring(2)
                        if (Feature.CRIMSON_ARMOR_ABILITY_STACKS.isEnabled()) {
                            val realRemove = "$remove§r"
                            stringsToRemove.add(realRemove)
                        }
                    } else {
                        if (Feature.CRIMSON_ARMOR_ABILITY_STACKS.isEnabled()) stringsToRemove.add(remove)
                    }
                    stack = stack.substring(0, stack.length - 1)

                    section = section.substring(0, section.length - remove.length)
                    section = section.trim { it <= ' ' }
                    crimsonArmorAbilityStack.setCurrentValue(stack.toInt())
                    continue@out
                }
            }
        }

        return section
    }

    /**
     * Parses the health section and sets the read values as attributes in [Utils].
     * Returns the healing indicator if a healing Wand is active.
     *
     * @param healthSection Health section of the action bar
     * @return null or Wand healing indicator or `healthSection` if neither health bar nor health text are enabled
     */
    private fun parseHealth(healthSection: String): String {
        // Normal:      §c1390/1390❤
        // With Wand:   §c1390/1390❤+§c30▅
        val separateDisplay =
            configValues!!.isEnabled(Feature.HEALTH_BAR) || configValues!!.isEnabled(Feature.HEALTH_TEXT)
        var returnString = healthSection
        val newHealth: Float
        val maxHealth: Float
        val stripped = TextUtils.stripColor(healthSection)
        val m = HEALTH_PATTERN_S.matcher(stripped)
        if (separateDisplay && m.matches()) {
            newHealth = parseFloat(m.group("health"))
            maxHealth = parseFloat(m.group("maxHealth"))
            if (m.group("wand") != null) {
                // Jank way of doing this for now
                returnString = "" // "§c"+ m.group("wand");
                stringsToRemove.add(stripped.substring(0, m.start("wand")))
            } else {
                stringsToRemove.add(healthSection)
                returnString = ""
            }
            healthLock = false
            val postSetLock = utils!!.attributes[Attribute.MAX_HEALTH]!!
                .value != maxHealth ||
                    (abs(
                        (utils!!.attributes[Attribute.HEALTH]!!
                            .value - newHealth).toDouble()
                    ) / maxHealth) > .05
            setAttribute(Attribute.HEALTH, newHealth)
            setAttribute(Attribute.MAX_HEALTH, maxHealth)
            healthLock = postSetLock
        }
        return returnString
    }

    /**
     * Parses the mana section and sets the read values as attributes in [Utils].
     *
     * @param manaSection Mana section of the action bar
     * @return null or `manaSection` if neither mana bar nor mana text are enabled
     */
    private fun parseMana(manaSection: String): String? {
        // 183/171✎ Mana
        // 421/421✎ 10ʬ
        // 421/421✎ -10ʬ
        val m = MANA_PATTERN_S.matcher(TextUtils.stripColor(manaSection).trim())
        if (m.matches()) {
            setAttribute(Attribute.MANA, parseFloat(m.group("num")))
            setAttribute(Attribute.MAX_MANA, parseFloat(m.group("den")))
            var overflowMana = 0f
            if (m.group("overflow") != null) {
                overflowMana = parseFloat(m.group("overflow"))
            }
            setAttribute(Attribute.OVERFLOW_MANA, overflowMana)
            //TODO:
//            main.getRenderListener().setPredictMana(false)
            if (configValues!!.isEnabled(Feature.MANA_BAR) || configValues!!.isEnabled(Feature.MANA_TEXT)) {
                return null
            }
        }
        return manaSection
    }

    /**
     * Parses the defense section and sets the read values as attributes in [Utils].
     *
     * @param defenseSection Defense section of the action bar
     * @return null or `defenseSection` if neither defense text nor defense percentage are enabled
     */
    private fun parseDefense(defenseSection: String): String? {
        // §a720§a❈ Defense
        // Tethered T1 (Dungeon Healer)--means tethered to 1 person I think: §a1024§a? Defense§6  T1
        // Tethered T3! (Dungeon Healer)--not sure why exclamation mark: §a1039§a? Defense§a§l  T3!
        // Tethered T3! (Dungeon Healer) + Aligned ||| (Gyrokinetic Wand): §a1039§a? Defense§a |||§a§l  T3!
        val stripped = TextUtils.stripColor(defenseSection)
        val m = DEFENSE_PATTERN_S.matcher(stripped)
        if (m.matches()) {
            val defense = parseFloat(m.group("defense"))
            setAttribute(Attribute.DEFENCE, defense)
            otherDefense = TextUtils.getFormattedString(defenseSection, m.group("other").trim { it <= ' ' })
            if (configValues!!.isEnabled(Feature.DEFENCE_TEXT) || configValues!!.isEnabled(Feature.DEFENCE_PERCENTAGE)) {
                return null
            }
        }
        return defenseSection
    }

    /**
     * Parses the skill section and displays the skill progress gui element.
     * If the skill section provided is the same as the one from the last action bar message, then the last output is
     * displayed.
     *
     *
     *
     * **Example Skill Section Messages**
     *
     *
     * §3+10.9 Combat (313,937.1/600,000)
     *
     *
     * Another Example: §5+§d30 §5Runecrafting (969/1000)
     *
     *
     * Percent: §3+2 Farming (1.01%)
     *
     *
     * Percent without decimal: §3+2 Farming (1%)
     *
     *
     * Maxed out skill: §5+§d60 §5Runecrafting (118,084/0)
     *
     * @param skillSection Skill XP section of the action bar
     * @return `null` or `skillSection` if wrong format or skill display is disabled
     */
    @Throws(ParseException::class)
    private fun parseSkill(skillSection: String): String? {
        if (configValues!!.isEnabled(Feature.SKILL_DISPLAY) || configValues!!.isEnabled(Feature.SKILL_PROGRESS_BAR)) {
            val matcher = SKILL_GAIN_PATTERN_S.matcher(TextUtils.stripColor(skillSection))
            val nf = NumberFormat.getInstance(Locale.US)
            val skillTextBuilder = StringBuilder()
            var skillType: SkillType? = null

            nf.maximumFractionDigits = 2

            if (lastParsedSkillSection == skillSection) {
                skillTextBuilder.append(lastSkillProgressString)
                skillType = lastSkillType
            } else if (matcher.matches()) {
                if (configValues!!.isEnabled(Feature.SHOW_SKILL_XP_GAINED)) {
                    skillTextBuilder.append("+").append(matcher.group("gained"))
                }

                skillType = SkillType.getFromString(matcher.group("skillName"))
                //TODO:
//                val skillPercent = matcher.group("percent") != null
                val skillPercent = false
                var parseCurrAndTotal = true

                if (skillPercent) {
                    //TODO:
//                    percent = nf.parse(matcher.group("percent")).toFloat()
//                    val skillLevel: Int = main.getSkillXpManager().getSkillLevel(skillType)
//                    // Try to re-create xxx/xxx display
//                    if (skillLevel != -1) {
//                        totalSkillXP = main.getSkillXpManager().getSkillXpForNextLevel(skillType, skillLevel)
//                        currentSkillXP = totalSkillXP * percent / 100
//                    } else {
//                        parseCurrAndTotal = false
//                    }
                } else {
                    currentSkillXP = nf.parse(matcher.group("current")).toFloat()
                    totalSkillXP = nf.parse(matcher.group("total")).toInt()
                    percent = if (totalSkillXP == 0) 100f else 100f * currentSkillXP / totalSkillXP
                }
                percent = min(100.0, percent.toDouble()).toFloat()


                if (!parseCurrAndTotal || configValues!!.isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
                    // We may only have the percent at this point
                    skillTextBuilder.append(" (").append(String.format("%.2f", percent)).append("%)")
                } else {
                    // Append "(currentXp/totalXp)"
                    skillTextBuilder.append(" (").append(nf.format(currentSkillXP.toDouble()))
                    // Only print the total when it doesn't = 0
                    if (totalSkillXP != 0) {
                        skillTextBuilder.append("/")
                        if (configValues!!.isEnabled(Feature.ABBREVIATE_SKILL_XP_DENOMINATOR)) {
                            skillTextBuilder.append(TextUtils.abbreviate(totalSkillXP))
                        } else {
                            skillTextBuilder.append(nf.format(totalSkillXP.toLong()))
                        }
                    }
                    skillTextBuilder.append(")")
                }

                // This feature is only accessible when we have parsed the current and total skill xp
                if (parseCurrAndTotal && configValues!!.isEnabled(Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
                    val gained = nf.parse(matcher.group("gained")).toFloat()

                    skillTextBuilder.append(" - ")

                    if (percent != 100f) {
                        if (gained != 0f) {
                            skillTextBuilder.append(
                                Translations.getMessage(
                                    "messages.actionsLeft",
                                    ceil(((totalSkillXP - currentSkillXP) / gained).toDouble()) as Int
                                )
                            )
                        } else {
                            skillTextBuilder.append(Translations.getMessage("messages.actionsLeft", "∞"))
                        }
                    }
                }

                lastParsedSkillSection = skillSection
                lastSkillProgressString = skillTextBuilder.toString()
                lastSkillType = skillType
            }

            //TODO:
//            if (skillTextBuilder.length != 0) {
//                main.getRenderListener().setSkillText(skillTextBuilder.toString())
//                main.getRenderListener().setSkill(skillType)
//                main.getRenderListener().setSkillFadeOutTime(System.currentTimeMillis() + 4000)
//                if (configValues!!.isEnabled(Feature.SKILL_DISPLAY)) {
//                    return null
//                }
//            }
        }

        return skillSection
    }

    /**
     * Parses the ticker section and updates [.tickers] and [.maxTickers] accordingly.
     * [.tickers] being usable tickers and [.maxTickers] being the total amount of possible tickers.
     *
     * @param tickerSection Ticker section of the action bar
     * @return null or `tickerSection` if the ticker display is disabled
     */
    private fun parseTickers(tickerSection: String): String? {
        // Zombie with full charges: §a§lⓩⓩⓩⓩ§2§l§r
        // Zombie with one used charges: §a§lⓩⓩⓩ§2§lⓄ§r
        // Scorpion tickers: §e§lⓄⓄⓄⓄ§7§l§r
        // Ornate: §e§lⓩⓩⓩ§6§lⓄⓄ§r

        // Zombie uses ⓩ with color code a for usable charges, Ⓞ with color code 2 for unusable
        // Scorpion uses Ⓞ with color code e for usable tickers, Ⓞ with color code 7 for unusable
        // Ornate uses ⓩ with color code e for usable charges, Ⓞ with color code 6 for unusable

        tickers = 0
        maxTickers = 0
        var hitUnusables = false
        for (character in tickerSection.toCharArray()) {
            if (!hitUnusables && (character == '7' || character == '2' || character == '6')) {
                // While the unusable tickers weren't hit before and if it reaches a grey(scorpion) or dark green(zombie)
                // or gold (ornate) color code, it means those tickers are used, so stop counting them.
                hitUnusables = true
            } else if (character == 'Ⓞ' || character == 'ⓩ') { // Increase the ticker counts
                if (!hitUnusables) {
                    tickers++
                }
                maxTickers++
            }
        }
        return if (configValues!!.isEnabled(Feature.TICKER_CHARGES_DISPLAY)) {
            null
        } else {
            tickerSection
        }
    }


    /**
     * Parses the drill section
     *
     * @param drillSection Drill fuel section of the action bar
     * @return null or `drillSection` if wrong format or drill display is disabled
     */
    private fun parseDrill(drillSection: String, splitStats: Array<String>): String? {
        // §21,798/3k Drill Fuel§r
        // splitStats should convert into [1798, 3000]
        val fuel: Int = max(0.0, splitStats[0].toDouble()).toInt()
        val maxFuel: Int = max(1.0, splitStats[1].toDouble()).toInt()
        setAttribute(Attribute.FUEL, fuel.toFloat())
        setAttribute(Attribute.MAX_FUEL, maxFuel.toFloat())
        return if (configValues!!.isEnabled(Feature.DRILL_FUEL_BAR) || configValues!!.isEnabled(
                Feature.DRILL_FUEL_TEXT
            )
        ) {
            null
        } else {
            drillSection
        }
    }

    /**
     * Parses a float from a given string.
     *
     * @param string the string to parse
     * @return the parsed float or `-1` if parsing was unsuccessful
     */
    private fun parseFloat(string: String): Float {
        return try {
            TextUtils.NUMBER_FORMAT.parse(string).toFloat()
        } catch (e: ParseException) {
            (-1).toFloat()
        }
    }

    /**
     * Sets an attribute in [Utils]
     * Ignores health if it's locked
     *
     * @param attribute Attribute
     * @param value     Attribute value
     */
    private fun setAttribute(attribute: Attribute, value: Float) {
        if (attribute == Attribute.HEALTH && healthLock) return
        utils!!.attributes[attribute]!!.setValue(value)
    }

    companion object {
        private val COLLECTIONS_CHAT_PATTERN: Pattern =
            Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((((?<current>[0-9.,kM]+)/(?<total>[0-9.,kM]+))|((?<percent>[0-9.,]+)%))\\))")
        private val SKILL_GAIN_PATTERN_S: Pattern =
            Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((((?<current>[0-9.,]+)/(?<total>[0-9.,]+))|((?<percent>[0-9.]+)%))\\))")
        private val MANA_PATTERN_S: Pattern =
            Pattern.compile("(?<num>[0-9,.]+)/(?<den>[0-9,.]+)✎(| Mana| (?<overflow>-?[0-9,.]+)ʬ)")
        private val DEFENSE_PATTERN_S: Pattern =
            Pattern.compile("(?<defense>[0-9,.]+)❈ Defense(?<other>( (?<align>\\|\\|\\|))?( {2}(?<tether>T[0-9,.]+!?))?.*)?")
        private val HEALTH_PATTERN_S: Pattern =
            Pattern.compile("(?<health>[0-9,.]+)/(?<maxHealth>[0-9,.]+)❤(?<wand>\\+(?<wandHeal>[0-9,.]+)[▆▅▄▃▂▁])?")


        private val main = instance
        private val logger = getLogger()
    }
}
