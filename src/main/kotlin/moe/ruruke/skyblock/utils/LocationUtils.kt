package moe.ruruke.skyblock.utils

import moe.ruruke.skyblock.core.Location
import java.util.*

/**
 * Class used to help with certain location places that has sub-locations like the Dwarven Mines
 */
object LocationUtils {
    // List of sublocations of the Dwarven Mines
    private val dwarvenLocations: List<Location> = ArrayList(
        Arrays.asList(
            Location.DWARVEN_MINES,
            Location.DWARVEN_VILLAGE,
            Location.GATES_TO_THE_MINES,
            Location.THE_LIFT,
            Location.THE_FORGE,
            Location.FORGE_BASIN,
            Location.LAVA_SPRINGS,
            Location.PALACE_BRIDGE,
            Location.ROYAL_PALACE,
            Location.ARISTOCRAT_PASSAGE,
            Location.HANGING_TERRACE,
            Location.CLIFFSIDE_VEINS,
            Location.RAMPARTS_QUARRY,
            Location.DIVANS_GATEWAY,
            Location.FAR_RESERVE,
            Location.GOBLIN_BURROWS,
            Location.UPPER_MINES,
            Location.ROYAL_MINES,
            Location.MINERS_GUILD,
            Location.GREAT_ICE_WALL,
            Location.THE_MIST,
            Location.CC_MINECARTS_CO,
            Location.GRAND_LIBRARY,
            Location.HANGING_COURT
        )
    )

    // List of sublocations of the Crystal Hollows
    private val hollowsLocations: List<Location> = ArrayList(
        Arrays.asList(
            Location.MAGMA_FIELDS,
            Location.CRYSTAL_HOLLOWS,
            Location.CRYSTAL_NUCLEUS,
            Location.JUNGLE,
            Location.MITHRIL_DEPOSITS,
            Location.GOBLIN_HOLDOUT,
            Location.PRECURSOR_REMNANT,
            Location.FAIRY_GROTTO,
            Location.KHAZAD_DUM,
            Location.JUNGLE_TEMPLE,
            Location.MINES_OF_DIVAN,
            Location.GOBLIN_QUEEN_DEN,
            Location.LOST_PRECURSOR_CITY
        )
    )

    // List of locations that spawn zealots/zealot variants
    private val zealotSpawnLocations: List<Location> =
        ArrayList(Arrays.asList(Location.DRAGONS_NEST, Location.ZEALOT_BRUISER_HIDEOUT))

    /**
     *
     * @param locationName - The location name
     * @return true if this sublocation is located within Dwarven Mines location
     */
    fun isInDwarvenMines(locationName: String): Boolean {
        for (location in dwarvenLocations) {
            if (location.getScoreboardName() == locationName) {
                return true
            }
        }
        return false
    }

    //same thing but for hollows
    fun isInCrystalHollows(locationName: String): Boolean {
        for (location in hollowsLocations) {
            if (location.getScoreboardName() == locationName) {
                return true
            }
        }
        return false
    }

    fun isZealotSpawnLocation(locationName: String): Boolean {
        for (location in zealotSpawnLocations) {
            if (location.getScoreboardName() == locationName) {
                return true
            }
        }
        return false
    }
}
