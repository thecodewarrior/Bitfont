package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.utils.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class Bitfont(name: String, ascent: Int, descent: Int, capHeight: Int, xHeight: Int, spacing: Int) {
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

    @Deprecated("This is dumb. Just encode the advance, dummy. (I am the dummy)")
    var spacing: Int = spacing.clamp(0, 65535)
        set(value) {
            field = value.clamp(0, 65535)
            defaultGlyph = createDefaultGlyph()
        }

    val glyphs = Int2ObjectOpenHashMap<Glyph>()
    var defaultGlyph: Glyph = createDefaultGlyph()
        private set

    private fun createDefaultGlyph(): Glyph {
        val capHeight = if (capHeight == 0) 1 else capHeight
        val xHeight = if (xHeight == 0) 1 else xHeight
        val glyph = Glyph(this)
        glyph.bearingX = 0
        glyph.bearingY = -capHeight
        val grid = BitGrid(xHeight, capHeight)
        for (x in 0 until xHeight) {
            grid[x, 0] = true
            grid[x, capHeight - 1] = true
        }
        for (y in 0 until capHeight) {
            grid[0, y] = true
            grid[xHeight - 1, y] = true
        }
        glyph.image = grid
        return glyph
    }
}