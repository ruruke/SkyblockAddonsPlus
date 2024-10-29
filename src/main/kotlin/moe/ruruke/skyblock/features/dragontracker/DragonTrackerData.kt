package moe.ruruke.skyblock.features.dragontracker

import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
class DragonTrackerData {
    private val recentDragons: List<DragonType> = LinkedList()
    private val dragonsSince: Map<DragonsSince, Int> = EnumMap(
        DragonsSince::class.java
    )

    @Setter
    private val eyesPlaced = 0
}
