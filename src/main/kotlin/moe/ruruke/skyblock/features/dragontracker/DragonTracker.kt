package moe.ruruke.skyblock.features.dragontracker

import com.google.common.collect.Lists

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.features.ItemDiff
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.skyblockdata.PetInfo

class DragonTracker {
    @Transient
    private var contributedToCurrentDragon = false

    @Transient
    private var lastDragonKilled: Long = -1

    @Transient
    private var eyesToPlace = 0

    // Saves the last second of inventory differences
    @Transient
    private val recentInventoryDifferences: MutableMap<Long, List<ItemDiff>> = HashMap()

    fun getDragsSince(dragonsSince: DragonsSince?): Int {
        //TODO:
        val dragonTrackerData: DragonTrackerData =
            SkyblockAddonsPlus.instance.persistentValuesManager!!.getPersistentValues().getDragonTracker()
        return dragonTrackerData.getDragonsSince().getOrDefault(dragonsSince, 0)
    }


    fun getRecentDragons(): List<DragonType> {
        return SkyblockAddonsPlus.instance.persistentValuesManager!!.getPersistentValues().getDragonTracker()
            .getRecentDragons()
    }
    fun dragonSpawned(dragonTypeText: String) {
        if (eyesToPlace > 0) {
            val dragonTrackerData: DragonTrackerData =
                SkyblockAddonsPlus.instance.persistentValuesManager!!.getPersistentValues().getDragonTracker()
            val dragonType: DragonType = DragonType.Companion.fromName(dragonTypeText)!!
            if (dragonType != null) {
                if (dragonTrackerData.getRecentDragons().size == 3) {
                    dragonTrackerData.getRecentDragons().removeAt(0)
                }
                dragonTrackerData.getRecentDragons().add(dragonType)
            }
            for (dragonsSince in DragonsSince.entries) {
                dragonTrackerData.getDragonsSince()[dragonsSince] =
                    dragonTrackerData.getDragonsSince().getOrDefault(dragonsSince, 0) + 1
            }
            if (dragonType == DragonType.SUPERIOR) {
                dragonTrackerData.getDragonsSince()[DragonsSince.SUPERIOR] = 0
            }

            dragonTrackerData.setEyesPlaced( dragonTrackerData.getEyesPlaced() + eyesToPlace)
            eyesToPlace = 0

            SkyblockAddonsPlus.persistentValuesManager!!.saveValues()
        }
    }

    fun dragonKilled() {
        if (!contributedToCurrentDragon) {
            return
        }

        lastDragonKilled = System.currentTimeMillis()
        contributedToCurrentDragon = false
    }

    fun checkInventoryDifferenceForDrops(newInventoryDifference: List<ItemDiff>) {
        recentInventoryDifferences.entries.removeIf { entry: Map.Entry<Long, List<ItemDiff>> -> System.currentTimeMillis() - entry.key > 1000 }
        recentInventoryDifferences[System.currentTimeMillis()] = newInventoryDifference

        // They haven't killed a dragon recently OR the last killed dragon was over 60 seconds ago...
        if (lastDragonKilled == -1L || System.currentTimeMillis() - lastDragonKilled > 60 * 1000) {
            return
        }


        for (inventoryDifference in recentInventoryDifferences.values) {
            for (itemDifference in inventoryDifference) {
                if (itemDifference.getAmount() < 1) {
                    continue
                }

                val dragonTrackerData: DragonTrackerData =
                    SkyblockAddonsPlus.persistentValuesManager!!.getPersistentValues().getDragonTracker()
                val skyBlockItemID: String = ItemUtils.getSkyblockItemID(itemDifference.getExtraAttributes())!!
                when (skyBlockItemID) {
                    "ASPECT_OF_THE_DRAGON" -> {
                        dragonTrackerData.getDragonsSince()[DragonsSince.ASPECT_OF_THE_DRAGONS] = 0
                        SkyblockAddonsPlus.persistentValuesManager!!.saveValues()
                    }

                    "PET" -> {
                        val petInfo: PetInfo = ItemUtils.getPetInfo(itemDifference.getExtraAttributes())!!
                        if (petInfo != null && "ENDER_DRAGON" == petInfo.getType()) {
                            dragonTrackerData.getDragonsSince()[DragonsSince.ENDER_DRAGON_PET] = 0
                            SkyblockAddonsPlus.persistentValuesManager!!.saveValues()
                        }
                    }

                    else -> {}
                }
            }
        }

        recentInventoryDifferences.clear()
    }

    fun reset() {
        eyesToPlace = 0
        contributedToCurrentDragon = false
        lastDragonKilled = -1
    }

    fun addEye() {
        eyesToPlace++
    }

    fun removeEye() {
        eyesToPlace--
    }

    companion object {
        
        private val dummyDragons: List<DragonType> =
            Lists.newArrayList(DragonType.PROTECTOR, DragonType.SUPERIOR, DragonType.WISE)

        
        private val instance = DragonTracker()
    }
}
