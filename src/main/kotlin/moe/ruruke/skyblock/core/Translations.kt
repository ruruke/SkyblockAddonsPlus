package moe.ruruke.skyblock.core

import moe.ruruke.skyblock.SkyblockAddonsPlus
import net.minecraft.client.Minecraft


object Translations {
    private val VARIABLE_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("%[A-Za-z-]+%")

    fun getMessage(path: String, vararg variables: Any?): String? {
        var text: String = ""
        try {
            val main: SkyblockAddonsPlus = SkyblockAddonsPlus.instance!!

            // Get the string.
            val pathSplit: Array<String> =
                path.split(java.util.regex.Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var jsonObject: com.google.gson.JsonObject = SkyblockAddonsPlus.configValues!!.getLanguageConfig()
            for (pathPart: String in pathSplit) {
                if (pathPart != "") {
                    val jsonElement: com.google.gson.JsonElement = jsonObject.get(pathPart)

                    if (jsonElement.isJsonObject()) {
                        jsonObject = jsonObject.getAsJsonObject(pathPart)
                    } else {
                        text = jsonObject.get(path.substring(path.lastIndexOf(pathPart))).getAsString()
                        break
                    }
                }
            }

            // Iterate through the string and replace any variables.
            var matcher: java.util.regex.Matcher = VARIABLE_PATTERN.matcher(text)
            val variablesDeque: java.util.Deque<Any> = java.util.ArrayDeque(java.util.Arrays.asList(*variables))

            while (matcher.find() && !variablesDeque.isEmpty()) {
                // Replace a variable and re-make the matcher.
                text = matcher.replaceFirst(
                    java.util.regex.Matcher.quoteReplacement(
                        variablesDeque.pollFirst().toString()
                    )
                )
                matcher = VARIABLE_PATTERN.matcher(text)
            }


            // Handle RTL text...
            if ((SkyblockAddonsPlus.configValues!!.getLanguage() == Language.HEBREW || SkyblockAddonsPlus.configValues!!.getLanguage() == Language.ARABIC) &&
                !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                text = bidiReorder(text);
            }
        } catch (ex: java.lang.Exception) {
            text = path // In case of fire...
        }
        return text
    }

    private fun bidiReorder(text: String): String {
        return text;
//        try {
//            val bidi: com.ibm.icu.text.Bidi = com.ibm.icu.text.Bidi(
//                (ArabicShaping(ArabicShaping.LETTERS_SHAPE)).shape(text),
//                com.ibm.icu.text.Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT
//            )
//            bidi.setReorderingMode(com.ibm.icu.text.Bidi.REORDER_DEFAULT.toInt())
//            return bidi.writeReordered(com.ibm.icu.text.Bidi.DO_MIRRORING.toInt())
//        } catch (ex: ArabicShapingException) {
//            return text
//        }
    }
}
