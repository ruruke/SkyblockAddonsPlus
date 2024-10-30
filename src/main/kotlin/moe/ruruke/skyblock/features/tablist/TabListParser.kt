package moe.ruruke.skyblock.features.tablist

import moe.ruruke.skyblock.SkyblockAddonsPlus
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Location
import moe.ruruke.skyblock.core.SkillType
import moe.ruruke.skyblock.features.spookyevent.SpookyEventManager
import moe.ruruke.skyblock.features.tabtimers.TabEffectManager
import moe.ruruke.skyblock.utils.TextUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.client.network.NetworkPlayerInfo
import java.util.*
import java.util.regex.Matcher

object TabListParser {
    private val main: SkyblockAddonsPlus.Companion = SkyblockAddonsPlus.instance

    var HYPIXEL_ADVERTISEMENT_CONTAINS: String = "HYPIXEL.NET"

    private val GOD_POTION_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("You have a God Potion active! (?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})")
    private val ACTIVE_EFFECTS_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("Active Effects(?:§.)*(?:\\n(?:§.)*§7.+)*")
    private val COOKIE_BUFF_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("Cookie Buff(?:§.)*(?:\\n(§.)*§7.+)*")
    private val UPGRADES_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("(?<firstPart>§e[A-Za-z ]+)(?<secondPart> §f[0-9dhms ]+)")
    private val RAIN_TIME_PATTERN_S: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("Rain: (?<time>[0-9dhms ]+)")
    private val CANDY_PATTERN_S: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("Your Candy: (?<green>[0-9,]+) Green, (?<purple>[0-9,]+) Purple \\((?<points>[0-9,]+) pts\\.\\)")
    private val SKILL_LEVEL_S: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("Skills: (?<skill>[A-Za-z]+) (?<level>[0-9]+).*")

    private var renderColumns: MutableList<RenderColumn>? = mutableListOf()
    fun getRenderColumns(): MutableList<RenderColumn>? {
        return renderColumns
    }

    private var parsedRainTime: String? = null
    fun getParsedRainTime(): String? {
        return parsedRainTime
    }

    fun parse() {
        val mc: Minecraft = Minecraft.getMinecraft()

        if (!main.utils!!.isOnSkyblock() || (!main.configValues!!.isEnabled(Feature.COMPACT_TAB_LIST) &&
                    (!main.configValues!!.isEnabled(Feature.BIRCH_PARK_RAINMAKER_TIMER) || main.utils!!.getLocation() !== Location.BIRCH_PARK) &&
                    main.configValues!!.isDisabled(Feature.CANDY_POINTS_COUNTER) && main.configValues!!
                .isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP))
        ) {
            renderColumns = null
            return
        }

        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            renderColumns = null
            return
        }

        val netHandler: NetHandlerPlayClient= mc.thePlayer.sendQueue
        var fullList: List<NetworkPlayerInfo?> =
            GuiPlayerTabOverlay.field_175252_a.sortedCopy<NetworkPlayerInfo>(netHandler.getPlayerInfoMap())
        if (fullList.size < 80) {
            renderColumns = null
            return
        }
        fullList = fullList.subList(0, 80)


        // Parse into columns, combining any duplicate columns
        val columns = parseColumns(fullList)
        val footerAsColumn = parseFooterAsColumn()

        if (footerAsColumn != null) {
            columns.add(footerAsColumn)
        }

        // Parse every column into sections
        parseSections(columns)

        // Combine columns into how they will be rendered
        renderColumns = java.util.LinkedList()
        val renderColumn = RenderColumn()
        (renderColumns as LinkedList<RenderColumn>).add(renderColumn)
        combineColumnsToRender(columns, renderColumn)
    }

    fun getColumnFromName(columns: List<ParsedTabColumn>, name: String): ParsedTabColumn? {
        for (parsedTabColumn in columns) {
            if (name == parsedTabColumn.getTitle()) {
                return parsedTabColumn
            }
        }

        return null
    }

    private fun parseColumns(fullList: List<NetworkPlayerInfo?>): MutableList<ParsedTabColumn> {
        val tabList: GuiPlayerTabOverlay = Minecraft.getMinecraft().ingameGUI.getTabList()

        val columns: MutableList<ParsedTabColumn> = java.util.LinkedList()
        var entry = 0
        while (entry < fullList.size) {
            val title: String = TextUtils.trimWhitespaceAndResets(tabList.getPlayerName(fullList[entry]))
            var column = getColumnFromName(columns, title)
            if (column == null) {
                column = ParsedTabColumn(title)
                columns.add(column)
            }

            var columnEntry = entry + 1
            while (columnEntry < fullList.size && columnEntry < entry + 20) {
                column.addLine(tabList.getPlayerName(fullList[columnEntry]))
                columnEntry++
            }
            entry += 20
        }

        return columns
    }

    fun parseFooterAsColumn(): ParsedTabColumn? {
        val tabList: GuiPlayerTabOverlay = Minecraft.getMinecraft().ingameGUI.getTabList()

        if (tabList.footer == null) {
            return null
        }

        val column = ParsedTabColumn("§2§lOther")

        var footer: String = tabList.footer.getFormattedText()

        //System.out.println(footer);

        // Make active effects/booster cookie status compact...
        val m = GOD_POTION_PATTERN.matcher(tabList.footer.getUnformattedText())
        footer = if (m.find()) {
            ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll(
                ("Active Effects: §r§e" + TabEffectManager.getInstance()
                    .getEffectCount()).toString() + "\n§cGod Potion§r: " + m.group("timer")
            )
        } else {
            ACTIVE_EFFECTS_PATTERN.matcher(footer)
                .replaceAll("Active Effects: §r§e" + TabEffectManager.getInstance().getEffectCount())
        }

        var matcher = COOKIE_BUFF_PATTERN.matcher(footer)
        if (matcher.find() && matcher.group().contains("Not active!")) {
            footer = matcher.replaceAll("Cookie Buff \n§r§7Not Active")
        }

        for (line in java.util.ArrayList<String>(
            java.util.Arrays.asList<String>(
                *footer.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )) {
            // Lets not add the advertisements to the columns
            var line = line
            if (line.contains(HYPIXEL_ADVERTISEMENT_CONTAINS)) {
                continue
            }

            // Split every upgrade into 2 lines so it's not too long...
            matcher = UPGRADES_PATTERN.matcher(TextUtils.stripResets(line))
            if (matcher.matches()) {
                // Adds a space in front of any text that is not a sub-title
                var firstPart: String = TextUtils.trimWhitespaceAndResets(matcher.group("firstPart"))
                if (!firstPart.contains("§l")) {
                    firstPart = " $firstPart"
                }
                column.addLine(firstPart)

                line = matcher.group("secondPart")
            }
            // Adds a space in front of any text that is not a sub-title
            line = TextUtils.trimWhitespaceAndResets(line)
            if (!line.contains("§l")) {
                line = " $line"
            }

            column.addLine(line)
        }

        return column
    }

    fun parseSections(columns: List<ParsedTabColumn>) {
        parsedRainTime = null
        var foundSpooky = false
        var parsedSkill = false
        var m: Matcher? = null
        for (column in columns) {
            var currentSection: ParsedTabSection? = null
            for (line in column.getLines()) {
                // Empty lines reset the current section

                if (TextUtils.trimWhitespaceAndResets(line).isEmpty()) {
                    currentSection = null
                    continue
                }
                val stripped: String = TextUtils.stripColor(line).trim()
                if (parsedRainTime == null && (RAIN_TIME_PATTERN_S.matcher(stripped).also { m = it }).matches()) {
                    parsedRainTime = m!!.group("time");
                }
                if (!foundSpooky && (CANDY_PATTERN_S.matcher(stripped).also { m = it }).matches()) {
                    SpookyEventManager.update(
                        m!!.group("green").replace(",".toRegex(), "").toInt(),
                        m!!.group("purple").replace(",".toRegex(), "").toInt(),
                        m!!.group("points").replace(",".toRegex(), "").toInt()
                    )
                    foundSpooky = true
                }
                if (!parsedSkill && (SKILL_LEVEL_S.matcher(stripped).also { m = it }).matches()) {
                    val skillType: SkillType = SkillType.getFromString(m!!.group("skill"))!!
                    val level = m!!.group("level").toInt()
                    main.skillXpManager!!.setSkillLevel(skillType, level)
                    parsedSkill = true
                }

                if (currentSection == null) {
                    column.addSection(ParsedTabSection(column).also { currentSection = it })
                }

                currentSection!!.addLine(line)
            }
        }
        if (!foundSpooky) {
            SpookyEventManager.reset()
        }
    }

    fun combineColumnsToRender(columns: List<ParsedTabColumn>, initialColumn: RenderColumn) {
        var initialColumn = initialColumn
        var lastTitle: String? = null
        for (column in columns) {
            for (section in column.getSections()) {
                var sectionSize = section.size()

                // Check if we need to add the column title before this section
                var needsTitle = false
                if (lastTitle !== section.getColumn().getTitle()) {
                    needsTitle = true
                    sectionSize++
                }

                var currentCount = initialColumn.size()

                // The section is larger than max lines, we need to overflow
                if (sectionSize >= TabListRenderer.MAX_LINES / 2) { // TODO Double check this?

                    // If we are already at the max, we must start a new
                    // column so the title isn't by itself

                    if (currentCount >= TabListRenderer.MAX_LINES) {
                        renderColumns!!.add(RenderColumn().also { initialColumn = it })
                        currentCount = 1
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(TabLine("", TabStringType.TEXT))
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle()
                        initialColumn.addLine(TabLine(lastTitle, TabStringType.TITLE))
                        currentCount++
                    }

                    // Add lines 1 by 1, checking whether the count goes over the maximum.
                    // If it does go over the maximum add a new column
                    for (line in section.getLines()) {
                        if (currentCount >= TabListRenderer.MAX_LINES) {
                            renderColumns!!.add(RenderColumn().also { initialColumn = it })
                            currentCount = 1
                        }

                        initialColumn.addLine(TabLine(line, TabStringType.Companion.fromLine(line)))
                        currentCount++
                    }
                } else {
                    // This section will cause this column to go over the max, so let's
                    // move on to the next column
                    if (currentCount + sectionSize > TabListRenderer.MAX_LINES) {
                        renderColumns!!.add(RenderColumn().also { initialColumn = it })
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(TabLine("", TabStringType.TEXT))
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle()
                        initialColumn.addLine(TabLine(lastTitle, TabStringType.TITLE))
                    }

                    // And then add all the lines
                    for (line in section.getLines()) {
                        initialColumn.addLine(TabLine(line, TabStringType.Companion.fromLine(line)))
                    }
                }
            }
        }
    }
}
