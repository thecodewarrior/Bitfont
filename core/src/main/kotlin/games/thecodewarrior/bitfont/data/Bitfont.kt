package games.thecodewarrior.bitfont.data

import games.thecodewarrior.bitfont.utils.clamp
import games.thecodewarrior.bitfont.utils.serialization.MsgPackable
import games.thecodewarrior.bitfont.utils.serialization.MsgUnpackable
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.msgpack.core.MessageInsufficientBufferException
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import java.util.Collections

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
            generatedDefaultGlyph = createDefaultGlyph()
        }

    var xHeight: Int = xHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            generatedDefaultGlyph = createDefaultGlyph()
        }

    var spacing: Int = spacing.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            generatedDefaultGlyph = createDefaultGlyph()
        }

    val ligatures = Object2IntOpenHashMap<String>()

    val glyphs = Int2ObjectOpenHashMap<Glyph>()
    val reservedSpecialGlyphs: Set<Int>
        get() = listOf(
            listOf(-1),
            ligatures.values
        ).flatten().toSet()

    private var generatedDefaultGlyph = createDefaultGlyph()
    val defaultGlyph: Glyph
        get() {
            val glyph = glyphs[-1]
            if(glyph == null || glyph.isEmpty())
                return generatedDefaultGlyph
            return glyph
        }

    fun nextSpecialGlyph(): Int {
        val reserved = reservedSpecialGlyphs
        var i = -1
        while (i in reserved) {
            i--
        }
        return i
    }

    private fun createDefaultGlyph(): Glyph {
        val capHeight = if(capHeight == 0) 1 else capHeight
        val xHeight = if(xHeight == 0) 1 else xHeight
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
            writePayload(magicBytes)
            packInt(version)

            val tables = packTables()
            packArrayHeader(tables.size)
            tables.forEach { name, data ->
                packString(name)
                packInt(data.size)
                writePayload(data)
            }
        }
    }

    private fun packTables(): Map<String, ByteArray> {
        val map = mutableMapOf<String, ByteArray>()

        val specials = reservedSpecialGlyphs - setOf(-1) // an empty -1 should be removed so it can be generated
        glyphs.int2ObjectEntrySet().toList().forEach {
            if (it.value.isEmpty() && it.intKey !in specials)
                glyphs.remove(it.intKey)
        }

        map["info"] = MsgPackable.packToBytes { packer ->
            packer.packString(name)
            packer.packInt(ascent)
            packer.packInt(descent)
            packer.packInt(capHeight)
            packer.packInt(xHeight)
            packer.packInt(spacing)
        }

        map["glyphs"] = MsgPackable.packToBytes { packer ->
            val glyphEntries = glyphs.int2ObjectEntrySet()
                .sortedBy { it.intKey }
            packer.packMapHeader(glyphEntries.size)
            glyphEntries.forEach { (point, glyph) ->
                packer.packInt(point)
                glyph.pack(packer)
            }
        }

        map["ligatures"] = MsgPackable.packToBytes { packer ->
            val ligEntries = ligatures.object2IntEntrySet()
                .sortedBy { it.key }
            packer.packMapHeader(ligEntries.size)
            ligEntries.forEach { (chars, glyph) ->
                packer.packString(chars)
                packer.packInt(glyph)
            }
        }

        return map
    }

    companion object: MsgUnpackable<Bitfont> {
        val version = 1
        val magic = "BITFONT"
        val magicBytes = magic.toByteArray()

        override fun unpack(unpacker: MessageUnpacker): Bitfont {
            val fileMagic = (try {
                unpacker.readPayload(magicBytes.size)
            } catch(e: MessageInsufficientBufferException) {
                ByteArray(0)
            } catch(e: Exception) {
                throw e
            })!!

            if(!fileMagic.contentEquals(magicBytes))
                throw IllegalArgumentException("Passed data is not a bitfont file. Missing magic constant `$magic` " +
                    "at the start of the file.")

            val fileVersion = unpacker.unpackInt()

            val tables = mutableMapOf<String, ByteArray>()
            val tableCount = unpacker.unpackArrayHeader()
            repeat(tableCount) {
                tables[unpacker.unpackString()] = unpacker.readPayload(unpacker.unpackInt())
            }

            val font = Bitfont("<none>", 0, 0, 0, 0, 0)

            unpackTables(font, tables)

            return font
        }

        private fun unpackTables(font: Bitfont, map: Map<String, ByteArray>) {
            map["info"]?.also { data ->
                MsgUnpackable.unpack(data) { unpacker ->
                    font.name = unpacker.unpackString()
                    font.ascent = unpacker.unpackInt()
                    font.descent = unpacker.unpackInt()
                    font.capHeight = unpacker.unpackInt()
                    font.xHeight = unpacker.unpackInt()
                    font.spacing = unpacker.unpackInt()
                }
            }

            map["glyphs"]?.also { data ->
                MsgUnpackable.unpack(data) { unpacker ->
                    val glyphCount = unpacker.unpackMapHeader()
                    try {
                        for (i in 0 until glyphCount) {
                            font.glyphs[unpacker.unpackInt()] = Glyph.unpack(unpacker)
                        }
                    } catch(e: MessageInsufficientBufferException) {
                        e.printStackTrace()
                        font.name = font.name + "~"
                    }
                }
            }

            map["ligatures"]?.also { data ->
                MsgUnpackable.unpack(data) { unpacker ->
                    val ligCount = unpacker.unpackMapHeader()
                    for (i in 0 until ligCount) {
                        font.ligatures[unpacker.unpackString()] = unpacker.unpackInt()
                    }
                }
            }
        }
    }
}