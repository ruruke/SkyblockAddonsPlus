package moe.ruruke.skyblock.features.tabtimers

import moe.ruruke.skyblock.utils.TextUtils
import kotlin.math.abs

class TabEffect(//Effect Name, eg. "Critical"
    private var effect: String, //Duration String, eg. "01:20"
    private val duration: String
) : Comparable<TabEffect> {
    fun setEffect(effect: String) {
        this.effect = effect
    }
    fun getEffect(): String {
        return effect
    }
    fun getDuration(): String {
        return effect
    }
    fun getDurationSeconds(): Int {
        return durationSeconds
    }

    private var durationSeconds: Int //Duration in seconds, eg. 80
    fun setDurationSeconds(durationSeconds: Int) {
        this.durationSeconds = durationSeconds
    }
    fun durationSeconds(): Int {
        return durationSeconds
    }

    init {
        val s = duration.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        durationSeconds = 0
        for (i in s.size downTo 1) {

            durationSeconds = (durationSeconds + s[i - 1].toInt() * (Math.pow(60.0,(s.size - i).toDouble()))).toInt()
        }
    }

    val durationForDisplay: String
        get() = "§r$duration"

    override fun compareTo(o: TabEffect): Int {
        val difference: Int = o.getDurationSeconds() - getDurationSeconds()

        if (abs(difference.toDouble()) <= 1) {
            return TextUtils.stripColor(o.getEffect()).compareTo(TextUtils.stripColor(getEffect()))
        }

        return difference
    }
}
