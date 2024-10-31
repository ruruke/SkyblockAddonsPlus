package moe.ruruke.skyblock.features.slayertracker


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.config.NewConfig
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations
import moe.ruruke.skyblock.features.ItemDiff
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.skyblockdata.Rune
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import java.util.*

class SlayerTracker {
    // Saves the last second of inventory differences
    @Transient
    private val recentInventoryDifferences: MutableMap<Long, List<ItemDiff>> = HashMap<Long, List<ItemDiff>>()

    @Transient
    private var lastSlayerCompleted: Long = -1

    fun getSlayerKills(slayerBoss: SlayerBoss?): Int {
        val slayerTrackerData: SlayerTrackerData =
            main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
        return slayerTrackerData.getSlayerKills().getOrDefault(slayerBoss, 0)
    }

    fun getDropCount(slayerDrop: SlayerDrop?): Int {
        val slayerTrackerData: SlayerTrackerData =
            main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
        return slayerTrackerData.getSlayerDropCounts().getOrDefault(slayerDrop, 0)
    }
    /**
     * Returns whether any slayer trackers are enabled
     *
     * @return {@code true} if at least one slayer tracker is enabled, {@code false} otherwise
     */
    fun isTrackerEnabled(): Boolean {
        return NewConfig.isEnabled(Feature.REVENANT_SLAYER_TRACKER) ||
                NewConfig.isEnabled(Feature.TARANTULA_SLAYER_TRACKER) ||
                NewConfig.isEnabled(Feature.SVEN_SLAYER_TRACKER) ||
                NewConfig.isEnabled(Feature.VOIDGLOOM_SLAYER_TRACKER)
    }
    /**
     * Adds a kill to the slayer type
     */
    fun completedSlayer(slayerTypeText: String?) {
        val slayerBoss: SlayerBoss = SlayerBoss.Companion.getFromMobType(slayerTypeText)!!
        if (slayerBoss != null) {
            val slayerTrackerData: SlayerTrackerData =
                main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
            slayerTrackerData.getSlayerKills().put(slayerBoss, slayerTrackerData.getSlayerKills().getOrDefault(slayerBoss, 0) + 1)
            slayerTrackerData.setLastKilledBoss(slayerBoss)
            lastSlayerCompleted = System.currentTimeMillis()

            main.persistentValuesManager!!.saveValues()
        }
    }

    fun checkInventoryDifferenceForDrops(newInventoryDifference: List<ItemDiff>) {
        recentInventoryDifferences.entries.removeIf { entry: Map.Entry<Long, List<ItemDiff>> -> System.currentTimeMillis() - entry.key > 1000 }
        recentInventoryDifferences[System.currentTimeMillis()] = newInventoryDifference

        val slayerTrackerData: SlayerTrackerData =
            main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
        // They haven't killed a dragon recently OR the last killed dragon was over 30 seconds ago...
        if (slayerTrackerData.getLastKilledBoss() == null || lastSlayerCompleted == -1L || System.currentTimeMillis() - lastSlayerCompleted > 30 * 1000) {
            return
        }


        for (inventoryDifference in recentInventoryDifferences.values) {
            for (itemDifference in inventoryDifference) {
                if (itemDifference.getAmount() < 1) {
                    continue
                }

                for (drop in slayerTrackerData.getLastKilledBoss()!!.getDrop()!!) {
                    if (drop!!.getSkyblockId() == ItemUtils.getSkyblockItemID(itemDifference.getExtraAttributes())) {
                        // If this is a rune and it doesn't match, continue

                        val rune: Rune = ItemUtils.getRuneData(itemDifference.getExtraAttributes())!!
                        if (drop.getRuneID() != null && (rune == null || rune.getType() == null || !rune.getType()
                                .equals(drop.getRuneID()))
                        ) {
                            continue
                        }
                        // If this is a book and it doesn't match, continue
                        if (drop.getSkyblockId() == "ENCHANTED_BOOK") {
                            var match = true
                            val diffTag: NBTTagCompound =
                                itemDifference.getExtraAttributes()!!.getCompoundTag("enchantments")
                            val dropTag: NBTTagCompound = ItemUtils.getEnchantments(drop.getItemStack())!!
                            if (diffTag != null && dropTag != null && diffTag.getKeySet().size == dropTag.getKeySet().size) {
                                for (key in diffTag.getKeySet()) {
                                    if (!dropTag.hasKey(
                                            key,
                                            Constants.NBT.TAG_INT
                                        ) || dropTag.getInteger(key) != diffTag.getInteger(key)
                                    ) {
                                        match = false
                                        break
                                    }
                                }
                            } else {
                                match = false
                            }
                            if (!match) {
                                continue
                            }
                        }
                        slayerTrackerData.getSlayerDropCounts()[drop] =
                            slayerTrackerData.getSlayerDropCounts().getOrDefault(drop, 0) + itemDifference.getAmount()
                    }
                }
            }
        }

        recentInventoryDifferences.clear()
    }

    /**
     * Sets the value of a specific slayer stat
     *
     *
     * This method is called from [moe.ruruke.skyblock.commands.SkyblockAddonsCommand.processCommand]
     * when the player runs the command to change the slayer tracker stats.
     *
     * @param args the arguments provided when the player executed the command
     */
    fun setStatManually(args: Array<String>) {
        val slayerBoss: SlayerBoss = SlayerBoss.Companion.getFromMobType(args[1])
            ?: throw IllegalArgumentException(
                Translations.getMessage(
                    "commandUsage.sba.slayer.invalidBoss",
                    args[1]
                )
            )

        val slayerTrackerData: SlayerTrackerData =
            main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
        if (args[2].equals("kills", ignoreCase = true)) {
            val count = args[3].toInt()
            slayerTrackerData.getSlayerKills()[slayerBoss] = count
            main.utils!!.sendMessage(
                Translations.getMessage(
                    "commandUsage.sba.slayer.killsSet", args[1], args[3]
                ).toString()
            )
            main.persistentValuesManager!!.saveValues()
            return
        }
        var slayerDrop = try {
            SlayerDrop.valueOf(args[2].uppercase(Locale.getDefault()))
        } catch (ex: IllegalArgumentException) {
            null
        }

        if (slayerDrop != null) {
            val count = args[3].toInt()
            slayerTrackerData.getSlayerDropCounts()[slayerDrop] = count
            main.utils!!.sendMessage(
                Translations.getMessage(
                    "commandUsage.sba.slayer.statSet", args[2], args[1], args[3]
                ).toString()
            )
            main.persistentValuesManager!!.saveValues()
            return
        }

        throw IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidStat", args[1]))
    }

    fun setKillCount(slayerBoss: SlayerBoss?, kills: Int) {
        val slayerTrackerData: SlayerTrackerData =
            main.persistentValuesManager!!.getPersistentValues().getSlayerTracker()
        slayerTrackerData.getSlayerKills()[slayerBoss!!] = kills
    }

    companion object {
        private val instance = SlayerTracker()
        private val main: SkyblockAddonsPlus.Companion= SkyblockAddonsPlus.instance
        fun getInstance(): SlayerTracker {
            return instance
        }
    }
}
