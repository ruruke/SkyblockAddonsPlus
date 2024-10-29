package moe.ruruke.skyblock.core.seacreatures

import moe.ruruke.skyblock.core.ItemRarity


class SeaCreatureManager {
    private val allSeaCreatureSpawnMessages: MutableSet<String> = HashSet()

    fun getAllSeaCreatureSpawnMessages(): Set<String> {
        return allSeaCreatureSpawnMessages
    }

    private val legendarySeaCreatureSpawnMessages: MutableSet<String> = HashSet()
    fun getLegendarySeaCreatureSpawnMessages(): Set<String> {
        return legendarySeaCreatureSpawnMessages
    }

    /**
     * Populate sea creature information from local and online sources
     */
    fun setSeaCreatures(seaCreatures: Map<String?, SeaCreature>) {
        allSeaCreatureSpawnMessages.clear()
        legendarySeaCreatureSpawnMessages.clear()
        for (sc in seaCreatures.values) {
            allSeaCreatureSpawnMessages.add(sc.spawnMessage!!)
            if (sc.rarity!!.compareTo(ItemRarity.LEGENDARY) >= 0) {
                legendarySeaCreatureSpawnMessages.add(sc.spawnMessage!!)
            }
        }
    }

    companion object {
        val instance: SeaCreatureManager = SeaCreatureManager()
    }
}
