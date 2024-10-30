package moe.ruruke.skyblock.core

import moe.ruruke.skyblock.utils.ColorCode

/**
 * Skyblock item rarity definitions
 */
enum class ItemRarity(
    /** The name of the rarity as displayed in an item's lore  */
    private val loreName: String, colorCode: ColorCode
) {
    COMMON("COMMON", ColorCode.WHITE),
    UNCOMMON("UNCOMMON", ColorCode.GREEN),
    RARE("RARE", ColorCode.BLUE),
    EPIC("EPIC", ColorCode.DARK_PURPLE),
    LEGENDARY("LEGENDARY", ColorCode.GOLD),
    MYTHIC("MYTHIC", ColorCode.LIGHT_PURPLE),
    DIVINE("DIVINE", ColorCode.AQUA),

    SPECIAL("SPECIAL", ColorCode.RED),
    VERY_SPECIAL("VERY SPECIAL", ColorCode.RED);

    /** The color code for the color of the rarity as it's displayed in an item's lore  */
    private val colorCode: ColorCode

    fun getColorCode(): ColorCode {
        return colorCode
    }
    init {
        this.colorCode = colorCode
    }

    fun getLoreName() = loreName
}
