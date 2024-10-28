//package moe.ruruke.skyblock.config
//
//import codes.biscuit.hypixellocalizationlib.HypixelLanguage
//import org.apache.logging.log4j.Logger
//import java.io.File
//import java.io.FileReader
//import java.io.FileWriter
//import java.util.concurrent.locks.ReentrantLock
//
//@Setter
//@Getter
//class PersistentValuesManager(configDir: File) {
//    private val persistentValuesFile =
//        File(configDir.absolutePath + "/skyblockaddons_persistent.cfg")
//
//    private var persistentValues = PersistentValues()
//
//    class PersistentValues {
//        var kills: Int = 0 // Kills since last eye
//        var totalKills: Int = 0 // Lifetime zealots killed
//        var summoningEyeCount: Int = 0 // Lifetime summoning eyes
//
//        private val slayerTracker: SlayerTrackerData = SlayerTrackerData()
//        private val dragonTracker: DragonTrackerData = DragonTrackerData()
//        private val storageCache: Map<String, CompressedStorage> = HashMap<String, CompressedStorage>()
//
//        private val blockCraftingIncompletePatterns = true // unused after crafting pattern removal
//        private val selectedCraftingPattern: CraftingPattern =
//            CraftingPattern.FREE // unused after crafting pattern removal
//
//        private val oresMined = 0
//        private val seaCreaturesKilled = 0
//
//        private val lastTimeFetchur = 0L // Last time the player gave Fetchur the correct item in ms from epoch
//
//        private val hypixelLanguage: HypixelLanguage = HypixelLanguage.ENGLISH
//    }
//
//
//    /**
//     * Loads the persistent values from `config/skyblockaddons_persistent.cfg` in the user's Minecraft folder.
//     */
//    fun loadValues() {
//        if (persistentValuesFile.exists()) {
//            try {
//                FileReader(persistentValuesFile).use { reader ->
//                    persistentValues = SkyblockAddons.getGson().fromJson(
//                        reader,
//                        PersistentValues::class.java
//                    )
//                }
//            } catch (ex: Exception) {
//                logger.error("Error loading persistent values.", ex)
//            }
//        } else {
//            saveValues()
//        }
//        FetchurManager.getInstance().postPersistentConfigLoad()
//    }
//
//    /**
//     * Saves the persistent values to `config/skyblockaddons_persistent.cfg` in the user's Minecraft folder.
//     */
//    fun saveValues() {
//        // TODO: Better error handling that tries again/tells the player if it fails
//        SkyblockAddons.runAsync {
//            if (!SAVE_LOCK.tryLock()) {
//                return@runAsync
//            }
//            logger.info("Saving persistent values")
//
//            try {
//                persistentValuesFile.createNewFile()
//
//                FileWriter(persistentValuesFile).use { writer ->
//                    SkyblockAddons.getGson().toJson(persistentValues, writer)
//                }
//            } catch (ex: Exception) {
//                logger.error("Error saving persistent values.", ex)
//            }
//
//            logger.info("Persistent Values Saved")
//            SAVE_LOCK.unlock()
//        }
//    }
//
//    /**
//     * Adds one to the summoning eye counter, adds the kills since last eye to the lifetime kill counter, and resets the kills since last eye counter.
//     */
//    fun addEyeResetKills() {
//        persistentValues.summoningEyeCount++
//        persistentValues.totalKills += persistentValues.kills
//        persistentValues.kills =
//            -1 // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
//        saveValues()
//    }
//
//    /**
//     * Resets all zealot counter stats.
//     */
//    fun resetZealotCounter() {
//        persistentValues.summoningEyeCount = 0
//        persistentValues.totalKills = 0
//        persistentValues.kills = 0
//        saveValues()
//    }
//
//    companion object {
//        private val logger: Logger = SkyblockAddons.getLogger()
//
//        private val SAVE_LOCK = ReentrantLock()
//    }
//}
