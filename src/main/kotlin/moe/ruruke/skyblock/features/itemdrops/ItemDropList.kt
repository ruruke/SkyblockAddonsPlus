package moe.ruruke.skyblock.features.itemdrops

import moe.ruruke.skyblock.core.ItemRarity


/**
 * This is the list used by the Stop Dropping/Selling Rare Items feature to determine if items can be dropped or sold.
 * It is used by [ItemDropChecker] to check if items can be dropped/sold.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
internal class ItemDropList {
    /** Items in the inventory (excluding the hotbar) that are at or above this rarity are prohibited from being dropped/sold  */
    private val minimumInventoryItemRarity: ItemRarity? = null

    fun GetMinimumInventoryItemRarity(): ItemRarity? {
        return minimumInventoryItemRarity
    }

    /** Items in the hotbar that are at or above this rarity are prohibited from being dropped/sold  */
    private val minimumHotbarItemRarity: ItemRarity? = null
    fun GetMinimumHotbarItemRarity(): ItemRarity? {
        return minimumHotbarItemRarity
    }

    /** Items with a rarity below the minimum that can't be dropped, takes precedence over the whitelist  */
    private val dontDropTheseItems: List<String>? = null
    fun GetDontDropTheseItems(): List<String>? {
        return dontDropTheseItems
    }

    /** Items with a rarity above the minimum that is allowed to be dropped  */
    private val allowDroppingTheseItems: List<String>? = null
    fun GetAllowDroppingTheseItems(): List<String>? {
        return allowDroppingTheseItems
    }
}
