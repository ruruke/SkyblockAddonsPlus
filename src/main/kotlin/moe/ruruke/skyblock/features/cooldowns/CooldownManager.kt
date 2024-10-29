package moe.ruruke.skyblock.features.cooldowns

import moe.ruruke.skyblock.utils.ItemUtils.Companion.getItemLore
import moe.ruruke.skyblock.utils.ItemUtils.Companion.getSkyblockItemID
import moe.ruruke.skyblock.utils.TextUtils.Companion.stripColor
import net.minecraft.item.ItemStack
import java.util.regex.Pattern

/**
 * Manager class for items on cooldown. <br></br>
 * Register a new item with [put][.put] and check cooldowns with
 * [isOnCooldown][.isOnCooldown], [getRemainingCooldown][.getRemainingCooldown] and
 * [getRemainingCooldownPercent][.getRemainingCooldownPercent]
 */
object CooldownManager {
    private var itemCooldowns: Map<String?, Int> = HashMap()

    fun setItemCooldowns(cooldown: java.util.HashMap<String?, Int>) {
        itemCooldowns = cooldown
    }

    private val ITEM_COOLDOWN_PATTERN: Pattern = Pattern.compile("Cooldown: ([0-9]+)s")
    private val ALTERNATE_COOLDOWN_PATTERN: Pattern = Pattern.compile("([0-9]+) Second Cooldown")

    private val cooldowns: MutableMap<String?, CooldownEntry> = HashMap()

    private fun get(item: ItemStack): CooldownEntry {
        return get(getSkyblockItemID(item))
    }

    private fun get(itemId: String?): CooldownEntry {
        return cooldowns.getOrDefault(itemId, CooldownEntry.Companion.NULL_ENTRY)
    }

    fun getItemCooldown(item: ItemStack?): Int {
        return itemCooldowns.getOrDefault(getSkyblockItemID(item), 0)
    }

    fun getItemCooldown(itemId: String?): Int {
        return itemCooldowns.getOrDefault(itemId, 0)
    }

    /**
     * Put an item on cooldown by reading the cooldown value from the json.
     *
     * @param item ItemStack to put on cooldown
     */
    fun put(item: ItemStack?) {
        val itemId = getSkyblockItemID(item) ?: return
        val cooldown = itemCooldowns.getOrDefault(itemId, 0)
        if (cooldown > 0) {
            put(itemId, cooldown.toLong())
        }
    }

    /**
     * Put an item on cooldown by reading the cooldown value from the json.
     *
     * @param itemId ItemStack to put on cooldown
     */
    fun put(itemId: String?) {
        if (itemId == null) {
            return
        }
        val cooldown = itemCooldowns.getOrDefault(itemId, 0)
        if (cooldown > 0) {
            put(itemId, cooldown.toLong())
        }
    }


    /**
     * Put an item on cooldown with provided cooldown, for items that do not show their cooldown
     * in their lore.
     *
     * @param item Item to put on cooldown
     * @param cooldown Cooldown in milliseconds
     */
    fun put(item: ItemStack?, cooldown: Long) {
        val itemId = getSkyblockItemID(item)
        if (itemId != null && cooldown > 0) {
            put(itemId, cooldown)
        }
    }

    /**
     * Put an item on cooldown by item ID and provided cooldown.
     *
     * @param itemId Skyblock ID of the item to put on cooldown
     * @param cooldown Cooldown in milliseconds
     */
    fun put(itemId: String?, cooldown: Long) {
        require(cooldown >= 0) { "Cooldown must be positive and not 0" }

        if (!cooldowns.containsKey(itemId) || !cooldowns[itemId]!!.isOnCooldown) { // Don't allow overriding a current cooldown.
            val cooldownEntry = CooldownEntry(cooldown)
            cooldowns[itemId] = cooldownEntry
        }
    }

    /**
     * Remove the cooldown from the specified itemId
     *
     * @param itemId the item id from which to remove the cooldown
     */
    fun remove(itemId: String?) {
        cooldowns[itemId] = CooldownEntry.Companion.NULL_ENTRY
    }


    /**
     * Check if an item is on cooldown.
     *
     * @param item Item to check
     * @return Whether that item is on cooldown. `true` if it is, `false` if it's not, it's not registered,
     * is null or doesn't have a skyblock ID
     */
    fun isOnCooldown(item: ItemStack): Boolean {
        return get(item).isOnCooldown
    }

    /**
     * Check if an item is on cooldown by item ID.
     *
     * @param itemId skyblock ID of the item to check
     * @return Whether that item is on cooldown. `true` if it is, `false` if it's not or it's not registered
     */
    fun isOnCooldown(itemId: String?): Boolean {
        return get(itemId).isOnCooldown
    }

    /**
     * Get the remaining cooldown of an item in milliseconds
     *
     * @param item Item to get the cooldown of
     * @return Remaining time until its cooldown runs out or `0` if it's not on cooldown
     */
    fun getRemainingCooldown(item: ItemStack): Long {
        return get(item).remainingCooldown
    }

    /**
     * Get the remaining cooldown of an item in milliseconds by its item ID
     *
     * @param itemId Skyblock ID of the item to get the cooldown of
     * @return Remaining time until its cooldown runs out or `0` if it's not on cooldown
     */
    fun getRemainingCooldown(itemId: String?): Long {
        return get(itemId).remainingCooldown
    }

    /**
     * Get the remaining cooldown of an item in percent between `0 to 1`
     *
     * @param item Item to get the cooldown of
     * @return Remaining cooldown in percent or `0` if the item is not on cooldown
     */
    fun getRemainingCooldownPercent(item: ItemStack): Double {
        return get(item).remainingCooldownPercent
    }

    /**
     * Get the remaining cooldown of an item in percent between `0 to 1` by its ID
     *
     * @param itemId Skyblock ID of the item to get the cooldown of
     * @return Remaining cooldown in percent or `0` if the item is not on cooldown
     */
    fun getRemainingCooldownPercent(itemId: String?): Double {
        return get(itemId).remainingCooldownPercent
    }

    /**
     * Read the cooldown value of an item from it's lore.
     * This requires that the lore shows the cooldown either like `X Second Cooldown` or
     * `Cooldown: Xs`. Cooldown is returned in seconds.
     *
     * @param itemStack Item to read cooldown from
     * @return Read cooldown in seconds or `-1` if no cooldown was found
     * @see .ITEM_COOLDOWN_PATTERN
     *
     * @see .ALTERNATE_COOLDOWN_PATTERN
     */
    private fun getLoreCooldown(itemStack: ItemStack): Int {
        for (loreLine in getItemLore(itemStack)) {
            val strippedLoreLine = stripColor(loreLine)

            var matcher = ITEM_COOLDOWN_PATTERN.matcher(strippedLoreLine)
            if (matcher.matches()) {
                try {
                    return matcher.group(1).toInt()
                } catch (ignored: NumberFormatException) {
                }
            } else {
                matcher = ALTERNATE_COOLDOWN_PATTERN.matcher(strippedLoreLine)
                if (matcher.matches()) {
                    try {
                        return matcher.group(1).toInt()
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }
        }
        return -1
    }
}
