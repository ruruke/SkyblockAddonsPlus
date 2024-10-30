package moe.ruruke.skyblock.features.craftingpatterns

import moe.ruruke.skyblock.features.ItemDiff
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

/**
 * Crafting patterns enum, constants and utility methods
 *
 * @author DidiSkywalker
 */
enum class CraftingPattern(val index: Int, val pattern: IntArray?) {
    FREE(0, null),
    THREE(
        1, intArrayOf(
            1, 1, 1,
            0, 0, 0,
            0, 0, 0
        )
    ),
    FIVE(
        2, intArrayOf(
            1, 1, 1,
            1, 1, 0,
            0, 0, 0
        )
    ),
    SIX(
        3, intArrayOf(
            1, 1, 1,
            1, 1, 1,
            0, 0, 0
        )
    );

    /**
     * Check if a translated slot is within the pattern
     *
     * @param slot Slot index translated with [.slotToCraftingGridIndex]
     * @return Whether that slot is within the pattern
     */
    fun isSlotInPattern(slot: Int): Boolean {
        return slot >= 0 && slot <= 8 && pattern!![slot] == 1
    }

    /**
     * Checks the items of a crafting grid against the pattern for these characteristics:
     * - filled: Every expected slot is filled, but other slots may be filled too
     * - satisfied: Only expected slots are filled
     * - free space: Amount of items that still fit into the stacks inside the patterns
     *
     * @param grid ItemStack array of length 9 containing the items of the crafting grid
     * @return [CraftingPatternResult] containing all above mentioned characteristics
     */
    fun checkAgainstGrid(grid: Array<ItemStack?>): CraftingPatternResult {
        require(!(grid == null || grid.size < 9)) { "grid cannot be null or smaller than 9." }

        var filled = true
        var satisfied = true
        var emptySpace = 0
        val freeSpaceMap: MutableMap<String, ItemDiff?> = HashMap()

        for (i in pattern!!.indices) {
            val itemStack = grid[i]
            val hasStack = itemStack != null

            if (isSlotInPattern(i) && !hasStack) {
                filled = false
                satisfied = false
            } else if (!isSlotInPattern(i) && hasStack) {
                satisfied = false
            }

            if (isSlotInPattern(i)) {
                if (hasStack) {
                    if (!freeSpaceMap.containsKey(itemStack!!.displayName)) {
                        freeSpaceMap[itemStack.displayName] = ItemDiff(itemStack.displayName, 0)
                    }

                    val diff = freeSpaceMap[itemStack.displayName]
                    diff!!.add(itemStack.maxStackSize - itemStack.stackSize)
                } else {
                    // empty slot inside the pattern: add 64 free space
                    emptySpace += 64
                }
            }
        }

        return CraftingPatternResult(filled, satisfied, emptySpace, freeSpaceMap)
    }

    companion object {
        val ICONS: ResourceLocation = ResourceLocation("skyblockaddons", "craftingpatterns.png")

        /**
         * Displayname of the SkyBlock crafting table
         */
        const val CRAFTING_TABLE_DISPLAYNAME: String = "Craft Item"

        /**
         * Slot index of the crafting result
         */
        const val CRAFTING_RESULT_INDEX: Int = 23

        val CRAFTING_GRID_SLOTS: List<Int> = mutableListOf(
            10, 11, 12,
            19, 20, 21,
            28, 29, 30
        )

        /**
         * Translates a slot index to the corresponding index in the crafting grid between 0 and 8 or
         * return -1 if the slot is not within the crafting grid.
         *
         * @param slotIndex Slot index to translate
         * @return index 0-8 or -1 if not in the crafting grid
         */
        fun slotToCraftingGridIndex(slotIndex: Int): Int {
            return CRAFTING_GRID_SLOTS.indexOf(slotIndex)
        }

        /**
         * Translate a crafting grid index to the corresponding slot index in the full inventory
         *
         * @param index Crafting grid index 0-8
         * @return Slot index
         */
        fun craftingGridIndexToSlot(index: Int): Int {
            if (index < 0 || index > 8) {
                throw IndexOutOfBoundsException("Crafting Grid index must be between 0 and 8")
            }

            return CRAFTING_GRID_SLOTS[index]
        }
    }
}
