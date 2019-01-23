package games.thecodewarrior.bitfont.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.utils.serialization.JsonWritable

class Glyph(val codepoint: Int): JsonWritable<JsonObject> {
    var bearingX: Int = 0
    var bearingY: Int = 0
    var image: BitGrid = BitGrid(1, 1)

    override fun writeJson(): JsonObject = json {
        obj(
            "codepoint" to codepoint,
            "bearing" to array(bearingX, bearingY),
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
            glyph.image = BitGrid.readJson(j.obj("image")!!)
            return glyph
        }
    }
}