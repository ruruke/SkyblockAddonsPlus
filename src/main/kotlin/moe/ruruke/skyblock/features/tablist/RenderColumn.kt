package moe.ruruke.skyblock.features.tablist

import java.util.*
import kotlin.math.max

class RenderColumn {
    private val lines: MutableList<TabLine> = LinkedList()
    fun getLines(): MutableList<TabLine> {
        return lines
    }

    fun size(): Int {
        return lines.size
    }

    fun addLine(line: TabLine) {
        lines.add(line)
    }

    val maxWidth: Int
        get() {
            var maxWidth = 0

            for (tabLine in lines) {
                maxWidth = max(maxWidth.toDouble(), tabLine.getWidth().toDouble()).toInt()
            }

            return maxWidth
        }
}
