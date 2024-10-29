package moe.ruruke.skyblock.core.enchantedItemBlacklist

import moe.ruruke.skyblock.core.ItemRarity


/**
 * This is the blacklist and whitelist used by the "Avoid Placing Enchanted Items" feature to determine which items to block.
 * This list is loaded from a file in [DataUtils].
 *
 * @see EnchantedItemPlacementBlocker
 */
class EnchantedItemLists
/**
 * Creates a new instance of `EnchantedItemLists` with variables set to the values given.
 *
 * @param blacklistedIDs the list of item IDs of the enchanted items to blacklist
 * @param rarityLimit the minimum rarity to blacklist
 */(
    /** This is the list of all the item IDs of the enchanted items that the player will not be allowed to place on their island.  */
    var blacklistedIDs: List<String>,
    /**
     * This is the list of all the item IDs of the enchanted items above the rarity limit that the player will be allowed
     * to place on their island.
     */
    var whitelistedIDs: List<String>,
    /** This is the minimum rarity to block for enchanted items that aren't yet on one of the lists.  */
    var rarityLimit: ItemRarity
) 