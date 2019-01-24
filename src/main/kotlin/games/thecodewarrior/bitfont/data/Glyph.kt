package games.thecodewarrior.bitfont.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.utils.serialization.JsonWritable
import glm_.func.common.clamp
import glm_.vec2.Vec2i
import kotlin.math.max

class Glyph(val codepoint: Int): JsonWritable<JsonObject> {
    var bearingX: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var bearingY: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var advance: Int? = null
        set(value) {
            field = value?.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }

    fun calcAdvance(spacing: Int): Int = advance ?: if (image.isEmpty()) 0 else max(0, bearingX) + image.width + spacing

    var image: BitGrid = BitGrid(1, 1)

    fun isEmpty(): Boolean = image.isEmpty() && advance == null

    override fun writeJson(): JsonObject = json {
        obj(
            "codepoint" to codepoint,
            "bearing" to array(bearingX, bearingY),
            "advance" to advance,
            "image" to image.writeJson()
        )
    }

    companion object: JsonReadable<JsonObject, Glyph> {
        override fun readJson(j: JsonObject): Glyph {
            val glyph = Glyph(j.int("codepoint")!!)
            j.array<Int>("bearing")!!.also {
                glyph.bearingX = it[0]
                glyph.bearingY = it[1]
            }
            glyph.advance = j.int("advance")
            glyph.image = BitGrid.readJson(j.obj("image")!!)
            return glyph
        }
    }
}