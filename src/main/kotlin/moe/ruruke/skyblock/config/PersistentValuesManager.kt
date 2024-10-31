package moe.ruruke.skyblock.config

import codes.biscuit.hypixellocalizationlib.HypixelLanguage


import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Language
import moe.ruruke.skyblock.features.FetchurManager
import moe.ruruke.skyblock.features.backpacks.CompressedStorage
import moe.ruruke.skyblock.features.craftingpatterns.CraftingPattern
import moe.ruruke.skyblock.features.dragontracker.DragonTrackerData
import moe.ruruke.skyblock.features.slayertracker.SlayerTrackerData

import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantLock



class PersistentValuesManager(configDir: File) {
    private val persistentValuesFile =
        File(configDir.absolutePath + "/skyblockaddons_persistent.cfg")

    private var persistentValues = PersistentValues()

    class PersistentValues {
        private var kills: Int = 0 // Kills since last eye
        fun getKills(): Int {
            return kills
        }
        fun setKills(v: Int){
            kills = v
        }
        var totalKills: Int = 0 // Lifetime zealots killed
        var summoningEyeCount: Int = 0 // Lifetime summoning eyes

        private val slayerTracker: SlayerTrackerData = SlayerTrackerData()
        private val dragonTracker: DragonTrackerData = DragonTrackerData()
        private val storageCache: MutableMap<String, CompressedStorage> = HashMap<String, CompressedStorage>()
        fun getLastTimeFetchur(): Long {
            return lastTimeFetchur
        }
        fun getStorageCache(): MutableMap<String, CompressedStorage> {
            return storageCache
        }
        fun setLastTimeFetchur(lasttime: Long){
            lastTimeFetchur = lasttime
        }
        fun getSlayerTracker(): SlayerTrackerData {
            return slayerTracker
        }
        fun getDragonTracker(): DragonTrackerData {
            return dragonTracker
        }
        fun getStorage(): Map<String, CompressedStorage> {
            return storageCache
        }
        fun setHypixelLanguage(language: HypixelLanguage){
            hypixelLanguage = language
        }
        fun setOresMined(mind: Int){
            oresMined = mind
        }
        fun setSeaCreaturesKilled(killed: Int){
            seaCreaturesKilled = killed
        }
        fun setSelectedCraftingPattern(pattern: CraftingPattern){
            selectedCraftingPattern = pattern
        }
        private var blockCraftingIncompletePatterns = true // unused after crafting pattern removal
        fun getBlockCraftingIncompletePatterns(): Boolean {
            return blockCraftingIncompletePatterns
        }
        fun isBlockCraftingIncompletePatterns(): Boolean {
            return blockCraftingIncompletePatterns
        }
        fun setBlockCraftingIncompletePatterns(value: Boolean){
            blockCraftingIncompletePatterns = value
        }
        private var selectedCraftingPattern: CraftingPattern =
            CraftingPattern.FREE // unused after crafting pattern removal
        fun getSelectedCraftingPattern(): CraftingPattern {
            return selectedCraftingPattern
        }

        private var oresMined = 0
        fun getOresMined(): Int {
            return oresMined
        }
        private var seaCreaturesKilled = 0
        fun getSeaCreaturesKilled(): Int {
            return seaCreaturesKilled
        }

        private var lastTimeFetchur = 0L // Last time the player gave Fetchur the correct item in ms from epoch
        fun getLastFetchTime(): Long {
            return lastTimeFetchur
        }

        private var hypixelLanguage: HypixelLanguage = HypixelLanguage.ENGLISH
        fun getHypixelLanguage(): HypixelLanguage {
            return hypixelLanguage
        }
    }


    /**
     * Loads the persistent values from `config/skyblockaddons_persistent.cfg` in the user's Minecraft folder.
     */
    fun loadValues() {
        if (persistentValuesFile.exists()) {
            try {
                FileReader(persistentValuesFile).use { reader ->
                    persistentValues = SkyblockAddonsPlus.getGson().fromJson(
                        reader,
                        PersistentValues::class.java
                    )
                }
            } catch (ex: Exception) {
                logger.error("Error loading persistent values.", ex)
            }
        } else {
            saveValues()
        }
        FetchurManager.getInstance().postPersistentConfigLoad()
    }

    /**
     * Saves the persistent values to `config/skyblockaddons_persistent.cfg` in the user's Minecraft folder.
     */
    fun saveValues() {
        // TODO: Better error handling that tries again/tells the player if it fails
        SkyblockAddonsPlus.runAsync {
            if (!SAVE_LOCK.tryLock()) {
                return@runAsync
            }
            logger.info("Saving persistent values")

            try {
                persistentValuesFile.createNewFile()

                FileWriter(persistentValuesFile).use { writer ->
                    SkyblockAddonsPlus.getGson().toJson(persistentValues, writer)
                }
            } catch (ex: Exception) {
                logger.error("Error saving persistent values.", ex)
            }

            logger.info("Persistent Values Saved")
            SAVE_LOCK.unlock()
        }
    }

    /**
     * Adds one to the summoning eye counter, adds the kills since last eye to the lifetime kill counter, and resets the kills since last eye counter.
     */
    fun addEyeResetKills() {
        persistentValues.summoningEyeCount++
        persistentValues.totalKills += persistentValues.getKills()
        persistentValues.setKills(-1) // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        saveValues()
    }
    fun getPersistentValues(): PersistentValues {
        return persistentValues
    }
    /**
     * Resets all zealot counter stats.
     */
    fun resetZealotCounter() {
        persistentValues.summoningEyeCount = 0
        persistentValues.totalKills = 0
        persistentValues.setKills(0)
        saveValues()
    }

    companion object {
        private val logger: Logger = SkyblockAddonsPlus.getLogger()

        private val SAVE_LOCK = ReentrantLock()
    }
}
