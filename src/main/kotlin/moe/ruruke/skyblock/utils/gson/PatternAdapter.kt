package moe.ruruke.skyblock.utils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.util.regex.Pattern

class PatternAdapter : TypeAdapter<Pattern?>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Pattern?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(value.pattern())
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Pattern? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val patternString = `in`.nextString()
        return Pattern.compile(patternString)
    }
}
