package games.thecodewarrior.bitfont.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.utils.serialization.JsonWritable
import glm_.func.common.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class Bitfont(name: String, lineHeight: Int, ascender: Int, descender: Int, capHeight: Int, xHeight: Int): JsonWritable<JsonObject> {
    var name: String = name

    var lineHeight: Int = lineHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var ascender: Int = ascender.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var descender: Int = descender.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var capHeight: Int = capHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var xHeight: Int = xHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    val glyphs = Int2ObjectOpenHashMap<Glyph>()

    override fun writeJson(): JsonObject = json {
        obj(
            "name" to name,
            "lineHeight" to lineHeight,
            "ascender" to ascender,
            "descender" to descender,
            "capHeight" to capHeight,
            "xHeight" to xHeight,
            "glyphs" to obj(
                *glyphs.filter {
                    !it.value.isEmpty()
                }.map {
                    "${it.key}" to it.value.writeJson()
                }.toTypedArray()
            )
        )
    }

    companion object: JsonReadable<JsonObject, Bitfont> {
        override fun readJson(j: JsonObject): Bitfont {
            val font = Bitfont(
                j.string("name")!!,
                j.int("lineHeight")!!,
                j.int("ascender")!!,
                j.int("descender")!!,
                j.int("capHeight")!!,
                j.int("xHeight")!!
            )
            j.obj("glyphs")!!.forEach { key, value ->
                font.glyphs[key.toInt()] = Glyph.readJson(value as JsonObject)
            }
            return font
        }
    }
}