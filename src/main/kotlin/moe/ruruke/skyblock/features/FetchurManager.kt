package moe.ruruke.skyblock.features

import lombok.Getter
import lombok.Setter
import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.misc.scheduler.Scheduler
import moe.ruruke.skyblock.utils.ItemUtils
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import java.util.*

/**
 * Manages the Fetchur Feature, Pointing out which item Fetchur wants next
 *
 * @author Pedro9558
 */
class FetchurManager {
    @Getter
    private val fetchurTaskCompletedPhrase = "thanks thats probably what i needed"

    @Getter
    private val fetchurAlreadyDidTaskPhrase = "come back another time, maybe tmrw"

    // Used for storage, essential for Fetchur Warner
    @Getter
    @Setter
    private var currentItemSaved: FetchurItem? = null

    val currentFetchurItem: FetchurItem
        /**
         * Get the item fetchur needs today
         *
         * @return the item
         */
        get() {
            // Get the zero-based day of the month
            val dayIdx = getFetchurDayOfMonth(System.currentTimeMillis()) - 1
            return items[dayIdx % items.size]
        }

    /**
     * Figure out whether the player submitted today's fetchur item.
     * Can return incorrect answer if the player handed in Fetchur today, but sba wasn't loaded at the time.
     * Clicking Fetchur again (and reading the NPC response) will update the value to be correct.
     *
     * @return `true` iff the player hasn't yet submitted the item in today (EST).
     */
    fun hasFetchedToday(): Boolean {
        val lastTimeFetched: Long =
            SkyblockAddonsPlus.instance.persistentValuesManager!!.getPersistentValues().getLastTimeFetchur()
        val currTime = System.currentTimeMillis()
        // Return true iff the days of the month from last submission and current time match
        return currTime - lastTimeFetched < MILLISECONDS_IN_A_DAY && getFetchurDayOfMonth(lastTimeFetched) == getFetchurDayOfMonth(
            currTime
        )
    }

    /**
     * Returns the day of the month in the fetchur calendar (EST time zone)
     *
     * @param currTimeMilis Epoch UTC miliseconds (e.g. from [System.currentTimeMillis])
     * @return the 1-indexed day of the month in the fetchur time zone
     */
    private fun getFetchurDayOfMonth(currTimeMilis: Long): Int {
        fetchurCalendar.timeInMillis = currTimeMilis
        return fetchurCalendar[Calendar.DAY_OF_MONTH]
    }

    /**
     * Called periodically to check for any changes in the Fetchur item.
     * Will also notify the player of a change if enabled.
     */
    fun recalculateFetchurItem() {
        val item = currentFetchurItem
        if (item != currentItemSaved) {
            currentItemSaved = item
            val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
            // Warn player when there's a change
            //TODO:
//            if (main.configValues!!.isEnabled(Feature.WARN_WHEN_FETCHUR_CHANGES)) {
//                main.utils!!.playLoudSound("random.orb", 0.5)
//                main.getRenderListener().setTitleFeature(Feature.WARN_WHEN_FETCHUR_CHANGES)
//                main.getScheduler()
//                    .schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.configValues!!.getWarningSeconds())
//            }
        }
    }

    /**
     * Triggered if the player has just given the correct item, or has already given the correct item, to Fetchur.
     */
    fun saveLastTimeFetched() {
        val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance
        main.persistentValuesManager!!.getPersistentValues().setLastTimeFetchur(System.currentTimeMillis())
        main.persistentValuesManager!!.saveValues()
    }

    /**
     * Called after persistent loading to seed the saved item (so the warning doesn't trigger when joining skyblock)
     */
    fun postPersistentConfigLoad() {
        if (hasFetchedToday()) {
            currentItemSaved = currentFetchurItem
        }
    }

    /**
     * A class representing the item fetchur wants
     * containing the item instance and the text format of the item
     */
    class FetchurItem(@field:Getter private val itemStack: ItemStack, @field:Getter private val itemText: String) {
        fun getItemText(): String {
            return itemText
        }
        fun getItemStack(): ItemStack {
            return itemStack
        }
        override fun equals(anotherObject: Any?): Boolean {
            if (anotherObject !is FetchurItem) return false
            val another = anotherObject
            return another.getItemText() == this.getItemText() && another.getItemStack() == this.getItemStack()
        }
    }

    companion object {
        private const val MILLISECONDS_IN_A_DAY = (24 * 60 * 60 * 1000).toLong()

        // Hypixel timezone
        // Currently using new york timezone, gotta check november 7th to see if this still works
        @Getter
        private val fetchurZone: TimeZone = TimeZone.getTimeZone("America/New_York")
        private val fetchurCalendar: Calendar = GregorianCalendar(TimeZone.getTimeZone("America/New_York"))

        // A list containing the items fetchur wants
        // If you want to put it in a repository, YOU MUST PUT IT IN THE EXACT SAME ORDER AS I PLACED ON THIS LIST
        // Changing the order will affect the algorithm
        // I tried to fix(thats why they are in a different order) (if hypixel changes again it breaks)
        private val items = arrayOf(
            FetchurItem(ItemStack(Blocks.stained_glass, 20, 4), "Yellow Stained Glass"),
            FetchurItem(ItemStack(Items.compass, 1), "Compass"),
            FetchurItem(ItemStack(Items.prismarine_crystals, 20), "Mithril"),
            FetchurItem(ItemStack(Items.fireworks, 1), "Firework Rocket"),
            FetchurItem(
                ItemUtils.createSkullItemStack(
                    "§fCheap Coffee",
                    null,
                    "2fd02c32-6d35-3a1a-958b-e8c5a657c7d4",
                    "194221a0de936bac5ce895f2acad19c64795c18ce5555b971594205bd3ec"
                ), "Cheap Coffee"
            ),
            FetchurItem(ItemStack(Items.oak_door, 1), "Wooden Door"),
            FetchurItem(ItemStack(Items.rabbit_foot, 3), "Rabbit's Feet"),
            FetchurItem(ItemStack(Blocks.tnt, 1), "Superboom TNT"),
            FetchurItem(ItemStack(Blocks.pumpkin, 1), "Pumpkin"),
            FetchurItem(ItemStack(Items.flint_and_steel, 1), "Flint and Steel"),
            FetchurItem(
                ItemStack(Blocks.quartz_ore, 50),
                "Nether Quartz Ore"
            ),  //new FetchurItem(new ItemStack(Items.ender_pearl, 16), "Ender Pearl"),
            FetchurItem(ItemStack(Blocks.wool, 50, 14), "Red Wool")
        )

        @Getter
        private val instance = FetchurManager()

        fun getInstance(): FetchurManager {
            return instance
        }
    }
}
