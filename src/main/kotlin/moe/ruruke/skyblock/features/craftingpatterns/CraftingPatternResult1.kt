package moe.ruruke.skyblock.features.craftingpatterns

import moe.ruruke.skyblock.features.ItemDiff
import net.minecraft.item.ItemStack

/**
 * Class containing results of pattern checks through [CraftingPattern.checkAgainstGrid]
 */
class CraftingPatternResult internal constructor(
    /**
     * A pattern is considered filled if at least every expected slot is filled. Other slots may be filled too.
     *
     * @return Whether the checked grid filled the pattern
     */
    val isFilled: Boolean,
    /**
     * A pattern is considered satisfied if every expected slot and no other slots are filled.
     *
     * @return Whether the checked grid filled the pattern
     */
    val isSatisfied: Boolean, private val emptySpace: Int, private val freeSpaceMap: Map<String, ItemDiff?>
) {
    /**
     * Checks whether a given ItemStack can still fit inside without violating the pattern.
     *
     * @param itemStack ItemStack to check
     * @return Whether that ItemStack can safely fit the pattern
     */
    fun fitsItem(itemStack: ItemStack): Boolean {
        val itemDiff = freeSpaceMap.getOrDefault(itemStack.displayName, null)
        return if (itemDiff != null) {
            itemStack.stackSize <= itemDiff.getAmount()
        } else {
            itemStack.stackSize <= emptySpace
        }
    }
}
