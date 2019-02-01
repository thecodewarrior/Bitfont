package games.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import com.ibm.icu.lang.UCharacterCategory
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.Glyph
import glm_.vec2.Vec2i
import kotlin.streams.asSequence

class Typesetter(val font: Bitfont) {

    fun typeset(text: String, options: TypesettingOptions = TypesettingOptions()): TextLayout {
        val layout = TextLayout()
        val newlines = intArrayOf(0x000a, 0x000b, 0x000c, 0x000d, 0x00085, 0x2028, 0x2029)

        @Suppress("UNCHECKED_CAST")
        val glyphs = text.codePoints().asSequence()
            .mapIndexed { i, codepoint -> Triple(i, codepoint, (font.glyphs[codepoint] ?: font.glyphs[0])) }
            .toMutableList() as MutableList<Triple<Int, Int, Glyph?>>

        var cursor = Vec2i(0, 0)
        var lastGlyph: Glyph? = null
        for(glyphI in glyphs.indices) {
            val (i, codepoint, glyph) = glyphs[glyphI]
//            if(lastGlyph != null && options.applyKerning) {
//                cursor = Vec2i(cursor.x + font.kerning(lastGlyph, glyph), cursor.y)
//            }
            val glyphPos = if(glyph != null) Vec2i(
                cursor.x + glyph.bearingX,
                cursor.y + glyph.bearingY
            ) else Vec2i(cursor)
            layout.characters.add(TypesetCharacter(i, codepoint, Vec2i(cursor), glyphPos, glyph))

            if(codepoint in newlines) {
                val isCrBeforeLf = codepoint == 0x000D && glyphs.getOrNull(glyphI+1)?.second == 0x000A
                if(!isCrBeforeLf) {
                    cursor = Vec2i(0, cursor.y + font.ascent + font.descent)
                }
            } else if(glyph != null){
                cursor = Vec2i(cursor.x + glyph.calcAdvance(font.spacing), cursor.y)
            }
            lastGlyph = glyph
        }
        layout.endPos = cursor
        return layout
    }
}

data class TypesettingOptions(
    var applyKerning: Boolean = true
)

class TextLayout {
    val characters = mutableListOf<TypesetCharacter>()
    var endPos = Vec2i()
}

data class TypesetCharacter(val index: Int, val codepoint: Int, val pos: Vec2i, val glyphPos: Vec2i, val glyph: Glyph?)
