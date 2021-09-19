package dev.thecodewarrior.bitfont.data.file

import dev.thecodewarrior.bitfont.data.BitGrid
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.data.Glyph
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

public object BitfontFormatV1: BitfontFileFormat() {
    override val readOnly: Boolean = false

    override fun packTables(font: Bitfont, file: BitfontFile) {
        file.createTable("info") {
            packString(font.name)
            packInt(font.ascent)
            packInt(font.descent)
            packInt(font.capHeight)
            packInt(font.xHeight)
            packInt(font.spacing)
        }
        file.createTable("glyphs") {
            GlyphsTableFormat.pack(this, font)
        }
    }

    override fun unpackTables(file: BitfontFile): Bitfont {
        val font = file.useTable("info") {
             Bitfont(
                unpackString(),
                unpackInt(),
                unpackInt(),
                unpackInt(),
                unpackInt(),
                unpackInt()
            )
        }

        file.useTable("glyphs") {
            GlyphsTableFormat.unpack(this, font)
        }

        return font
    }

    private object GlyphsTableFormat: MutableFormat<Bitfont> {
        override fun pack(packer: MessagePacker, value: Bitfont) {
            packer.pack {
                val glyphEntries = value.glyphs.int2ObjectEntrySet()
                    .sortedBy { it.intKey }
                packMapHeader(glyphEntries.size)
                glyphEntries.forEach { (point, glyph) ->
                    packInt(point)
                    GlyphFormat.pack(packer, glyph, value)
                }
            }
        }

        override fun unpack(unpacker: MessageUnpacker, value: Bitfont) {
            unpacker.unpack {
                val glyphCount = unpackMapHeader()
                for (i in 0 until glyphCount) {
                    val codepoint = unpackInt()
                    val glyph = GlyphFormat.unpack(unpacker, value)
                    value.glyphs[codepoint] = glyph
                }
            }
        }
    }

    private object GlyphFormat: ContextAwareImmutableFormat<Glyph, Bitfont> {
        override fun pack(packer: MessagePacker, value: Glyph, context: Bitfont) {
            value.crop()
            packer.pack {
                packInt(value.bearingX)
                packInt(value.bearingY)
                packInt(value.advance)
                BitGridFormat.pack(packer, value.image)
            }
        }

        override fun unpack(unpacker: MessageUnpacker, context: Bitfont): Glyph {
            unpacker.apply {
                val glyph = Glyph(context)
                glyph.bearingX = unpackInt()
                glyph.bearingY = unpackInt()
                val advance = if (tryUnpackNil()) null else unpackInt()
                glyph.image = BitGridFormat.unpack(unpacker)
                glyph.advance = advance ?: if (glyph.image.isEmpty())
                    0
                else
                    glyph.bearingX + glyph.image.width + context.spacing
                glyph.crop()
                return glyph
            }
        }
    }

    private object BitGridFormat: ImmutableFormat<BitGrid> {
        override fun pack(packer: MessagePacker, value: BitGrid) {
            packer.pack {
                packInt(value.width)
                packInt(value.height)
                writePayload(value.data.toByteArray())
            }
        }

        override fun unpack(unpacker: MessageUnpacker): BitGrid {
            return unpacker.unpack {
                val width = unpackInt()
                val height = unpackInt()
                val grid = BitGrid(width, height)
                readPayload(grid.data.size).toUByteArray().copyInto(grid.data)
                grid
            }
        }
    }
}