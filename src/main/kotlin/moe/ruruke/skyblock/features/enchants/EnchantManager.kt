package moe.ruruke.skyblock.features.enchants

import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.configValues
import moe.ruruke.skyblock.core.Feature
import moe.ruruke.skyblock.core.Translations.getMessage
import moe.ruruke.skyblock.features.enchants.EnchantManager.Enchant.Stacking
import moe.ruruke.skyblock.features.enchants.EnchantManager.Enchant.Ultimate
import moe.ruruke.skyblock.utils.ColorCode
import moe.ruruke.skyblock.utils.ItemUtils
import moe.ruruke.skyblock.utils.RomanNumeralParser.integerToRoman
import moe.ruruke.skyblock.utils.RomanNumeralParser.parseNumeral
import moe.ruruke.skyblock.utils.TextUtils.Companion.NUMBER_FORMAT
import moe.ruruke.skyblock.utils.TextUtils.Companion.abbreviate
import moe.ruruke.skyblock.utils.TextUtils.Companion.stripColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import org.lwjgl.input.Mouse
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.math.max

object EnchantManager {
    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    private val ENCHANTMENT_PATTERN: Pattern =
        Pattern.compile("(?<enchant>[A-Za-z][A-Za-z -]+) (?<levelNumeral>[IVXLCDM]+)(?=, |$| [\\d,]+$)")
    private val GREY_ENCHANT_PATTERN: Pattern =
        Pattern.compile("^(Respiration|Aqua Affinity|Depth Strider|Efficiency).*")
    private const val COMMA = ", "

    private val enchants = Enchants()

    private val loreCache = Cache()

    /**
     * Parse through enchantments, update the item's nbt, and cache the result for future queries
     *
     * @param loreList the current item lore (which may be processed by enchants)
     * @param item
     */
    fun parseEnchants(loreList: MutableList<String>, item: ItemStack) {
        val extraAttributes = ItemUtils.getExtraAttributes(item)
        val enchantNBT = extraAttributes?.getCompoundTag("enchantments")
        //TODO:
//        if (enchantNBT == null && SkyblockAddonsPlus.Companion.getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
//            return;
//        }
        // Add caching tooltip so continuous hover isn't so much of a problem
        if (loreCache.isCached(loreList)) {
            loreList.clear()
            loreList.addAll(loreCache.cachedAfter)
            return
        }
        // Update the cache so we have something to which to compare later
        loreCache.updateBefore(loreList)
        val config = configValues

        val fontRenderer = Minecraft.getMinecraft().fontRendererObj
        var startEnchant = -1
        var endEnchant = -1
        var maxTooltipWidth = 0
        val indexOfLastGreyEnchant = accountForAndRemoveGreyEnchants(loreList, item)
        val _a = if (indexOfLastGreyEnchant == -1) 0 else indexOfLastGreyEnchant + 1
        for (i in _a until loreList.size) {
            val u = loreList[i]
            val s = stripColor(u)
            if (startEnchant == -1) {
                if (containsEnchantment(enchantNBT, s)) {
                    startEnchant = i
                }
            } else if (s.trim { it <= ' ' }.length == 0 && endEnchant == -1) {
                endEnchant = i - 1
            }
            // Get max tooltip size, disregarding the enchants section
            if (startEnchant == -1 || endEnchant != -1) {
                maxTooltipWidth =
                    max(fontRenderer.getStringWidth(loreList[i]).toDouble(), maxTooltipWidth.toDouble())
                        .toInt()
            }
        }
        if (enchantNBT == null && endEnchant == -1) {
            endEnchant = startEnchant
        }
        if (endEnchant == -1) {
            loreCache.updateAfter(loreList)
            return
        }
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth)

        var hasLore = false
        val orderedEnchants = TreeSet<FormattedEnchant>()
        var lastEnchant: FormattedEnchant? = null
        // Order all enchants
        for (i in startEnchant..endEnchant) {
            val unformattedLine = stripColor(loreList[i])
            val m = ENCHANTMENT_PATTERN.matcher(unformattedLine)
            var containsEnchant = false
            while (m.find()) {
                // Pull out the enchantment and the enchantment level from lore
                val enchant = enchants.getFromLore(m.group("enchant"))
                val level = parseNumeral(m.group("levelNumeral"))
                if (enchant != null) {
                    // Get the original (input) formatting code of the enchantment, which may have been affected by other mods
                    var inputFormatEnchant: String? = "null"
                    if (config!!.isDisabled(Feature.ENCHANTMENTS_HIGHLIGHT)) {
                        inputFormatEnchant = getInputEnchantFormat(loreList[i], m.group())
                    }
                    lastEnchant = FormattedEnchant(enchant, level, inputFormatEnchant)
                    // Try to add the enchant to the list, otherwise find the same enchant that was already present in the list
                    if (!orderedEnchants.add(lastEnchant)) {
                        for (e in orderedEnchants) {
                            if (e.compareTo(lastEnchant) == 0) {
                                lastEnchant = e
                                break
                            }
                        }
                    }
                    containsEnchant = true
                }
            }
            // Add any enchantment lore that might follow an enchant to the lore description
            if (!containsEnchant && lastEnchant != null) {
                lastEnchant.addLore(loreList[i])
                hasLore = true
            }
        }
        val numEnchants = orderedEnchants.size

        for (enchant in orderedEnchants) {
            maxTooltipWidth = max(enchant.renderLength.toDouble(), maxTooltipWidth.toDouble()).toInt()
        }


        if (orderedEnchants.size == 0) {
            loreCache.updateAfter(loreList)
            return
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear()

        val insertEnchants: MutableList<String>
        val layout = config!!.enchantLayout.value
        // Pack as many enchantments as we can into one line (while not overstuffing it)
        if (layout == EnchantListLayout.COMPRESS && numEnchants != 1) {
            insertEnchants = ArrayList()

            // Get format for comma
            val comma = Objects.requireNonNull(configValues).getRestrictedColor(Feature.ENCHANTMENT_COMMA_COLOR)
                .toString() + COMMA
            val commaLength = fontRenderer.getStringWidth(comma)

            // Process each line of enchants
            var sum = 0
            var builder = StringBuilder(maxTooltipWidth)
            for (enchant in orderedEnchants) {
                // Check if there will be overflow on this line. This will never happen for a single enchant on a line
                if (sum + enchant.renderLength > maxTooltipWidth) {
                    builder.delete(builder.length - comma.length, builder.length)
                    insertEnchants.add(builder.toString())
                    builder = StringBuilder(maxTooltipWidth)
                    sum = 0
                }
                // Add the enchant followed by a comma
                builder.append(enchant.formattedString).append(comma)
                sum += enchant.renderLength + commaLength
            }
            // Flush any remaining enchants
            if (builder.length >= comma.length) {
                builder.delete(builder.length - comma.length, builder.length)
                insertEnchants.add(builder.toString())
            }
        } else if (layout == EnchantListLayout.NORMAL && !hasLore) {
            insertEnchants = ArrayList()
            if (config.isEnabled(Feature.ENCHANTMENTS_HIGHLIGHT)) {
                // Get format for comma
                val comma = configValues.getRestrictedColor(Feature.ENCHANTMENT_COMMA_COLOR).toString() + COMMA

                // Process each line of enchants
                var i = 0
                var builder = StringBuilder(maxTooltipWidth)
                for (enchant in orderedEnchants) {
                    // Add the enchant
                    builder.append(enchant.formattedString)
                    // Add a comma for the first on the row, followed by a comma
                    if (i % 2 == 0) {
                        builder.append(comma)
                    } else {
                        insertEnchants.add(builder.toString())
                        builder = StringBuilder(maxTooltipWidth)
                    }
                    i++
                }
                // Flush any remaining enchants
                if (builder.length >= comma.length) {
                    builder.delete(builder.length - comma.length, builder.length)
                    insertEnchants.add(builder.toString())
                }
            } else if (config.isDisabled(Feature.ENCHANTMENTS_HIGHLIGHT)) {
                // Get format for comma
                val comma = COMMA

                // Process each line of enchants
                var i = 0
                var builder = StringBuilder(maxTooltipWidth)
                for (enchant in orderedEnchants) {
                    // Add the enchant
                    builder.append(enchant.formattedString)
                    // Add a comma for the first on the row, followed by a comma
                    if (i % 2 == 0) {
                        builder.append(comma)
                    } else {
                        insertEnchants.add(builder.toString())
                        builder = StringBuilder(maxTooltipWidth)
                    }
                    i++
                }
                // Flush any remaining enchants
                if (builder.length >= comma.length) {
                    builder.delete(builder.length - comma.length, builder.length)
                    insertEnchants.add(builder.toString())
                }
            }
        } else {
            // Add each enchantment (one per line) + add enchant lore (if available)
            if (config.isDisabled(Feature.HIDE_ENCHANT_DESCRIPTION)) {
                insertEnchants = ArrayList((if (hasLore) 3 else 1) * numEnchants)
                for (enchant in orderedEnchants) {
                    // Add the enchant
                    insertEnchants.add(enchant.formattedString)
                    // Add the enchant lore (if any)
                    insertEnchants.addAll(enchant.lore)
                }
            } else {
                // Add each enchantment (one per line) and ignore enchant lore
                insertEnchants = ArrayList(numEnchants)
                for (enchant in orderedEnchants) {
                    // Add the enchant
                    insertEnchants.add(enchant.formattedString)
                }
            }
        }

        // Add all of the enchants to the lore
        loreList.addAll(startEnchant, insertEnchants)
        // Cache the result so we can use it again
        loreCache.updateAfter(loreList)
    }

    /**
     * Adds the progression to the next level to any of the stacking enchants
     *
     * @param loreList        the tooltip being built
     * @param extraAttributes the extra attributes tag of the item
     * @param insertAt        the position at which we should insert the tag
     * @return the index after the point at which we inserted new lines, or {@param insertAt} if we didn't insert anything.
     */
    fun insertStackingEnchantProgress(loreList: ArrayList<String?>, extraAttributes: NBTTagCompound?, insertAt: Int): Int {
        var insertAt = insertAt
        if (extraAttributes == null || configValues!!.isDisabled(Feature.SHOW_STACKING_ENCHANT_PROGRESS)) {
            return insertAt
        }
        for (enchant in enchants.STACKING.values) {
            if (extraAttributes.hasKey(enchant.nbtNum, Constants.NBT.TAG_ANY_NUMERIC)) {
                val stackedEnchantNum = extraAttributes.getInteger(enchant.nbtNum)
                val nextLevel = enchant.stackLevel!!.higher(stackedEnchantNum)
                val statLabel = getMessage("enchants." + enchant.statLabel)
                val colorCode = configValues.getRestrictedColor(Feature.SHOW_STACKING_ENCHANT_PROGRESS)
                val b = StringBuilder()
                b.append("§7").append(statLabel).append(": ").append(colorCode)
                if (nextLevel == null) {
                    // §7Expertise Kills: §a5000000000 §7(Maxed)
                    b.append(abbreviate(stackedEnchantNum)).append(" §7(").append(getMessage("enchants.maxed"))
                        .append(")")
                } else {
                    // §7Expertise Kills: §a500 §7/ 1k
                    val format: String = NUMBER_FORMAT.format(stackedEnchantNum)
                    b.append(format).append(" §7/ ").append(abbreviate(nextLevel))
                }
                loreList.add(insertAt++, b.toString())
            }
        }
        return insertAt
    }

    /**
     * Helper method to determine whether we should skip this line in parsing the lore.
     * E.g. we want to skip "Breaking Power X" seen on pickaxes.
     *
     * @param enchantNBT the enchantments extraAttributes NBT of the item
     * @param s          the line of lore we are parsing
     * @return `true` if no enchants on the line are in the enchants table, `false` otherwise.
     */
    fun containsEnchantment(enchantNBT: NBTTagCompound?, s: String): Boolean {
        val m = ENCHANTMENT_PATTERN.matcher(s)
        while (m.find()) {
            val enchant = enchants.getFromLore(m.group("enchant"))
            if (enchantNBT == null || enchantNBT.hasKey(enchant.nbtName)) {
                return true
            }
        }
        return false
    }

    /**
     * Calculates and returns the format of the unformatted enchant in the formatted enchants line
     *
     *
     * Used for color/style compatibility mode.
     *
     * @param formattedEnchants  the colored/styled line of lore with enchants
     * @param unformattedEnchant the uncolored/unstyled enchant name
     * @return `null` if {@param unformattedEnchant} is not found in {@param formattedEnchants}, or the colored/styled enchant substring.
     */
    private fun getInputEnchantFormat(formattedEnchants: String, unformattedEnchant: String): String? {
        if (unformattedEnchant.length == 0) {
            return ""
        }
        val styles = "kKlLmMnNoO"
        var preEnchantFormat = StringBuilder()
        var formattedEnchant = StringBuilder()

        var i = -2
        val len = formattedEnchants.length
        var unformattedEnchantIdx = 0
        var k = 0
        while (true) {
            i = formattedEnchants.indexOf('§', i + 2)
            // No more formatting codes were found in the string
            if (i == -1) {
                // Test if there is an instance of the formatted enchant in the rest of the string
                while (k < len) {
                    // Enchant string matches at position k
                    if (formattedEnchants[k] == unformattedEnchant[unformattedEnchantIdx]) {
                        formattedEnchant.append(formattedEnchants[k])
                        unformattedEnchantIdx++
                        // We have matched the entire enchant. Return the current format + the formatted enchant
                        if (unformattedEnchantIdx == unformattedEnchant.length) {
                            return preEnchantFormat.append(formattedEnchant).toString()
                        }
                    } else {
                        unformattedEnchantIdx = 0
                        // Transfer formats from formatted enchant to format
                        preEnchantFormat =
                            StringBuilder(mergeFormats(preEnchantFormat.toString(), formattedEnchant.toString()))
                        formattedEnchant = StringBuilder()
                    }
                    k++
                }
                // No matching enchant found
                return null
            } else {
                while (k < i) {
                    if (formattedEnchants[k] == unformattedEnchant[unformattedEnchantIdx]) {
                        formattedEnchant.append(formattedEnchants[k])
                        unformattedEnchantIdx++
                        // We have matched the entire enchant. Return the current format + the formatted enchant
                        if (unformattedEnchantIdx == unformattedEnchant.length) {
                            return preEnchantFormat.append(formattedEnchant).toString()
                        }
                    } else {
                        unformattedEnchantIdx = 0
                        // Transfer formats from formatted enchant to format
                        preEnchantFormat =
                            StringBuilder(mergeFormats(preEnchantFormat.toString(), formattedEnchant.toString()))
                        formattedEnchant = StringBuilder()
                    }
                    k++
                }
                // Add the format code if present
                if (i + 1 < len) {
                    val formatChar = formattedEnchants[i + 1]
                    // If not parsing an enchant, alter the pre enchant format
                    if (unformattedEnchantIdx == 0) {
                        // Restart format at a new color
                        if (styles.indexOf(formatChar) == -1) {
                            preEnchantFormat = StringBuilder()
                        }
                        // Append the new format code to the formatter
                        preEnchantFormat.append("§").append(formatChar)
                    } else {
                        // Restart format at a new color
                        formattedEnchant.append("§").append(formatChar)
                    }
                    // Skip the formatting code "§[0-9a-zA-Z]" on the next round
                    k = i + 2
                }
            }
        }
    }

    /**
     * Calculate the color/style formatting after first and second format strings
     *
     *
     * Used for: Given the color/style formatting before an enchantment. as well as the enchantment itself,
     * Calculate the color/style formatting after the enchantment
     *
     * @param firstFormat  the color/style formatting before the string
     * @param secondFormat the string that may have formatting codes within it
     * @return the relevant formatting codes in effect after {@param secondFormat}
     */
    private fun mergeFormats(firstFormat: String, secondFormat: String?): String {
        if (secondFormat == null || secondFormat.length == 0) {
            return firstFormat
        }
        val styles = "kKlLmMnNoO"
        var builder = StringBuilder(firstFormat)
        var i = -2
        while ((secondFormat.indexOf('§', i + 2).also { i = it }) != -1) {
            if (i + 1 < secondFormat.length) {
                val c = secondFormat[i + 1]
                // If it's not a style then it's a color code
                if (styles.indexOf(c) == -1) {
                    builder = StringBuilder()
                }
                builder.append("§").append(c)
            }
        }
        return builder.toString()
    }

    /**
     * Counts (and optionally removes) vanilla grey enchants that are added on the first 1-2 lines of lore.
     * Removal of the grey enchants is specified by the [Feature.HIDE_GREY_ENCHANTS] feature.
     *
     * @param tooltip the tooltip being built
     * @param item    to which the tooltip corresponds
     * @return an integer denoting the last index of a grey enchantment, or -1 if none were found.
     */
    private fun accountForAndRemoveGreyEnchants(tooltip: MutableList<String>, item: ItemStack): Int {
        // No grey enchants will be added if there is no vanilla enchantments tag
        if (item.enchantmentTagList == null || item.enchantmentTagList.tagCount() == 0) {
            return -1
        }
        var lastGreyEnchant = -1
        val removeGreyEnchants = configValues!!.isEnabled(Feature.HIDE_GREY_ENCHANTS)

        // Start at index 1 since index 0 is the title
        var total = 0
        var i = 1
        while (total < 1 + item.enchantmentTagList.tagCount() && i < tooltip.size) {
            // only a max of 2 gray enchants are possible
            val line = tooltip[i]
            if (GREY_ENCHANT_PATTERN.matcher(line).matches()) {
                lastGreyEnchant = i

                if (removeGreyEnchants) {
                    tooltip.removeAt(i)
                }
            } else {
                i++
            }
            total++
        }
        return if (removeGreyEnchants) -1 else lastGreyEnchant
    }


    private fun correctTooltipWidth(maxTooltipWidth: Int): Int {
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        var maxTooltipWidth = maxTooltipWidth
        val scaledresolution = ScaledResolution(Minecraft.getMinecraft())
        val mouseX = Mouse.getX() * scaledresolution.scaledWidth / Minecraft.getMinecraft().displayWidth
        var tooltipX = mouseX + 12
        if (tooltipX + maxTooltipWidth + 4 > scaledresolution.scaledWidth) {
            tooltipX = mouseX - 16 - maxTooltipWidth
            if (tooltipX < 4) {
                maxTooltipWidth = if (mouseX > scaledresolution.scaledWidth / 2) {
                    mouseX - 12 - 8
                } else {
                    scaledresolution.scaledWidth - 16 - mouseX
                }
            }
        }

        if (scaledresolution.scaledWidth > 0 && maxTooltipWidth > scaledresolution.scaledWidth) {
            maxTooltipWidth = scaledresolution.scaledWidth
        }
        return maxTooltipWidth
    }

    fun markCacheDirty() {
        loreCache.configChanged = true
    }

    class Enchants {
        var NORMAL: HashMap<String, Enchant.Normal> = HashMap()
        var ULTIMATE: HashMap<String, Ultimate> = HashMap()
        var STACKING: HashMap<String, Stacking> = HashMap()

        fun getFromLore(loreName: String): Enchant {
            var loreName = loreName
            loreName = loreName.lowercase()
            var enchant: Enchant? = NORMAL[loreName]
            if (enchant == null) {
                enchant = ULTIMATE[loreName]
            }
            if (enchant == null) {
                enchant = STACKING[loreName]
            }
            if (enchant == null) {
                enchant = Enchant.Dummy(loreName)
            }
            return enchant
        }

        override fun toString(): String {
            return "NORMAL:\n$NORMAL\nULTIMATE:\n$ULTIMATE\nSTACKING:\n$STACKING"
        }
    }


    open class Enchant : Comparable<Enchant?> {
        lateinit var nbtName: String
        lateinit var unformattedName: String
        var goodLevel: Int = 0
        var maxLevel: Int = 0

        val isNormal: Boolean
            get() = this is Normal

        val isUltimate: Boolean
            get() = this is Ultimate

        val isStacking: Boolean
            get() = this is Stacking

        fun getFormattedName(level: Int): String {
            return getFormat(level) + unformattedName
        }

        open fun getFormat(level: Int): String {
            val config = configValues
            if (level >= maxLevel) {
                return config!!.getRestrictedColor(Feature.ENCHANTMENT_PERFECT_COLOR).toString()
            }
            if (level > goodLevel) {
                return config!!.getRestrictedColor(Feature.ENCHANTMENT_GREAT_COLOR).toString()
            }
            if (level == goodLevel) {
                return config!!.getRestrictedColor(Feature.ENCHANTMENT_GOOD_COLOR).toString()
            }
            return config!!.getRestrictedColor(Feature.ENCHANTMENT_POOR_COLOR).toString()
        }

        override fun toString(): String {
            return "$nbtName $goodLevel $maxLevel\n"
        }


        /**
         * Orders enchants by type in the following way:
         * 1) Ultimates (alphabetically)
         * 2) Stacking (alphabetically)
         * 3) Normal (alphabetically)
         */

        override fun compareTo(other: Enchant?): Int {
            if (this.isUltimate == other!!.isUltimate) {
                if (this.isStacking == other.isStacking) {
                    return unformattedName.compareTo(other.unformattedName)
                }
                return if (this.isStacking) -1 else 1
            }
            return if (this.isUltimate) -1 else 1
        }


        class Normal : Enchant()

        class Ultimate : Enchant() {
            override fun getFormat(level: Int): String {
                return "§d§l"
            }
        }

        class Stacking : Enchant() {
            var nbtNum: String? = null
            var statLabel: String? = null
            var stackLevel: TreeSet<Int>? = null

            override fun toString(): String {
                return nbtNum + " " + stackLevel.toString() + " " + super.toString()
            }
        }

        internal class Dummy(name: String) : Enchant() {
            init {
                unformattedName = name
                nbtName = name.lowercase(Locale.getDefault()).replace(" ".toRegex(), "_")
            }

            override fun getFormat(level: Int): String {
                return ColorCode.DARK_RED.toString()
            }
        }
    }


    internal class Cache {
        var cachedAfter: List<String> = ArrayList()

        var configChanged: Boolean = false

        var cachedBefore: List<String> = ArrayList()
            private set

        fun updateBefore(loreBeforeModifications: List<String>) {
            cachedBefore = ArrayList(loreBeforeModifications)
        }

        fun updateAfter(loreAfterModifications: List<String>) {
            cachedAfter = ArrayList(loreAfterModifications)
            configChanged = false
        }

        fun isCached(loreBeforeModifications: List<String>): Boolean {
            if (configChanged || loreBeforeModifications.size != cachedBefore.size) {
                return false
            }
            for (i in loreBeforeModifications.indices) {
                if (loreBeforeModifications[i] != cachedBefore[i]) {
                    return false
                }
            }
            return true
        }
    }

    internal class FormattedEnchant(var enchant: Enchant, var level: Int, var inputFormattedString: String?) :
        Comparable<FormattedEnchant?> {
        var loreDescription: MutableList<String> = ArrayList()

        fun addLore(lineOfEnchantLore: String) {
            loreDescription.add(lineOfEnchantLore)
        }

        val lore: List<String>
            get() = loreDescription

        val renderLength: Int
            get() = Minecraft.getMinecraft().fontRendererObj.getStringWidth(formattedString)

        val formattedString: String
            get() {
                val config = configValues
                val b = StringBuilder()
                if (config!!.isEnabled(Feature.ENCHANTMENTS_HIGHLIGHT)) {
                    b.append(enchant.getFormattedName(level))
                } else {
                    return inputFormattedString!!
                }
                b.append(" ")
                if (config.isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
                    b.append(level)
                } else {
                    b.append(integerToRoman(level))
                }

                return b.toString()
            }

        override fun compareTo(other: FormattedEnchant?): Int {
            return enchant.compareTo(other?.enchant)
        }
    }
}
