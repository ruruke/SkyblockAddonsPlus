package moe.ruruke.skyblock.core

import moe.ruruke.skyblock.utils.TextUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This class represents a date (excluding the year) and time in Skyblock.
 *
 *
 *
 * Examples:
 *
 * Spring 28th
 *
 * 5:20pm ☀
 *
 *
 *
 * Spring 28th
 *
 * 9:10pm ☽
 */
class SkyblockDate(
    private val MONTH: SkyblockMonth?,
    private val DAY: Int,
    private val HOUR: Int,
    private val MINUTE: Int,
    private val PERIOD: String
) {
    /**
     * All the months of the Skyblock calendar
     */
    enum class SkyblockMonth(val scoreboardString: String) {
        EARLY_WINTER("Early Winter"),
        WINTER("Winter"),
        LATE_WINTER("Late Winter"),
        EARLY_SPRING("Early Spring"),
        SPRING("Spring"),
        LATE_SPRING("Late Spring"),
        EARLY_SUMMER("Early Summer"),
        SUMMER("Summer"),
        LATE_SUMMER("Late Summer"),
        EARLY_AUTUMN("Early Autumn"),
        AUTUMN("Autumn"),
        LATE_AUTUMN("Late Autumn");

        companion object {
            /**
             * Returns the `SkyblockMonth` value with the given name.
             *
             * @param scoreboardName the name of the month as it appears on the scoreboard
             * @return the `SkyblockMonth` value with the given name or `null` if a value with the given name
             * isn't found
             */
            fun fromName(scoreboardName: String): SkyblockMonth? {
                for (skyblockMonth: SkyblockMonth in entries) {
                    if (skyblockMonth.scoreboardString == scoreboardName) {
                        return skyblockMonth
                    }
                }
                return null
            }
        }
    }

    /**
     * Returns this Skyblock date as a String in the format:
     * Month Day, hh:mm
     *
     * @return this Skyblock date as a formatted String
     */
    override fun toString(): String {
        val monthName: String?

        if (MONTH != null) {
            monthName = MONTH.scoreboardString
        } else {
            monthName = null
        }

        return java.lang.String.format(
            "%s %s, %d:%s%s",
            monthName,
            "$DAY ${TextUtils.getOrdinalSuffix(DAY)}",
            HOUR,
            TextUtils.NUMBER_FORMAT.format(MINUTE),
            PERIOD
        )
    }

    companion object {
        private val DATE_PATTERN: Pattern = Pattern.compile("(?<month>[\\w ]+) (?<day>\\d{1,2})(?:th|st|nd|rd)")
        private val TIME_PATTERN: Pattern = Pattern.compile("(?<hour>\\d{1,2}):(?<minute>\\d\\d)(?<period>am|pm)")

        fun parse(dateString: String?, timeString: String?): SkyblockDate? {
            if (dateString == null || timeString == null) {
                return null
            }

            val dateMatcher: Matcher = DATE_PATTERN.matcher(dateString.trim { it <= ' ' })
            val timeMatcher: Matcher = TIME_PATTERN.matcher(timeString.trim { it <= ' ' })
            var day: Int = 1
            var hour: Int = 0
            var minute: Int = 0
            var month: String = SkyblockMonth.EARLY_SPRING.scoreboardString
            var period: String = "am"
            if (dateMatcher.find()) {
                month = dateMatcher.group("month")
                day = dateMatcher.group("day").toInt()
            }
            if (timeMatcher.find()) {
                hour = timeMatcher.group("hour").toInt()
                minute = timeMatcher.group("minute").toInt()
                period = timeMatcher.group("period")
            }
            return SkyblockDate(SkyblockMonth.fromName(month), day, hour, minute, period)
        }
    }
}
