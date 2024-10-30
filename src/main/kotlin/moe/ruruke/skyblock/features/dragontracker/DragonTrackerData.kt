package moe.ruruke.skyblock.features.dragontracker



import java.util.*


class DragonTrackerData {
    private val recentDragons: List<DragonType> = LinkedList()
    private val dragonsSince: Map<DragonsSince, Int> = EnumMap(
        DragonsSince::class.java
    )

    
    private val eyesPlaced = 0
}
