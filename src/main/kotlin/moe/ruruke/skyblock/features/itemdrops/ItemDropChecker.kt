package moe.ruruke.skyblock.features.itemdrops

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.ItemRarity
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.ItemUtils
import net.minecraft.client.Minecraft
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
 * This class handles the item checking for the Stop Dropping/Selling Rare Items feature.
 * When the player tries to drop or sell an item, [this.canDropItem] is called to check
 * the item against the rarity requirements, the blacklist, and the whitelist.
 * These requirements determine if the item is allowed to be dropped/sold.
 *
 * @author ILikePlayingGames
 * @version 0.1
 * @see ItemDropList
 */
class ItemDropChecker {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    // Variables used for checking drop confirmations
    private var itemOfLastDropAttempt: ItemStack? = null
    private var timeOfLastDropAttempt: Long = 0
    private var attemptsRequiredToConfirm = 0

    /**
     * Checks if the item in this slot can be dropped or sold. The alert sound will be played if a drop attempt is denied.
     *
     * @param slot the inventory slot to check
     * @return `true` if this item can be dropped or sold, `false` otherwise
     */
    fun canDropItem(slot: Slot?): Boolean {
        return if (slot != null && slot.hasStack) {
            canDropItem(slot.stack)
        } else {
            true
        }
    }

    /**
     * Checks if this item can be dropped or sold.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @param playAlert plays an alert sound if `true` and a drop attempt is denied, otherwise the sound doesn't play
     * @return `true` if this item can be dropped or sold, `false` otherwise
     */
    /**
     * Checks if this item can be dropped or sold. The alert sound will be played if a drop attempt is denied.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @return `true` if this item can be dropped or sold, `false` otherwise
     */
    /**
     * Checks if this item can be dropped or sold.
     * This method is for items in the inventory, not those in the hotbar.
     * The alert sound will be played if a drop attempt is denied.
     *
     * @param item the item to check
     * @return `true` if this item can be dropped or sold, `false` otherwise
     */
    @JvmOverloads
    fun canDropItem(item: ItemStack, itemIsInHotbar: Boolean = false, playAlert: Boolean = true): Boolean {
        if (SkyblockAddonsPlus.utils!!.isOnSkyblock()) {
            val itemID: String? = ItemUtils.getSkyblockItemID(item)
            val itemRarity: ItemRarity? = ItemUtils.getRarity(item)

            if (itemID == null) {
                // Allow dropping of Skyblock items without IDs
                return true
            } else if (itemRarity == null) {
                /*
             If this Skyblock item has an ID but no rarity, allow dropping it.
             This really shouldn't happen but just in case it does, this condition is here.
             */
                return true
            }

            val blacklist: List<String> = SkyblockAddonsPlus.getOnlineData()!!.getDropSettings()!!.getDontDropTheseItems()!!
            val whitelist: List<String> = SkyblockAddonsPlus.getOnlineData()!!.getDropSettings()!!.getAllowDroppingTheseItems()!!

            if (itemIsInHotbar) {
                if (itemRarity.compareTo(
                        SkyblockAddonsPlus.getOnlineData()!!.getDropSettings()!!.getMinimumHotbarRarity()!!
                    ) < 0 && !blacklist.contains(itemID)
                ) {
                    return true
                } else {
                    // Dropping rare non-whitelisted items from the hotbar is not allowed.
                    if (whitelist.contains(itemID)) {
                        return true
                    } else {
                        if (playAlert) {
                            playAlert()
                        }
                        return false
                    }
                }
            } else {
                return if (itemRarity.compareTo(
                        SkyblockAddonsPlus.getOnlineData()!!.getDropSettings()!!.getMinimumInventoryRarity()!!
                    ) < 0 && !blacklist.contains(itemID)
                ) {
                    true
                } else {
                    /*
                                 If the item is above the minimum rarity and not whitelisted, require the player to attempt
                                 to drop it three times to confirm they want to drop it.
                                */
                    if (whitelist.contains(itemID)) {
                        true
                    } else {
                        dropConfirmed(item, 3, playAlert)
                    }
                }
            }
        } else if (SkyblockAddonsPlus.config!!.isEnabled(Feature.DROP_CONFIRMATION) && SkyblockAddonsPlus.configValues!!.isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES)
        ) {
            return dropConfirmed(item, 2, playAlert)
        } else {
            return true
        }
    }

    /**
     * Checks if the player has confirmed that they want to drop the given item stack.
     * The player confirms that they want to drop the item when they try to drop it the number of
     * times specified in `numberOfActions`.
     *
     * @param item the item stack the player is attempting to drop
     * @param numberOfActions the number of times the player has to drop the item to confirm
     * @param playAlert plays an alert sound if `true` and a drop attempt is denied, otherwise the sound doesn't play
     * @return `true` if the player has dropped the item enough
     */
    fun dropConfirmed(item: ItemStack, numberOfActions: Int, playAlert: Boolean): Boolean {
        if (item == null) {
            throw NullPointerException("Item cannot be null!")
        } else require(numberOfActions >= 2) { "At least two attempts are required." }

        // If there's no drop confirmation active, set up a new one.
        if (itemOfLastDropAttempt == null) {
            itemOfLastDropAttempt = item
            timeOfLastDropAttempt = Minecraft.getSystemTime()
            attemptsRequiredToConfirm = numberOfActions - 1
            onDropConfirmationFail()
            return false
        } else {
            // Reset the current drop confirmation on time out or if the item being dropped changes.
            if (Minecraft.getSystemTime() - timeOfLastDropAttempt > DROP_CONFIRMATION_TIMEOUT || !ItemStack.areItemStacksEqual(
                    item,
                    itemOfLastDropAttempt
                )
            ) {
                resetDropConfirmation()
                return dropConfirmed(item, numberOfActions, playAlert)
            } else {
                if (attemptsRequiredToConfirm >= 1) {
                    onDropConfirmationFail()
                    return false
                } else {
                    resetDropConfirmation()
                    return true
                }
            }
        }
    }

    /**
     * Called whenever a drop confirmation fails due to the player not attempting to drop the item enough times.
     * A message is sent and a sound is played notifying the player how many more times they need to drop the item.
     */
    fun onDropConfirmationFail() {
        val colorCode: ColorCode = SkyblockAddonsPlus.configValues!!.getRestrictedColor(Feature.DROP_CONFIRMATION)!!

        if (attemptsRequiredToConfirm >= 2) {
            val multipleAttemptsRequiredMessage: String =
                Translations.getMessage("messages.clickMoreTimes", attemptsRequiredToConfirm.toString())!!
            SkyblockAddonsPlus.utils!!.sendMessage(colorCode.toString() + multipleAttemptsRequiredMessage)
        } else {
            val oneMoreAttemptRequiredMessage: String = Translations.getMessage("messages.clickOneMoreTime")!!
            SkyblockAddonsPlus.utils!!.sendMessage(colorCode.toString() + oneMoreAttemptRequiredMessage)
        }
        playAlert()
        attemptsRequiredToConfirm--
    }

    /**
     * Plays an alert sound when a drop attempt is denied.
     */
    fun playAlert() {
        SkyblockAddonsPlus.utils!!.playLoudSound("note.bass", 0.5)
    }

    /**
     * Reset the drop confirmation settings.
     */
    fun resetDropConfirmation() {
        itemOfLastDropAttempt = null
        timeOfLastDropAttempt = 0L
        attemptsRequiredToConfirm = 0
    }

    companion object {
        private const val DROP_CONFIRMATION_TIMEOUT = 3000L
    }
}
