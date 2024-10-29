package moe.ruruke.skyblock.features.cooldowns

/**
 * Class for easy cooldown management
 */
class CooldownEntry internal constructor(private val cooldown: Long) {
    private val lastUse = System.currentTimeMillis()

    val isOnCooldown: Boolean
        /**
         * Check whether this entry is on cooldown
         *
         * @return `true` if the cooldown is still active, `false` if it ran out
         */
        get() = System.currentTimeMillis() < (lastUse + cooldown)

    val remainingCooldown: Long
        /**
         * Get the remaining cooldown in milliseconds
         *
         * @return Milliseconds until the cooldown runs out
         */
        get() {
            val diff = (lastUse + cooldown) - System.currentTimeMillis()
            return if (diff <= 0) 0 else diff
        }

    val remainingCooldownPercent: Double
        /**
         * Get the remaining cooldown as a Percentage of the remaining time to the base cooldown
         *
         * @return Percentage between `0 to 1` or `0` if not on cooldown
         */
        get() = if (isOnCooldown) (remainingCooldown.toDouble()) / (cooldown.toDouble()) else 0.0

    companion object {
        /**
         * Entry with no cooldown
         */
        val NULL_ENTRY: CooldownEntry = CooldownEntry(0)
    }
}
