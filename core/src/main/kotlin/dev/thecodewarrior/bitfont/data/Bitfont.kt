package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.data.file.BitfontFile
import dev.thecodewarrior.bitfont.data.file.BitfontFileFormat
import dev.thecodewarrior.bitfont.utils.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap
import java.io.InputStream

public class Bitfont(name: String, ascent: Int, descent: Int, capHeight: Int, xHeight: Int, spacing: Int) {
    public var name: String = name

    public var ascent: Int = ascent.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    public var descent: Int = descent.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
        }

    public var capHeight: Int = capHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    public var xHeight: Int = xHeight.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    @Deprecated("This is dumb")
    public var spacing: Int = spacing.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    public val glyphs: Int2ObjectSortedMap<Glyph> = Int2ObjectRBTreeMap()
    public var defaultGlyph: Glyph = createDefaultGlyph()
        private set

    private fun createDefaultGlyph(): Glyph {
        val height = if (capHeight == 0) 1 else capHeight
        val width = if (xHeight == 0) 1 else xHeight
        val glyph = Glyph(this)
        glyph.bearingX = 0
        glyph.bearingY = -height
        val grid = BitGrid(width, height)
        for (x in 0 until width) {
            grid[x, 0] = true
            grid[x, height - 1] = true
        }
        for (y in 0 until height) {
            grid[0, y] = true
            grid[width - 1, y] = true
        }
        glyph.image = grid
        glyph.advance = width + 1
        return glyph
    }

    public companion object {
        @JvmStatic
        public fun unpack(stream: InputStream): Bitfont {
            val file = BitfontFile.unpack(stream)
            return BitfontFileFormat.unpack(file)
        }
    }
}