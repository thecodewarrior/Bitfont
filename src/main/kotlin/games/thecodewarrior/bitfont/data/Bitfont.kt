package games.thecodewarrior.bitfont.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.utils.serialization.JsonWritable
import glm_.func.common.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class Bitfont(name: String, ascent: Int, descent: Int, capHeight: Int, xHeight: Int, spacing: Int): JsonWritable<JsonObject> {
    var name: String = name

    var ascent: Int = ascent.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var descent: Int = descent.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    var capHeight: Int = capHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    var xHeight: Int = xHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    var spacing: Int = spacing.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    val glyphs = Int2ObjectOpenHashMap<Glyph>()
    var defaultGlyph: Glyph = createDefaultGlyph()
        private set

    private fun createDefaultGlyph(): Glyph {
        val glyph = Glyph()
        glyph.bearingX = 0
        glyph.bearingY = -capHeight
        val grid = BitGrid(xHeight, capHeight)
        for(x in 0 until xHeight) {
            grid[x, 0] = true
            grid[x, capHeight-1] = true
        }
        for(y in 0 until capHeight) {
            grid[0, y] = true
            grid[xHeight-1, y] = true
        }
        glyph.image = grid
        return glyph
    }

    override fun writeJson(): JsonObject = json {
        obj(
            "name" to name,
            "ascent" to ascent,
            "descent" to descent,
            "capHeight" to capHeight,
            "xHeight" to xHeight,
            "spacing" to spacing,
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
                j.int("ascent")!!,
                j.int("descent")!!,
                j.int("capHeight")!!,
                j.int("xHeight")!!,
                j.int("spacing")!!
            )
            j.obj("glyphs")!!.forEach { key, value ->
                font.glyphs[key.toInt()] = Glyph.readJson(value as JsonObject)
            }
            return font
        }
    }
}