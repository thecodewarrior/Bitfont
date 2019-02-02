package games.thecodewarrior.bitfont.data

import games.thecodewarrior.bitfont.utils.clamp
import games.thecodewarrior.bitfont.utils.serialization.MsgPackable
import games.thecodewarrior.bitfont.utils.serialization.MsgUnpackable
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.msgpack.core.MessageInsufficientBufferException
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

class Bitfont(name: String, ascent: Int, descent: Int, capHeight: Int, xHeight: Int, spacing: Int): MsgPackable {
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

    override fun pack(packer: MessagePacker) {
        packer.apply {
            packString(name)
            packInt(ascent)
            packInt(descent)
            packInt(capHeight)
            packInt(xHeight)
            packInt(spacing)
            val entries = glyphs.int2ObjectEntrySet()
                .filter { !it.value.isEmpty() }
                .sortedBy { it.intKey }
            packMapHeader(entries.size)
            entries.forEach { (point, glyph) ->
                packInt(point)
                glyph.pack(packer)
            }
        }
    }

    companion object: MsgUnpackable<Bitfont> {
        override fun unpack(unpacker: MessageUnpacker): Bitfont {
            unpacker.apply {
                val name = unpackString()
                val ascent = unpackInt()
                val descent = unpackInt()
                val capHeight = unpackInt()
                val xHeight = unpackInt()
                val spacing = unpackInt()
                val font = Bitfont(name, ascent, descent, capHeight, xHeight, spacing)

                val glyphCount = unpackMapHeader()
                try {
                    for (i in 0 until glyphCount) {
                        font.glyphs[unpackInt()] = Glyph.unpack(unpacker)
                    }
                } catch(e: MessageInsufficientBufferException) {
                    e.printStackTrace()
                    font.name = font.name + "~"
                }
                return font
            }
        }
    }
}