package moe.ruruke.skyblock.features.spookyevent

import moe.ruruke.skyblock.SkyblockAddonsPlus
import org.apache.logging.log4j.Logger
import java.util.regex.Pattern

// TODO: Feature Rewrite
object SpookyEventManager {
    private val logger: Logger = SkyblockAddonsPlus.getLogger()

    private val CANDY_PATTERN: Pattern =
        Pattern.compile("Your Candy: (?<greenCandy>\\d+) Green, (?<purpleCandy>\\d+) Purple \\((?<points>\\d+) pts\\.\\)")

    private val dummyCandyCounts: MutableMap<CandyType, Int> = HashMap()
    fun getDummyCandyCounts(): MutableMap<CandyType, Int> {
        return dummyCandyCounts
    }

    init {
        dummyCandyCounts[CandyType.GREEN] = 12
        dummyCandyCounts[CandyType.PURPLE] = 34
    }


    private val candyCounts: MutableMap<CandyType, Int> = HashMap()
    fun getCandyCounts(): MutableMap<CandyType, Int> {
        return candyCounts
    }

    private var points = 0
    fun getPoints(): Int {
        return points
    }

    init {
        reset()
    }

    fun reset() {
        for (candyType in CandyType.entries) {
            candyCounts[candyType] = 0
        }
        points = 0
    }

    val isActive: Boolean
        get() = getCandyCounts().get(CandyType.GREEN) !== 0 || getCandyCounts().get(CandyType.PURPLE) !== 0

    fun update(strippedTabFooterString: String?) {
        if (strippedTabFooterString == null) {
            reset()
            return
        }

        try {
            val matcher = CANDY_PATTERN.matcher(strippedTabFooterString)
            if (matcher.find()) {
                candyCounts[CandyType.GREEN] = matcher.group("greenCandy").toInt()
                candyCounts[CandyType.PURPLE] = matcher.group("purpleCandy").toInt()
                points = matcher.group("points").toInt()
            }
        } catch (ex: Exception) {
            logger.error("An error occurred while parsing the spooky event event text in the tab list!", ex)
        }
    }

    /**
     * Temp function until feature re-write
     *
     * @param green
     * @param purple
     * @param pts
     */
    fun update(green: Int, purple: Int, pts: Int) {
        candyCounts[CandyType.GREEN] = green
        candyCounts[CandyType.PURPLE] = purple
        points = pts
    }
}
