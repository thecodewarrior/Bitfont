package games.thecodewarrior.bitfont.data

import glm_.func.common.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class BitFont(name: String, lineHeight: Int, ascender: Int, descender: Int, capHeight: Int, xHeight: Int) {
    var name: String = name

    var lineHeight: Int = lineHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    var ascender: Int = ascender
        set(value) {
            field = value.clamp(0, 65535)
        }

    var descender: Int = descender
        set(value) {
            field = value.clamp(0, 65535)
        }

    var capHeight: Int = capHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    var xHeight: Int = xHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    val glyphs = Int2ObjectOpenHashMap<Glyph>()
}