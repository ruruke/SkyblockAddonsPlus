package moe.ruruke.skyblock.features.dragontracker



import moe.ruruke.skyblock.core.Translations.getMessage
import moe.ruruke.skyblock.utils.ColorCode


enum class DragonType(color: ColorCode) {
    PROTECTOR(ColorCode.DARK_BLUE),
    OLD(ColorCode.GRAY),
    WISE(ColorCode.BLUE),
    UNSTABLE(ColorCode.BLACK),
    YOUNG(ColorCode.WHITE),
    STRONG(ColorCode.RED),
    SUPERIOR(ColorCode.GOLD);

    
    private val color: ColorCode? = null

    val displayName: String?
        get() = getMessage("dragonTracker." + name.lowercase())

    companion object {
        fun fromName(name: String): DragonType? {
            for (dragonType in entries) {
                if (dragonType.name == name.uppercase()) {
                    return dragonType
                }
            }

            return null
        }
    }
}
