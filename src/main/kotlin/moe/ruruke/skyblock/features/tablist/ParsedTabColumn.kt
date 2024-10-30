package moe.ruruke.skyblock.features.tablist
import java.util.*

class ParsedTabColumn(private val title: String) {
    private val lines: MutableList<String> = LinkedList()
    private val sections: MutableList<ParsedTabSection> = LinkedList()

    fun getLines(): MutableList<String> {
        return lines
    }
    fun getSections(): MutableList<ParsedTabSection> {
        return sections
    }
    fun addLine(line: String) {
        lines.add(line)
    }
    fun getTitle(): String {
        return title
    }

    fun addSection(section: ParsedTabSection) {
        sections.add(section)
    }

    fun size(): Int {
        return lines.size + 1
    }
}
