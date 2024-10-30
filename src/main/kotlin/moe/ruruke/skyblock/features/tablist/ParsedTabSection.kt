package moe.ruruke.skyblock.features.tablist

import java.util.*

class ParsedTabSection(private val column: ParsedTabColumn) {
    private val lines: MutableList<String> = LinkedList()
    fun getLines(): MutableList<String> {
        return lines
    }
    fun getColumn(): ParsedTabColumn {
        return column
    }

    fun addLine(line: String) {
        lines.add(line)
    }

    fun size(): Int {
        return lines.size
    }
}
