package moe.ruruke.skyblock.utils

import com.google.gson.JsonObject
import moe.ruruke.skyblock.SkyblockAddonsPlus.Companion.getGson
import net.minecraft.util.IChatComponent
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * Collection of text/string related utility methods
 */

class TextUtils {
    companion object{

        /**
         * Hypixel uses US number format.
         */
        val NUMBER_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.US)

        private val STRIP_COLOR_PATTERN: Pattern = Pattern.compile("(?i)§[0-9A-FK-ORZ]")
        private val STRIP_ICONS_PATTERN: Pattern = Pattern.compile("[♲Ⓑ⚒ቾ]+")
        private val STRIP_PREFIX_PATTERN: Pattern = Pattern.compile("\\[[^\\[\\]]*\\]")
        private val REPEATED_COLOR_PATTERN: Pattern = Pattern.compile("(?i)(§[0-9A-FK-ORZ])+")
        private val NUMBERS_SLASHES: Pattern = Pattern.compile("[^0-9 /]")
        private val SCOREBOARD_CHARACTERS: Pattern = Pattern.compile("[^a-z A-Z:0-9_/'.!§\\[\\]❤]")
        private val FLOAT_CHARACTERS: Pattern = Pattern.compile("[^.0-9\\-]")
        private val INTEGER_CHARACTERS: Pattern = Pattern.compile("[^0-9]")
        private val TRIM_WHITESPACE_RESETS: Pattern = Pattern.compile("^(?:\\s|§r)*|(?:\\s|§r)*$")
        private val USERNAME_PATTERN: Pattern = Pattern.compile("[A-Za-z0-9_]+")
        private val RESET_CODE_PATTERN: Pattern = Pattern.compile("(?i)§R")
        private val MAGNITUDE_PATTERN: Pattern = Pattern.compile("(\\d[\\d,.]*\\d*)+([kKmMbBtT])")

        private val suffixes: NavigableMap<Int, String> = TreeMap()

        init {
            suffixes[1000] = "k"
            suffixes[1000000] = "M"
            suffixes[1000000000] = "B"
            NUMBER_FORMAT.maximumFractionDigits = 2
        }

        /**
         * Formats a double number to look better with commas every 3 digits and up to two decimal places.
         * For example: `1,006,789.5`
         *
         * @param number Number to format
         * @return Formatted string
         */

        fun formatDouble(number: Double): String {
            return NUMBER_FORMAT.format(number)
        }

        /**
         * Strips color codes from a given text
         *
         * @param input Text to strip colors from
         * @return Text without color codes
         */
        fun stripColor(input: String): String {
            return STRIP_COLOR_PATTERN.matcher(input).replaceAll("")
        }

        /**
         * Strips icons from player names
         * @param input Text to strip icons from
         * @return Text without icons
         */
        fun stripIcons(input: String): String {
            return STRIP_ICONS_PATTERN.matcher(input).replaceAll("")
        }

        /**
         * Strips icons and colors and trims spaces from a potential username
         * @param input Text to strip from
         * @return Stripped Text
         */
        fun stripUsername(input: String): String {
            return trimWhitespaceAndResets(stripIcons(stripColor(stripPrefix((input)))))
        }

        fun stripPrefix(input: String): String {
            return STRIP_PREFIX_PATTERN.matcher(input).replaceAll("")
        }

        /**
         * Computationally efficient way to test if a given string has a rendered length of 0
         * @param input string to test
         * @return `true` if the input string is length 0 or only contains repeated formatting codes
         */
        fun isZeroLength(input: String): Boolean {
            return input.length == 0 || REPEATED_COLOR_PATTERN.matcher(input).matches()
        }


        /**
         * Removes any character that isn't a number, letter, or common symbol from a given text.
         *
         * @param text Input text
         * @return Input text with only letters and numbers
         */
        fun keepScoreboardCharacters(text: String): String {
            return SCOREBOARD_CHARACTERS.matcher(text).replaceAll("")
        }

        /**
         * Removes any character that isn't a number, - or . from a given text.
         *
         * @param text Input text
         * @return Input text with only valid float number characters
         */
        fun keepFloatCharactersOnly(text: String): String {
            return FLOAT_CHARACTERS.matcher(text).replaceAll("")
        }

        /**
         * Removes any character that isn't a number from a given text.
         *
         * @param text Input text
         * @return Input text with only valid integer number characters
         */
        fun keepIntegerCharactersOnly(text: String): String {
            return INTEGER_CHARACTERS.matcher(text).replaceAll("")
        }

        /**
         * Removes any character that isn't a number from a given text.
         *
         * @param text Input text
         * @return Input text with only numbers
         */
        fun getNumbersOnly(text: String): String {
            return NUMBERS_SLASHES.matcher(text).replaceAll("")
        }

        /**
         * Converts all numbers with magnitudes in a given string, e.g. "10k" -> "10000" and "10M" -> "10000000." Magnitudes
         * are not case-sensitive.
         *
         * **Supported magnitudes:**
         *
         * k - thousand
         *
         * m - million
         *
         * b - billion
         *
         * t - trillion
         *
         *
         *
         *
         * **Examples:**
         *
         * 1k -> 1,000
         *
         * 2.5K -> 2,500
         *
         * 100M -> 100,000,000
         *
         * @param text - Input text
         * @return Input text with converted magnitudes
         */
        @Throws(ParseException::class)
        fun convertMagnitudes(text: String): String {
            val matcher = MAGNITUDE_PATTERN.matcher(text)
            val sb = StringBuffer()

            while (matcher.find()) {
                var parsedDouble = NUMBER_FORMAT.parse(matcher.group(1)).toDouble()
                val magnitude = matcher.group(2).lowercase()

                when (magnitude) {
                    "k" -> parsedDouble *= 1000.0
                    "m" -> parsedDouble *= 1000000.0
                    "b" -> parsedDouble *= 1000000000.0
                    "t" -> parsedDouble *= 1000000000000.0
                }

                matcher.appendReplacement(sb, NUMBER_FORMAT.format(parsedDouble))
            }
            matcher.appendTail(sb)

            return sb.toString()
        }

        /**
         * Removes any duplicate spaces from a given text.
         *
         * @param text Input text
         * @return Input text without repeating spaces
         */
        fun removeDuplicateSpaces(text: String): String {
            return text.replace("\\s+".toRegex(), " ")
        }

        /**
         * Reverses a given text while leaving the english parts intact and in order.
         * (Maybe its more complicated than it has to be, but it gets the job done.)
         *
         * @param originalText Input text
         * @return Reversed input text
         */
        fun reverseText(originalText: String): String {
            val newString = StringBuilder()
            val parts = originalText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in parts.size downTo 1) {
                val textPart = parts[i - 1]
                var foundCharacter = false
                for (letter in textPart.toCharArray()) {
                    if (letter.code > 191) { // Found special character
                        foundCharacter = true
                        newString.append(StringBuilder(textPart).reverse())
                        break
                    }
                }
                newString.append(" ")
                if (!foundCharacter) {
                    newString.insert(0, textPart)
                }
                newString.insert(0, " ")
            }
            return removeDuplicateSpaces(newString.toString().trim { it <= ' ' })
        }

        /**
         * Get the ordinal suffix of a number, meaning
         *
         *  * st - if n ends with 1 but isn't 11
         *  * nd - if n ends with 2 but isn't 12
         *  * rd - if n ends with 3 but isn't 13
         *  * th - in all other cases
         *
         */
        fun getOrdinalSuffix(n: Int): String {
            if (n >= 11 && n <= 13) {
                return "th"
            }
            return when (n % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

        /**
         * @param textureURL The texture ID/hash that is in the texture URL (not including http://textures.minecraft.net/texture/)
         * @return A json string including the texture URL as a skin texture (used in NBT)
         */
        fun encodeSkinTextureURL(textureURL: String): String {
            val skin = JsonObject()
            skin.addProperty("url", "http://textures.minecraft.net/texture/$textureURL")

            val textures = JsonObject()
            textures.add("SKIN", skin)

            val root = JsonObject()
            root.add("textures", textures)

            return Base64.getEncoder().encodeToString(getGson().toJson(root).toByteArray(StandardCharsets.UTF_8))
        }

        fun abbreviate(number: Int): String {
            if (number < 0) {
                return "-" + abbreviate(-number)
            }
            if (number < 1000) {
                return number.toString()
            }

            val entry = suffixes.floorEntry(number)
            val divideBy = entry.key
            val suffix = entry.value

            val truncated = number / (divideBy / 10) //the number part of the output times 10
            val hasDecimal = truncated < 100 && (truncated / 10.0) != (truncated / 10).toDouble()
            return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
        }


        /**
         * Removes all leading or trailing reset color codes and whitespace from a string.
         *
         * @param input Text to trim
         * @return Text without leading or trailing reset color codes and whitespace
         */
        fun trimWhitespaceAndResets(input: String): String {
            return TRIM_WHITESPACE_RESETS.matcher(input).replaceAll("")
        }

        /**
         * Checks if text matches a Minecraft username
         *
         * @param input Text to check
         * @return Whether this input can be Minecraft username or not
         */
        fun isUsername(input: String): Boolean {
            return USERNAME_PATTERN.matcher(input).matches()
        }

        /**
         * Removes all reset color codes from a given text
         *
         * @param input Text to strip
         * @return Text with all reset color codes removed
         */
        fun stripResets(input: String): String {
            return RESET_CODE_PATTERN.matcher(input).replaceAll("")
        }


        /**
         * Converts a string into proper case (Source: [Dev Notes](https://dev-notes.com))
         * @param inputString a string
         * @return a new string in which the first letter of each word is capitalized
         */
        fun toProperCase(inputString: String): String {
            val ret: String
            val sb = StringBuffer()
            val match = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(inputString)
            while (match.find()) {
                match.appendReplacement(
                    sb,
                    match.group(1).uppercase(Locale.getDefault()) + match.group(2).lowercase(Locale.getDefault())
                )
            }
            ret = match.appendTail(sb).toString()
            return ret
        }


        /**
         * Calculates and returns the first formatted substring that matches the unformatted string
         *
         *
         * Used for color/style compatibility mode.
         *
         * @param unformattedSubstring the uncolored/unstyled substring of which we request a match
         * @param formatted            the colored string, from which we request a substring
         * @return `null` if {@param unformattedSubstring} is not found in {@param formatted}, or the colored/styled substring.
         */
        fun getFormattedString(formatted: String, unformattedSubstring: String): String? {
            if (unformattedSubstring.length == 0) {
                return ""
            }
            val styles = "kKlLmMnNoO"
            var preEnchantFormat = StringBuilder()
            var formattedEnchant = StringBuilder()

            var i = -2
            val len = formatted.length
            var unformattedEnchantIdx = 0
            var k = 0
            while (true) {
                i = formatted.indexOf('§', i + 2)
                // No more formatting codes were found in the string
                if (i == -1) {
                    // Test if there is an instance of the formatted enchant in the rest of the string
                    while (k < len) {
                        // Enchant string matches at position k
                        if (formatted[k] == unformattedSubstring[unformattedEnchantIdx]) {
                            formattedEnchant.append(formatted[k])
                            unformattedEnchantIdx++
                            // We have matched the entire enchant. Return the current format + the formatted enchant
                            if (unformattedEnchantIdx == unformattedSubstring.length) {
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
                        if (formatted[k] == unformattedSubstring[unformattedEnchantIdx]) {
                            formattedEnchant.append(formatted[k])
                            unformattedEnchantIdx++
                            // We have matched the entire enchant. Return the current format + the formatted enchant
                            if (unformattedEnchantIdx == unformattedSubstring.length) {
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
                        val formatChar = formatted[i + 1]
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
         * Recursively performs an action upon a chat component and its siblings
         * This code is adapted from Skytils
         *
         *
         * https://github.com/Skytils/SkytilsMod/commit/35b1fbed1613f07bd422c61dbe3d261218b8edc6
         *
         *
         * I, Sychic, the author of this code grant usage under the terms of the MIT License.
         * @param chatComponent root chat component
         * @param action action to be performed
         * @author Sychic
         */
        fun transformAllChatComponents(chatComponent: IChatComponent, action: Consumer<IChatComponent?>) {
            action.accept(chatComponent)
            for (sibling in chatComponent.siblings) {
                transformAllChatComponents(sibling, action)
            }
        }

        /**
         * Recursively searches for a chat component to transform based on a given Predicate.
         *
         * Important to note that this function will stop on the first successful transformation, unlike [.transformAllChatComponents]
         * @param chatComponent root chat component
         * @param action predicate that transforms a component and reports a successful transformation
         * @return Whether any transformation occurred
         */
        fun transformAnyChatComponent(chatComponent: IChatComponent, action: Predicate<IChatComponent?>): Boolean {
            if (action.test(chatComponent)) return true
            for (sibling in chatComponent.siblings) {
                if (transformAnyChatComponent(sibling, action)) return true
            }
            return false
        }
    }
}