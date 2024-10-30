package moe.ruruke.skyblock.core

import net.minecraft.util.ResourceLocation
import java.util.*

enum class Language(private val path: String) {
    // listed by popularity
    ENGLISH("en_US"),
    CHINESE_TRADITIONAL("zh_TW"),
    CHINESE_SIMPLIFIED("zh_CN"),
    HINDI("hi_IN"),
    SPANISH_SPAIN("es_ES"),
    SPANISH_MEXICO("es_MX"),
    FRENCH("fr_FR"),
    ARABIC("ar_SA"),
    RUSSIAN("ru_RU"),
    PORTUGUESE_PORTUGAL("pt_PT"),
    PORTUGUESE_BRAZIL("pt_BR"),
    GERMAN("de_DE"),
    JAPANESE("ja_JP"),
    TURKISH("tr_TR"),
    KOREAN("ko_KR"),
    VIETNAMESE("vi_VN"),
    ITALIAN("it_IT"),
    THAI("th_TH"),

    //rest listed alphabetically
    BULGARIAN("bg_BG"),
    CROATIAN("hr_HR"),
    CZECH("cs_CZ"),
    DANISH("da_DK"),
    DUTCH("nl_NL"),
    ESTONIAN("et_EE"),
    FINNISH("fi_FI"),
    GREEK("el_GR"),
    HEBREW("he_IL"),
    HUNGARIAN("hu_HU"),
    INDONESIAN("id_ID"),
    LITHUANIAN("lt_LT"),
    MALAY("ms_MY"),
    NORWEGIAN("no_NO"),
    PERSIAN("fa_IR"),
    POLISH("pl_PL"),
    ROMANIAN("ro_RO"),
    SERBIAN_LATIN("sr_CS"),
    SLOVENIAN("sl_SI"),
    SWEDISH("sv_SE"),
    UKRAINIAN("uk_UA"),
    PIRATE_ENGLISH("en_PT"),
    LOLCAT("lol_US"),
    BISCUITISH("bc_BC"),
    OWO("ow_Wo");

    private val resourceLocation: ResourceLocation

    init {
        this.resourceLocation =
            ResourceLocation("skyblockaddons", "flags/" + path.lowercase(Locale.getDefault()) + ".png")
    }

    fun getResourceLocation(): ResourceLocation {
        return resourceLocation
    }

    fun getPath(): String {
        return path
    }

    companion object {
        /**
         * Find the corresponding [Language] to a given key string like `en_US`.
         * Case-insensitive.
         *
         * @param languageKey The lanugage key to look for.
         * @return The language if one was found, or null.
         */
        fun getFromPath(languageKey: String?): Language? {
            for (language: Language in entries) {
                val path: String? = language.path
                if (path != null && path.equals(languageKey, ignoreCase = true)) {
                    return language
                }
            }
            return null
        }
    }
}
