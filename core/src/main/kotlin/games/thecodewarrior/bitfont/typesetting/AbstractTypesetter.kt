package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.BufferedIterator

class Typesetter(val glyphs: Iterator<Glyph>): BufferedIterator<TypesetGlyph>() {
    var cursorX: Int = 0
    var cursorY: Int = 0

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return
        val glyph = glyphs.next()
        push(TypesetGlyph(cursorX, cursorY, glyph))
        cursorX += glyph.calcAdvance()
    }
}

data class TypesetGlyph(val posX: Int, val posY: Int, val glyph: Glyph) {

}
