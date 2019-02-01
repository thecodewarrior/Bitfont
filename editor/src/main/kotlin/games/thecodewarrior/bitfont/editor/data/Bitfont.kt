package games.thecodewarrior.bitfont.editor.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonWritable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgPackable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgUnpackable
import glm_.func.common.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

class Bitfont(name: String, ascent: Int, descent: Int, capHeight: Int, xHeight: Int, spacing: Int): JsonWritable<JsonObject>, MsgPackable {
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
                *glyphs.int2ObjectEntrySet().filter {
                    !it.value.isEmpty()
                }.sortedBy { it.intKey }.map {
                    "${it.intKey}" to it.value.writeJson()
                }.toTypedArray()
            )
        )
    }

    override fun pack(packer: MessagePacker) {
        packer.apply {
            packString(name)
            packInt(ascent)
            packInt(descent)
            packInt(capHeight)
            packInt(xHeight)
            packInt(spacing)
            packMapHeader(glyphs.size)
            glyphs.int2ObjectEntrySet()
                .filter { !it.value.isEmpty() }
                .sortedBy { it.intKey }
                .forEach { (point, glyph) ->
                packInt(point)
                glyph.pack(packer)
            }
        }
    }

    companion object: JsonReadable<JsonObject, Bitfont>, MsgUnpackable<Bitfont> {
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

        override fun unpack(unpacker: MessageUnpacker): Bitfont {
            unpacker.apply {
                val name = unpackString()
                val ascent = unpackInt()
                val descent = unpackInt()
                val capHeight = unpackInt()
                val xHeight = unpackInt()
                val spacing = unpackInt()
                val font = Bitfont(name, ascent, descent, capHeight, xHeight, spacing)

                for(i in 0 until unpackMapHeader()) {
                    font.glyphs[unpackInt()] = Glyph.unpack(unpacker)
                }
                return font
            }
        }
    }
}