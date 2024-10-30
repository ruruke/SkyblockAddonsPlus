package moe.ruruke.skyblock.features.tablist

import moe.ruruke.skyblock.utils.TextUtils

enum class TabStringType {
    TITLE,
    SUB_TITLE,
    TEXT,
    PLAYER;

    companion object {
        fun fromLine(line: String): TabStringType {
            val strippedLine: String = TextUtils.stripUsername(line)
            if (strippedLine.startsWith(" ")) {
                return TEXT
            }

            return if (!line.contains("§l") && TextUtils.isUsername(strippedLine)) {
                PLAYER
            } else {
                SUB_TITLE
            }
        }
    }
}
