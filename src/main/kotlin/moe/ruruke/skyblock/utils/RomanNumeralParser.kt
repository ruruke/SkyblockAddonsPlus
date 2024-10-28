package moe.ruruke.skyblock.utils

import java.util.*
import java.util.regex.Pattern

/**
 * Utility class for working with Roman numerals
 * @author DidiSkywalker
 */
object RomanNumeralParser {
    /**
     * Pattern that validates a string as a correct Roman numeral
     */
    private val NUMERAL_VALIDATION_PATTERN: Pattern =
        Pattern.compile("^(?=[MDCLXVI])M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$")

    /**
     * Pattern that finds words that begin with a Roman numeral
     */
    private val NUMERAL_FINDING_PATTERN: Pattern =
        Pattern.compile(" (?=[MDCLXVI])(M*(?:C[MD]|D?C{0,3})(?:X[CL]|L?X{0,3})(?:I[XV]|V?I{0,3}))(.?)")

    /**
     * Map that contains mappings for decimal-to-roman conversion
     */
    private val INT_ROMAN_MAP = TreeMap<Int, String>()

    init {
        INT_ROMAN_MAP[1000] = "M"
        INT_ROMAN_MAP[900] = "CM"
        INT_ROMAN_MAP[500] = "D"
        INT_ROMAN_MAP[400] = "CD"
        INT_ROMAN_MAP[100] = "C"
        INT_ROMAN_MAP[90] = "XC"
        INT_ROMAN_MAP[50] = "L"
        INT_ROMAN_MAP[40] = "XL"
        INT_ROMAN_MAP[10] = "X"
        INT_ROMAN_MAP[9] = "IX"
        INT_ROMAN_MAP[5] = "V"
        INT_ROMAN_MAP[4] = "IV"
        INT_ROMAN_MAP[1] = "I"
    }

    fun integerToRoman(number: Int): String? {
        val l = INT_ROMAN_MAP.floorKey(number)
        if (number == l) {
            return INT_ROMAN_MAP[number]
        }
        return INT_ROMAN_MAP[l] + integerToRoman(number - l)
    }

    /**
     * Replaces all occurrences of Roman numerals in an input string with their integer values.
     * For example: VI -> 6, X -> 10, etc
     *
     * @param input Input string to replace numerals in
     * @return The input string with all numerals replaced by integers
     */
    fun replaceNumeralsWithIntegers(input: String): String {
        val result = StringBuffer()
        val matcher = NUMERAL_FINDING_PATTERN.matcher(input)

        // The matcher finds all words after a space that begin with a Roman numeral.
        while (matcher.find()) {
            val wordPartMatcher = Pattern.compile("^[\\w-']").matcher(matcher.group(2))

            // Ignore this match if it is a capital letter that is part of a word or if the first capture group matches an empty String.
            if (wordPartMatcher.matches() || matcher.group(1) == "") {
                continue
            }

            var parsedInteger = parseNumeral(matcher.group(1))

            // Don't replace the word "I".
            if (parsedInteger != 1 || matcher.group(2) == "§" || matcher.group(2) == "") {
                matcher.appendReplacement(result, " $parsedInteger$2")
            }
        }
        matcher.appendTail(result)

        return result.toString()
    }

    /**
     * Tests whether an input string is a valid Roman numeral.
     * To be valid the numerals must be either `I, V, X, L, C, D, M` and in upper case
     * and in correct format (meaning `IIII` is invalid as it should be `IV`)
     *
     * @param romanNumeral String to test
     * @return Whether that string represents a valid Roman numeral
     */
    fun isNumeralValid(romanNumeral: String): Boolean {
        return NUMERAL_VALIDATION_PATTERN.matcher(romanNumeral).matches()
    }

    /**
     * Parses a valid Roman numeral string to its integer value.
     * Use [.isNumeralValid] to check.
     *
     * @param numeralString Numeral to parse
     * @return Parsed value
     * @throws IllegalArgumentException If the input is malformed
     */
    fun parseNumeral(numeralString: String): Int {
        // Make sure this is a valid Roman numeral before trying to parse it.
        require(isNumeralValid(numeralString)) { "\"$numeralString\" is not a valid Roman numeral." }

        var value = 0 // parsed value
        val charArray = numeralString.toCharArray()
        var i = 0
        while (i < charArray.size) {
            val c = charArray[i]
            val numeral = Numeral.getFromChar(c)
            if (i + 1 < charArray.size) {
                // check next numeral to correctly evaluate IV, IX and so forth
                val nextNumeral = Numeral.getFromChar(charArray[i + 1])
                val diff = nextNumeral.value - numeral.value
                if (diff > 0) {
                    // if the next numeral is of higher value, it means their difference should be added instead
                    value += diff
                    i++ // skip next char
                    i++
                    continue
                }
            }
            value += numeral.value
            i++
        }
        return value
    }

    private enum class Numeral(val value: Int) {
        I(1),
        V(5),
        X(10),
        L(50),
        C(100),
        D(500),
        M(1000);

        companion object {
            fun getFromChar(c: Char): Numeral {
                try {
                    return valueOf(c.toString())
                } catch (ex: IllegalArgumentException) {
                    throw IllegalArgumentException("Expected valid Roman numeral, received '$c'.")
                }
            }
        }
    }
}