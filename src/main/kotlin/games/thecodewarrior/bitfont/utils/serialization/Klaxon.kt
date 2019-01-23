package games.thecodewarrior.bitfont.utils.serialization

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import games.thecodewarrior.bitfont.data.BitGrid

fun applyKlaxonExtensions(klaxon: Klaxon): Klaxon {
    return klaxon
        .converter(converter<BitGrid>(
            { jv ->
                val grid = BitGrid(jv.objInt("width"), jv.objInt("height"))
                val dataStr = jv.objString("data")
                dataStr.chunkedSequence(2).map { it.toUByte(16) }.forEachIndexed { i, v -> grid.data[i] = v }
                grid
            },
            { value ->
                """{"width": ${value.width}, "height": ${value.height}, "data": "${value.data.joinToString("") { it.toString(16) }}"}"""
            }
        ))
}

inline fun <reified T> converter(
    crossinline fromJson: (jv: JsonValue) -> T?,
    crossinline toJson: (value: T) -> String
): Converter {
    return object: Converter {
        override fun canConvert(cls: Class<*>): Boolean {
            return cls == T::class.java
        }

        override fun fromJson(jv: JsonValue): Any? {
            return fromJson(jv)
        }

        override fun toJson(value: Any): String {
            return toJson(value as T)
        }
    }
}
