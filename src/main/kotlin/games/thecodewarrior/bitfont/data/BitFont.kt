package games.thecodewarrior.bitfont.data

import com.beust.klaxon.Json
import glm_.func.common.clamp
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class BitFont(name: String, lineHeight: Int, ascender: Int, descender: Int, capHeight: Int, xHeight: Int) {
    @Json
    var name: String = name

    @Json
    var lineHeight: Int = lineHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    @Json
    var ascender: Int = ascender
        set(value) {
            field = value.clamp(0, 65535)
        }

    @Json
    var descender: Int = descender
        set(value) {
            field = value.clamp(0, 65535)
        }

    @Json
    var capHeight: Int = capHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    @Json
    var xHeight: Int = xHeight
        set(value) {
            field = value.clamp(0, 65535)
        }

    @Json(ignored = true)
    val glyphs = Int2ObjectOpenHashMap<Glyph>()

    @Json(name = "glyphs")
    var klaxonGlyphs: Map<String, Glyph>
        get() = glyphs.mapKeys { "${it.key}" }
        set(value) {
            glyphs.clear()
            glyphs.putAll(value.mapKeys { it.key.toInt() })
        }
}