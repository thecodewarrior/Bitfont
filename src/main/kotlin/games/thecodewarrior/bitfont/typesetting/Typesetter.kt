package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.Glyph
import glm_.vec2.Vec2i
import kotlin.streams.asSequence

class Typesetter(val font: Bitfont) {

    fun typeset(text: String, options: TypesettingOptions = TypesettingOptions()): List<TextRun> {
        var run = TextRun()
        val runs = mutableListOf<TextRun>()

        @Suppress("UNCHECKED_CAST")
        val glyphs = text.codePoints().asSequence()
            .mapIndexed { i, codepoint -> i to font.glyphs[codepoint] }
            .filter { (_, glyph) -> glyph != null }
            .toMutableList() as MutableList<Pair<Int, Glyph>>

        var cursor = Vec2i(0, 0)
        var lastGlyph: Glyph? = null
        for((i, glyph) in glyphs) {
            if("A${String(Character.toChars(glyph.codepoint))}A".lines().size > 1) {
                runs.add(run)
                run = TextRun()
                cursor = Vec2i(0, cursor.y + font.lineHeight + 1)
            }
//            if(lastGlyph != null && options.applyKerning) {
//                cursor = Vec2i(cursor.x + font.kerning(lastGlyph, glyph), cursor.y)
//            }
            val glyphPos = Vec2i(
                cursor.x + glyph.bearingX,
                cursor.y + glyph.bearingY
            )
            val placed = PlacedGlyph(glyph, glyphPos, i)
            run.glyphs.add(placed)
            cursor = Vec2i(cursor.x + glyph.calcAdvance(font.spacing), cursor.y)
            lastGlyph = glyph
        }
        runs.add(run)
        return runs
    }
}

data class TypesettingOptions(
    var applyKerning: Boolean = true
)

class TextRun {
    val glyphs = mutableListOf<PlacedGlyph>()
}

data class PlacedGlyph(val glyph: Glyph, val pos: Vec2i, val index: Int)
