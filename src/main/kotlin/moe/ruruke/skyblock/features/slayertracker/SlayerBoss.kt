package moe.ruruke.skyblock.features.slayertracker

import com.google.common.collect.Lists
import moe.ruruke.skyblock.features.slayertracker.SlayerDrop.*
import lombok.Getter
import moe.ruruke.skyblock.core.Translations
import kotlin.collections.ArrayList

enum class SlayerBoss(@field:Getter private val mobType: String, vararg drops: SlayerDrop?) {
    REVENANT(
        "Zombie", REVENANT_FLESH, FOUL_FLESH, PESTILENCE_RUNE, UNDEAD_CATALYST, SMITE_SIX, BEHEADED_HORROR,
        REVENANT_CATALYST, SNAKE_RUNE, SCYTHE_BLADE, SMITE_SEVEN, SHARD_OF_SHREDDED, WARDEN_HEART
    ),

    TARANTULA(
        "Spider", TARANTULA_WEB, TOXIC_ARROW_POISON, SPIDER_CATALYST, BANE_OF_ARTHROPODS_SIX, BITE_RUNE,
        FLY_SWATTER, TARANTULA_TALISMAN, DIGESTED_MOSQUITO
    ),

    SVEN(
        "Wolf",
        WOLF_TOOTH,
        HAMSTER_WHEEL,
        SPIRIT_RUNE,
        CRITICAL_SIX,
        FURBALL,
        RED_CLAW_EGG,
        COUTURE_RUNE,
        OVERFLUX_CAPACITOR,
        GRIZZLY_BAIT
    ),

    VOIDGLOOM(
        "Enderman", NULL_SPHERE, TWILIGHT_ARROW_POISON, ENDERSNAKE_RUNE, SUMMONING_EYE, MANA_STEAL_ONE,
        TRANSMISSION_TUNER, NULL_ATOM, HAZMAT_ENDERMAN, POCKET_ESPRESSO_MACHINE, SMARTY_PANTS_ONE, END_RUNE,
        HANDY_BLOOD_CHALICE, SINFUL_DICE, EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER, VOID_CONQUEROR_ENDERMAN_SKIN,
        ETHERWARP_MERGER, JUDGEMENT_CORE, ENCHANT_RUNE, ENDER_SLAYER_SEVEN
    );

    private val drops: ArrayList<SlayerDrop?>? = Lists.newArrayList(*drops)
    fun getDrop(): ArrayList<SlayerDrop?>? {
        return drops
    }

    val displayName: String
        get() = Translations.getMessage("slayerTracker." + name.lowercase())!!

    companion object {
        fun getFromMobType(mobType: String?): SlayerBoss? {
            for (slayerBoss in entries) {
                if (slayerBoss.mobType.equals(mobType, ignoreCase = true)) {
                    return slayerBoss
                }
            }

            return null
        }
    }
}
