package moe.ruruke.skyblock.utils

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

object ScoreboardManager {
    val SIDEBAR_EMOJI_PATTERN: Pattern =
        Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+")

    private var scoreboardTitle: String? = ""

    fun getScoreboardTitle(): String? {
        return scoreboardTitle
    }

    private var strippedScoreboardTitle: String? = ""
    fun getStrippedScoreboardTitle(): String? {
        return strippedScoreboardTitle
    }

    private var scoreboardLines: MutableList<String>?  = mutableListOf()
    fun getScoreboardLines(): MutableList<String>? {
        return scoreboardLines
    }

    private var strippedScoreboardLines: MutableList<String>? = mutableListOf()
    fun getStrippedScoreboardLines(): MutableList<String>? {
        return strippedScoreboardLines
    }

    private var lastFoundScoreboard: Long = -1
    fun getLastFoundScoreboard(): Long {
        return lastFoundScoreboard
    }

    fun getNumberOfLines(): Int {
        return scoreboardLines!!.size
    }
    fun tick() {
        val mc = Minecraft.getMinecraft()
        if (mc?.theWorld == null || mc.isSingleplayer) {
            clear()
            return
        }

        val scoreboard = mc.theWorld.scoreboard
        val sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1)
        if (sidebarObjective == null) {
            clear()
            return
        }

        lastFoundScoreboard = System.currentTimeMillis()

        // Update titles
        scoreboardTitle = sidebarObjective.displayName
        strippedScoreboardTitle = TextUtils.stripColor(scoreboardTitle!!)

        // Update score lines
        var scores = scoreboard.getSortedScores(sidebarObjective)
        val filteredScores = scores.stream()
            .filter { p_apply_1_: Score -> p_apply_1_.playerName != null && !p_apply_1_.playerName.startsWith("#") }
            .collect(
                Collectors.toList()
            )
        scores = if (filteredScores.size > 15) {
            Lists.newArrayList(
                Iterables.skip(
                    filteredScores,
                    scores.size - 15
                )
            )
        } else {
            filteredScores
        }

        Collections.reverse(filteredScores)

        scoreboardLines = ArrayList()
        strippedScoreboardLines = ArrayList()

        for (line in scores) {
            val team = scoreboard.getPlayersTeam(line.playerName)
            val scoreboardLine = ScorePlayerTeam.formatPlayerName(team, line.playerName).trim { it <= ' ' }
            val cleansedScoreboardLine = SIDEBAR_EMOJI_PATTERN.matcher(scoreboardLine).replaceAll("")
            val strippedCleansedScoreboardLine = TextUtils.stripColor(cleansedScoreboardLine)

            (scoreboardLines as ArrayList<String>).add(cleansedScoreboardLine)
            (strippedScoreboardLines as ArrayList<String>).add(strippedCleansedScoreboardLine)
        }
    }

    private fun clear() {
        strippedScoreboardTitle = null
        scoreboardTitle = strippedScoreboardTitle
        strippedScoreboardLines = null
        scoreboardLines = strippedScoreboardLines
    }

    fun hasScoreboard(): Boolean {
        return scoreboardTitle != null
    }

}
