package moe.ruruke.skyblock.features.tabtimers

import jdk.nashorn.internal.objects.annotations.Getter
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.utils.RomanNumeralParser
import java.util.*
import java.util.regex.Pattern

/**
 * Helper class for accessing Potion Effect and Power Up timers to draw on screen.
 */
class TabEffectManager {
    /**
     * The following two fields are accessed by
     * [codes.biscuit.skyblockaddons.listeners.RenderListener.drawPotionEffectTimers] to retrieve lists for drawing.
     *
     *
     * Both return a list of current Potion or Powerup timers. They can be empty, but are never null.
     */
    private val potionTimers: MutableList<TabEffect> = ArrayList()
    fun getPotionTimers(): List<TabEffect> {
        return potionTimers
    }

    private val powerupTimers: MutableList<TabEffect> = ArrayList()
    fun powerupTimer(): MutableList<TabEffect> {
        return powerupTimers
    }
    fun getEffectCount(): Int {
        return effectCount
    }

    private var effectCount = 0
    fun effectCount(): Int {
        return effectCount
    }

    /**
     * Adds a potion effect to the ones currently being displayed.
     *
     * @param potionEffect The potion effect text to be added.
     */
    fun putPotionEffect(potionEffect: String, timer: String) {
        putEffect(TabEffect(potionEffect, timer), potionTimers)
    }

    /**
     * Adds a powerup to the ones currently being displayed.
     *
     * @param powerup The powerup text to be added.
     */
    fun putPowerup(powerup: String, timer: String) {
        putEffect(TabEffect(powerup, timer), powerupTimers)
    }

    /**
     * Adds the effect to the specified list, after replacing the roman numerals on it- if applicable.
     *
     * @param effect The potion effect/powerup text to be added.
     * @param list The list to add it to (either potionTimers or powerupTimers).
     */
    private fun putEffect(effect: TabEffect, list: MutableList<TabEffect>) {
        if (NewConfig.isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
            effect.setEffect(RomanNumeralParser.replaceNumeralsWithIntegers(effect.getEffect()))
        }
        list.add(effect)
    }

    /**
     * Called by [codes.biscuit.skyblockaddons.listeners.PlayerListener.onTick] every second
     * to update the list of current effect timers.
     */
    fun update(tabFooterString: String?, strippedTabFooterString: String) {
        potionTimers.clear()
        powerupTimers.clear()

        //System.out.println(strippedTabFooterString);
        if (tabFooterString == null) {
            return
        }

        // Match the TabFooterString for Effects
        var matcher = EFFECT_PATTERN.matcher(tabFooterString)
        var effectString: String
        while (matcher.find()) {
            if ((matcher.group("potion").also { effectString = it }) != null) {
                putPotionEffect(effectString, matcher.group("timer"))
            } else if ((matcher.group("powerup").also { effectString = it }) != null) {
                putPowerup(effectString, matcher.group("timer"))
            }
        }

        matcher = EFFECT_COUNT_PATTERN.matcher(strippedTabFooterString)
        if (matcher.find()) {
            effectCount = matcher.group("effectCount").toInt()
        } else if ((GOD_POTION_PATTERN.matcher(strippedTabFooterString).also { matcher = it }).find()) {
            // Hard code
            putPotionEffect("§cGod Potion§r ", matcher.group("timer"))
            effectCount = 32
        } else {
            effectCount = 0
        }
    }

    companion object {
        /** The main TabEffectManager instance.  */
        private val instance = TabEffectManager()
        fun getInstance(): TabEffectManager {
            return instance
        }

        /**
         * Used to match potion effects from the footer.
         */
        private val EFFECT_PATTERN: Pattern =
            Pattern.compile("(?:(?<potion>§r§[a-f0-9][a-zA-Z ]+ (?:I[XV]|V?I{0,3})§r )|(?<powerup>§r§[a-f0-9][a-zA-Z ]+ ))§r§f(?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})")
        private val EFFECT_COUNT_PATTERN: Pattern = Pattern.compile("You have (?<effectCount>[0-9]+) active effects\\.")
        private val GOD_POTION_PATTERN: Pattern =
            Pattern.compile("You have a God Potion active! (?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})")

        /**
         * The following two fields are accessed by
         * [codes.biscuit.skyblockaddons.listeners.RenderListener.drawPotionEffectTimers]
         * to retrieve dummy lists for drawing when editing GUI locations while no Effects are active.
         *
         *
         * Both return a list of dummy Potion or Powerup timers.
         */
        private val dummyPotionTimers: List<TabEffect> = Arrays.asList(
            TabEffect("§r§ePotion Effect II ", "12:34"),
            TabEffect("§r§aEnchanting XP Boost III ", "1:23:45")
        )
        fun getDummyPotionTimers(): List<TabEffect> {
            return dummyPowerupTimers
        }

        private val dummyPowerupTimers = listOf(
            TabEffect("§r§bHoming Snowballs ", "1:39")
        )
        fun getDummyPowerupTimers(): List<TabEffect> {
            return dummyPowerupTimers
        }
    }
}