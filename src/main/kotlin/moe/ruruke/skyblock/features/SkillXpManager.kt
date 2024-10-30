package moe.ruruke.skyblock.features

import moe.ruruke.skyblock.core.SkillType


class SkillXpManager {
    /**
     * Returns the amount of xp needed to progress from `level` to `level + 1`
     *
     * @param type  the skill to query
     * @param level the level to query
     * @return the amount of xp needed to progress to `level + 1`
     */
    fun getSkillXpForNextLevel(type: SkillType?, level: Int): Int {
        return if (skillLevelXp.containsKey(type)) skillLevelXp[type]!!
            .getXpForNextLevel(level) else 0
    }

    fun initialize(input: JsonInput) {
        for ((key, value) in input) {
            skillLevelXp[key] =
                SkillXp(
                    value!!
                )
        }
    }

    /**
     * Sets the player's skill level
     *
     * @param type  the skill
     * @param level the level
     */
    fun setSkillLevel(type: SkillType, level: Int) {
        playerSkillLevel[type] = level
    }

    /**
     * Get the last stored skill level for the given skill
     *
     * @param type the skill to query
     * @return the last stored skill level, or -1 if not found
     */
    fun getSkillLevel(type: SkillType): Int {
        return playerSkillLevel.getOrDefault(type, -1)
    }

    class JsonInput : HashMap<SkillType?, List<Int?>?>()

    private class SkillXp(
        /**
         * Look-back values for a given skill level. "How much xp did I need to reach level x?"
         */
        private val cumulativeXp: List<Int?>
    ) {
        /**
         * Look-ahead values for a given skill level. "How much xp do I need to get from level x to x + 1?"
         */
        private val xpForNext: MutableList<Int> = ArrayList(cumulativeXp.size)

        /**
         * The maximum level for this skill
         */
        val maxLevel: Int = cumulativeXp.size - 1

        init {
            for (i in 0 until cumulativeXp.size - 1) {
                xpForNext.add(cumulativeXp[i + 1]!! - cumulativeXp[i]!!)
            }
        }

        fun getXpForNextLevel(level: Int): Int {
            return if (level >= maxLevel || level < 0) 0 else xpForNext[level]
        }
    }


    companion object {
        /**
         * The hypixel skill xp requirements for each skill level
         */
        private val skillLevelXp = HashMap<SkillType?, SkillXp>()

        /**
         * The player's skill level for each skill
         */
        private val playerSkillLevel = HashMap<SkillType, Int>()
    }
}
