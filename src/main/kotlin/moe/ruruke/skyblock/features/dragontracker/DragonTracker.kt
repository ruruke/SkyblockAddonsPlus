package moe.ruruke.skyblock.features.dragontracker

import com.google.common.collect.Lists
import lombok.Getter
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.features.ItemDiff

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
        return 0
//        val dragonTrackerData: DragonTrackerData =
//            SkyblockAddonsPlus.instance.getPersistentValuesManager().getPersistentValues().getDragonTracker()
//        return dragonTrackerData.dragonsSince.getOrDefault(dragonsSince, 0)
    }

//    val recentDragons: List<DragonType>
//        get() = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker()
//            .getRecentDragons()

    fun dragonSpawned(dragonTypeText: String) {
        return
        if (eyesToPlace > 0) {
            contributedToCurrentDragon = true
            //TODO:
//
//            val dragonTrackerData: DragonTrackerData =
//                SkyblockAddonsPlus.instance.getPersistentValuesManager().getPersistentValues().getDragonTracker()
//            val dragonType: DragonType = DragonType.Companion.fromName(dragonTypeText)
//            if (dragonType != null) {
//                if (dragonTrackerData.recentDragons.size == 3) {
//                    dragonTrackerData.recentDragons.removeAt(0)
//                }
//                dragonTrackerData.recentDragons.add(dragonType)
//            }
//            for (dragonsSince in DragonsSince.entries) {
//                dragonTrackerData.dragonsSince[dragonsSince] =
//                    dragonTrackerData.dragonsSince.getOrDefault(dragonsSince, 0) + 1
//            }
//            if (dragonType == DragonType.SUPERIOR) {
//                dragonTrackerData.dragonsSince[DragonsSince.SUPERIOR] = 0
//            }
//
//            dragonTrackerData.eyesPlaced = dragonTrackerData.eyesPlaced + eyesToPlace
//            eyesToPlace = 0
//
//            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues()
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
        //TODO:
        return
//        recentInventoryDifferences.entries.removeIf { entry: Map.Entry<Long, List<ItemDiff>> -> System.currentTimeMillis() - entry.key > 1000 }
//        recentInventoryDifferences[System.currentTimeMillis()] = newInventoryDifference
//
//        // They haven't killed a dragon recently OR the last killed dragon was over 60 seconds ago...
//        if (lastDragonKilled == -1L || System.currentTimeMillis() - lastDragonKilled > 60 * 1000) {
//            return
//        }
//
//
//        for (inventoryDifference in recentInventoryDifferences.values) {
//            for (itemDifference in inventoryDifference) {
//                if (itemDifference.getAmount() < 1) {
//                    continue
//                }
//
//                val dragonTrackerData: DragonTrackerData =
//                    SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker()
//                val skyBlockItemID: String = ItemUtils.getSkyblockItemID(itemDifference.getExtraAttributes())
//                when (skyBlockItemID) {
//                    "ASPECT_OF_THE_DRAGON" -> {
//                        dragonTrackerData.dragonsSince[DragonsSince.ASPECT_OF_THE_DRAGONS] = 0
//                        SkyblockAddons.getInstance().getPersistentValuesManager().saveValues()
//                    }
//
//                    "PET" -> {
//                        val petInfo: PetInfo = ItemUtils.getPetInfo(itemDifference.getExtraAttributes())
//                        if (petInfo != null && "ENDER_DRAGON" == petInfo.getType()) {
//                            dragonTrackerData.dragonsSince[DragonsSince.ENDER_DRAGON_PET] = 0
//                            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues()
//                        }
//                    }
//
//                    else -> {}
//                }
//            }
//        }
//
//        recentInventoryDifferences.clear()
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
        @Getter
        private val dummyDragons: List<DragonType> =
            Lists.newArrayList(DragonType.PROTECTOR, DragonType.SUPERIOR, DragonType.WISE)

        @Getter
        private val instance = DragonTracker()
    }
}
