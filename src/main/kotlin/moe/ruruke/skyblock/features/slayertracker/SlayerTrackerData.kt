package moe.ruruke.skyblock.features.slayertracker



import moe.ruruke.skyblock.features.slayertracker.SlayerDrop
import java.util.*

class SlayerTrackerData {
    private val slayerKills: HashMap<SlayerBoss, Int> = hashMapOf()

    fun getSlayerKills(): HashMap<SlayerBoss, Int> {
        return slayerKills
    }
    private val slayerDropCounts: HashMap<SlayerDrop, Int> = hashMapOf()

    fun getSlayerDropCounts(): HashMap<SlayerDrop, Int> {
        return slayerDropCounts
    }

    private var lastKilledBoss: SlayerBoss? = null

    fun setLastKilledBoss(slayerBoss: SlayerBoss?) {
        lastKilledBoss = slayerBoss
    }

    fun getLastKilledBoss(): SlayerBoss?{
        return lastKilledBoss
    }
}
