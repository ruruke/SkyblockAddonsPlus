package moe.ruruke.skyblock.utils

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap


import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.instance
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.scheduler
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.utils
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.InventoryType
import moe.ruruke.skyblock.features.ItemDiff
import moe.ruruke.skyblock.features.SlayerArmorProgress
import moe.ruruke.skyblock.misc.scheduler.Scheduler
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.crash.CrashReport
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ReportedException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern


//TODO Fix for Hypixel localization
/**
 * Utility methods related to player inventories
 */
class InventoryUtils {
    private var previousInventory: List<ItemStack?>? = null
    private val itemPickupLog: Multimap<String, ItemDiff> = ArrayListMultimap.create()

    private var inventoryWarningShown = false
    fun setInventoryWarningShown(value: Boolean){
        inventoryWarningShown = value
    }
    fun getInventoryType(): InventoryType? {
        return inventoryType
    }
    /**
     * Whether the player is wearing a Skeleton Helmet.
     */
    
    private var wearingSkeletonHelmet = false
    fun isWearingSkeletonHelmet(): Boolean {
        return wearingSkeletonHelmet
    }

    
    private var usingToxicArrowPoison = false
    fun isUsingToxicArrowPoison(): Boolean {
        return usingToxicArrowPoison
    }

    
    private val slayerArmorProgresses = arrayOfNulls<SlayerArmorProgress>(4)

    
    private var inventoryType: InventoryType? = null

    
    private var inventoryKey: String? = null

    
    private var inventoryPageNum = 0

    
    private var inventorySubtype: String? = null
    fun getInventorySubtype(): String? {
        return inventorySubtype
    }
    private val main = instance


    /**
     * Copies an inventory into a List of copied ItemStacks
     *
     * @param inventory Inventory to copy
     * @return List of copied ItemStacks
     */
    private fun copyInventory(inventory: Array<ItemStack>): List<ItemStack?> {
        val copy: MutableList<ItemStack?> = ArrayList(inventory.size)
        for (item in inventory) {
            if (item != null) {
                copy.add(ItemStack.copyItemStack(item))
            } else {
                copy.add(null)
            }
        }
        return copy
    }

    /**
     * Compares previously recorded Inventory state with current Inventory state to determine changes and
     * stores them in [.itemPickupLog]
     *
     * @param currentInventory Current Inventory state
     */
    fun getInventoryDifference(currentInventory: Array<ItemStack>) {
        val newInventory = copyInventory(currentInventory)
        val previousInventoryMap: MutableMap<String, Pair<Int, NBTTagCompound?>?> = HashMap()
        val newInventoryMap: MutableMap<String, Pair<Int, NBTTagCompound?>?> = HashMap()

        if (previousInventory != null) {
            for (i in newInventory.indices) {
                if (i == SKYBLOCK_MENU_SLOT) { // Skip the SkyBlock Menu slot altogether (which includes the Quiver Arrow now)
                    continue
                }

                var previousItem: ItemStack? = null
                var newItem: ItemStack? = null

                try {
                    previousItem = previousInventory!![i]
                    newItem = newInventory[i]

                    if (previousItem != null) {
                        var amount = if (previousInventoryMap.containsKey(previousItem.displayName)) {
                            previousInventoryMap[previousItem.displayName]!!.key + previousItem.stackSize
                        } else {
                            previousItem.stackSize
                        }
                        var extraAttributes = ItemUtils.getExtraAttributes(previousItem)
                        if (extraAttributes != null) {
                            extraAttributes = extraAttributes.copy() as NBTTagCompound
                        }
                        previousInventoryMap[previousItem.displayName] =
                            Pair(amount, extraAttributes)
                    }

                    if (newItem != null) {
                        if (newItem.displayName.contains(" " + ColorCode.DARK_GRAY + "x")) {
                            val newName = newItem.displayName.substring(0, newItem.displayName.lastIndexOf(" "))
                            newItem.setStackDisplayName(newName) // This is a workaround for merchants, it adds x64 or whatever to the end of the name.
                        }
                        var amount = if (newInventoryMap.containsKey(newItem.displayName)) {
                            newInventoryMap[newItem.displayName]!!.key + newItem.stackSize
                        } else {
                            newItem.stackSize
                        }
                        var extraAttributes = ItemUtils.getExtraAttributes(newItem)
                        if (extraAttributes != null) {
                            extraAttributes = extraAttributes.copy() as NBTTagCompound
                        }
                        newInventoryMap[newItem.displayName] =
                            Pair(amount, extraAttributes)
                    }
                } catch (exception: RuntimeException) {
                    val crashReport =
                        CrashReport.makeCrashReport(exception, "Comparing current inventory to previous inventory")
                    val inventoryDetails = crashReport.makeCategory("Inventory Details")
                    inventoryDetails.addCrashSection("Previous", "Size: " + previousInventory!!.size)
                    inventoryDetails.addCrashSection("New", "Size: " + newInventory.size)
                    val itemDetails = crashReport.makeCategory("Item Details")
                    itemDetails.addCrashSection(
                        "Previous Item", ("""
     Item: ${previousItem?.toString() ?: "null"}
     Display Name: ${if (previousItem != null) previousItem.displayName else "null"}
     Index: $i
     Map Value: ${if (previousItem != null) (if (previousInventoryMap[previousItem.displayName] != null) previousInventoryMap[previousItem.displayName].toString() else "null") else "null"}
     """.trimIndent())
                    )
                    itemDetails.addCrashSection(
                        "New Item", ("""
     Item: ${newItem?.toString() ?: "null"}
     Display Name: ${if (newItem != null) newItem.displayName else "null"}
     Index: $i
     Map Value: ${if (newItem != null) (if (previousInventoryMap[newItem.displayName] != null) previousInventoryMap[newItem.displayName].toString() else "null") else "null"}
     """.trimIndent())
                    )
                    throw ReportedException(crashReport)
                }
            }

            val inventoryDifference: MutableList<ItemDiff> = LinkedList()
            val keySet: MutableSet<String> = HashSet(previousInventoryMap.keys)
            keySet.addAll(newInventoryMap.keys)

            keySet.forEach(Consumer { key: String ->
                var previousAmount = 0
                if (previousInventoryMap.containsKey(key)) {
                    previousAmount = previousInventoryMap[key]!!.key
                }

                var newAmount = 0
                if (newInventoryMap.containsKey(key)) {
                    newAmount = newInventoryMap[key]!!.key
                }

                val diff = newAmount - previousAmount
                if (diff != 0) { // Get the NBT tag from whichever map the name exists in
                    inventoryDifference.add(
                        ItemDiff(
                            key, diff, newInventoryMap.getOrDefault(
                                key,
                                previousInventoryMap[key]
                            )!!.value
                        )
                    )
                }
            })
            //TODO:
//            if (configValues!!.isEnabled(Feature.DRAGON_STATS_TRACKER)) {
//
////                DragonTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference)
//            }
            //TODO:
//            if (SlayerTracker.getInstance().isTrackerEnabled()) {
//                SlayerTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference)
//            }

            // Add changes to already logged changes of the same item, so it will increase/decrease the amount
            // instead of displaying the same item twice
            if (configValues!!.isEnabled(Feature.ITEM_PICKUP_LOG)) {
                for (diff in inventoryDifference) {
                    val itemDiffs = itemPickupLog[diff.getDisplayName()]
                    if (itemDiffs.size <= 0) {
                        itemPickupLog.put(diff.getDisplayName(), diff)
                    } else {
                        var added = false
                        for (loopDiff in itemDiffs) {
                            if ((diff.getAmount() < 0 && loopDiff.getAmount() < 0) || (diff.getAmount() > 0 && loopDiff.getAmount() > 0)) {
                                loopDiff.add(diff.getAmount())
                                added = true
                            }
                        }
                        if (!added) {
                            itemPickupLog.put(diff.getDisplayName(), diff)
                        }
                    }
                }
            }
        }

        previousInventory = newInventory
    }

    /**
     * Resets the previously stored Inventory state
     */
    fun resetPreviousInventory() {
        previousInventory = null
    }

    /**
     * Removes items in the pickup log that have been there for longer than [ItemDiff.LIFESPAN]
     */
    fun cleanUpPickupLog() {
        itemPickupLog.entries()
            .removeIf { entry: Map.Entry<String, ItemDiff> -> entry.value.lifetime > ItemDiff.LIFESPAN }
    }

    /**
     * Checks if the players inventory is full and displays an alarm if so.
     *
     * @param mc Minecraft instance
     * @param p Player to check
     */
    fun checkIfInventoryIsFull(mc: Minecraft, p: EntityPlayerSP) {
        if (utils!!.isOnSkyblock() && NewConfig.isEnabled(Feature.FULL_INVENTORY_WARNING)) {
            /*
            If the inventory is full, show the full inventory warning.
            Slot 8 is the Skyblock menu/quiver arrow slot. It's ignored so shooting with a full inventory
            doesn't spam the full inventory warning.
             */
            for (i in p.inventory.mainInventory.indices) {
                // If we find an empty slot that isn't slot 8, remove any queued warnings and stop checking.
                if (p.inventory.mainInventory[i] == null && i != 8) {
                    if (inventoryWarningShown) {
                        scheduler!!.removeQueuedFullInventoryWarnings()
                    }
                    inventoryWarningShown = false
                    return
                }
            }

            // If we make it here, the inventory is full. Show the warning.
            if (mc.currentScreen == null && main.getPlayerListener()
                    .didntRecentlyJoinWorld() && !inventoryWarningShown
            ) {
                showFullInventoryWarning()
                scheduler!!.schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, configValues!!.getWarningSeconds())

                // Schedule a repeat if needed.
                if (configValues!!.isEnabled(Feature.REPEAT_FULL_INVENTORY_WARNING)) {
                    scheduler!!.schedule(Scheduler.CommandType.SHOW_FULL_INVENTORY_WARNING, 10)
                    scheduler!!.schedule(
                        Scheduler.CommandType.RESET_TITLE_FEATURE,
                        10 + configValues!!.getWarningSeconds()
                    )
                }

                inventoryWarningShown = true
            }
        }
    }

    /**
     * Shows the full inventory warning.
     */
    fun showFullInventoryWarning() {
        //TODO:
        utils!!.playLoudSound("random.orb", 0.5)
        main.renderListener!!.setTitleFeature(Feature.FULL_INVENTORY_WARNING)
    }

    /**
     * Checks if the player is wearing a Skeleton Helmet and updates [.wearingSkeletonHelmet] accordingly
     *
     * @param p Player to check
     */
    fun checkIfWearingSkeletonHelmet(p: EntityPlayerSP) {
        if (configValues!!.isEnabled(Feature.SKELETON_BAR)) {
            val item = p.getEquipmentInSlot(4)
            if (item != null && SKELETON_HELMET_ID == ItemUtils.getSkyblockItemID(item)) {
                wearingSkeletonHelmet = true
                return
            }
            wearingSkeletonHelmet = false
        }
    }

    /**
     * Determines if the player is using Toxic Arrow Poison by detecting if it is present in their inventory.
     *
     * @param p the player to check
     */
    fun checkIfUsingToxicArrowPoison(p: EntityPlayerSP) {
        if (configValues!!.isEnabled(Feature.TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON)) {
            for (item in p.inventory.mainInventory) {
                if (item != null && TOXIC_ARROW_POISON_ID == ItemUtils.getSkyblockItemID(item)) {
                    this.usingToxicArrowPoison = true
                    return
                }
            }
            this.usingToxicArrowPoison = false
        }
    }

    /**
     * The difference between a slot number in any given [Container] and what that number would be in a [ContainerPlayer].
     */
    fun getSlotDifference(container: Container?): Int {
        return if (container is ContainerChest) 9 - container.lowerChestInventory.sizeInventory
        else if (container is ContainerHopper) 4
        else if (container is ContainerFurnace) 6
        else if (container is ContainerBeacon) 8
        else 0
    }

    /**
     * Checks if the player is wearing any Revenant or Tarantula armor.
     * If the armor is detected, the armor's levelling progress is retrieved to be displayed on the HUD.
     *
     * @param p the player to check
     */
    fun checkIfWearingSlayerArmor(p: EntityPlayerSP) {
        if (configValues!!.isEnabled(Feature.SLAYER_INDICATOR)) {
            for (i in 3 downTo 0) {
                val itemStack = p.inventory.armorInventory[i]
                val itemID = if (itemStack != null) ItemUtils.getSkyblockItemID(itemStack) else null

                if (itemID != null && (itemID.startsWith("REVENANT") || itemID.startsWith("TARANTULA") ||
                            itemID.startsWith("FINAL_DESTINATION") || itemID.startsWith("REAPER"))
                ) {
                    var percent: String? = null
                    var defence: String? = null
                    val lore = ItemUtils.getItemLore(itemStack)
                    for (loreLine in lore) {
                        val matcher = REVENANT_UPGRADE_PATTERN.matcher(
                            TextUtils.stripColor(
                                loreLine
                            )
                        )
                        if (matcher.matches()) { // Example: line§5§o§7Next Upgrade: §a+240❈ §8(§a14,418§7/§c15,000§8)
                            try {
                                val percentage =
                                    matcher.group(2).replace(",", "").toFloat() / matcher.group(3).replace(",", "")
                                        .toInt() * 100
                                val bigDecimal = BigDecimal(percentage.toDouble()).setScale(0, RoundingMode.HALF_UP)
                                percent = bigDecimal.toString()
                                defence = ColorCode.GREEN.toString() + matcher.group(1)
                                break
                            } catch (ignored: NumberFormatException) {
                            }
                        }
                    }
                    if (percent != null && defence != null) {
                        val currentProgress = slayerArmorProgresses[i]

                        if (currentProgress == null || itemStack != currentProgress.getItemStack()) {
                            // The item has changed or didn't exist. Create new object.
                            slayerArmorProgresses[i] = SlayerArmorProgress(itemStack!!, percent, defence)
                        } else {
                            // The item has remained the same. Just update the stats.
                            currentProgress.setPercent(percent)
                            currentProgress.setDefence(defence)
                        }
                    }
                } else {
                    slayerArmorProgresses[i] = null
                }
            }
        }
    }

    /**
     * @return Log of recent Inventory changes
     */
    fun getItemPickupLog(): Collection<ItemDiff> {
        return itemPickupLog.values()
    }
    private fun getGui(lowerChestInventory: IInventory) {
        val guiName = lowerChestInventory.displayName.unformattedText
        println(guiName)
    }

    /**
     * Detects, stores, and returns the Skyblock inventory type of the given `GuiChest`. The inventory type is the
     * kind of menu the player has open, like a crafting table, or an enchanting table for example. If no known inventory
     * type is detected, `null` will be stored.
     *
     * @return an [InventoryType] enum constant representing the current Skyblock inventory type
     */
    fun updateInventoryType(guiChest: GuiChest): InventoryType? {
        // Get the open chest and test if it's the same one that we've seen before
        val inventory = guiChest.lowerChestInventory as IInventory
        if (inventory.displayName == null) {
            return null.also { inventoryType = it }
        }else{
            main.utils!!.sendMessage("Name > "+ inventory.name)
        }
        val chestName = TextUtils.stripColor(inventory.displayName.unformattedText)

        // Initialize inventory to null and get the open chest name
        inventoryType = null

        // Find an inventory match if possible
        for (inventoryTypeItr in InventoryType.entries) {
            val m: Matcher = inventoryTypeItr.getInventoryPattern().matcher(chestName)
            if (m.matches()) {
                if (m.groupCount() > 0) {
                    inventoryPageNum = try {
                        m.group("page").toInt()
                    } catch (e: Exception) {
                        0
                    }
                    inventorySubtype = try {
                        m.group("type")
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    inventoryPageNum = 0
                    inventorySubtype = null
                }
                inventoryType = inventoryTypeItr
                break
            }
        }
        inventoryKey = getInventoryKey(inventoryType, inventoryPageNum)
        return inventoryType
    }

    fun getInventoryKey(inventoryType: InventoryType?, inventoryPageNum: Int): String? {
        if (inventoryType == null) {
            return null
        }
        return inventoryType.getInventoryName() + inventoryPageNum
    }
    fun getInventoryKey(): String? {
        return inventoryKey
    }

    companion object {
        /** Slot index the SkyBlock menu is at.  */
        private const val SKYBLOCK_MENU_SLOT = 8

        /** Display name of the Skeleton Helmet.  */
        private const val SKELETON_HELMET_ID = "SKELETON_HELMET"
        private const val TOXIC_ARROW_POISON_ID = "TOXIC_ARROW_POISON"

        const val MADDOX_BATPHONE_ID: String = "AATROX_BATPHONE"
        const val JUNGLE_AXE_ID: String = "JUNGLE_AXE"
        const val TREECAPITATOR_ID: String = "TREECAPITATOR_AXE"
        const val CHICKEN_HEAD_ID: String = "CHICKEN_HEAD"
        val BAT_PERSON_SET_IDS: HashSet<String> = HashSet(
            mutableListOf(
                "BAT_PERSON_BOOTS",
                "BAT_PERSON_LEGGINGS",
                "BAT_PERSON_CHESTPLATE",
                "BAT_PERSON_HELMET"
            )
        )
        const val GRAPPLING_HOOK_ID: String = "GRAPPLING_HOOK"

        private val REVENANT_UPGRADE_PATTERN: Pattern =
            Pattern.compile("Next Upgrade: \\+([0-9]+❈) \\(([0-9,]+)/([0-9,]+)\\)")

        /**
         * Returns true if the player is wearing a full armor set with IDs contained in the given set
         *
         * @param player the player
         * @param armorSetIds the given set of armor IDs
         * @return `true` iff all player armor contained in given set, `false` otherwise.
         */
        fun isWearingFullSet(player: EntityPlayer, armorSetIds: Set<String?>): Boolean {
            var flag = true
            val armorInventory = player.inventory.armorInventory
            for (i in 0..3) {
                val itemID = ItemUtils.getSkyblockItemID(armorInventory[i])
                if (itemID == null || !armorSetIds.contains(itemID)) {
                    flag = false
                    break
                }
            }
            return flag
        }
    }
}
