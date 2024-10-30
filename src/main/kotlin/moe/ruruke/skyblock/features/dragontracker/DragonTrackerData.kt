package moe.ruruke.skyblock.features.dragontracker



import java.util.*
import kotlin.collections.HashMap


class DragonTrackerData {
    private val recentDragons: ArrayList<DragonType> = arrayListOf()
    private val dragonsSince: HashMap<DragonsSince, Int> = hashMapOf()
    fun getDragonsSince(): HashMap<DragonsSince, Int> = dragonsSince
    fun getRecentDragons(): ArrayList<DragonType> = recentDragons

    private var eyesPlaced = 0
    fun getEyesPlaced(): Int = eyesPlaced
    fun setEyesPlaced(eyesPlaced: Int) {
        this.eyesPlaced = eyesPlaced
    }
}
