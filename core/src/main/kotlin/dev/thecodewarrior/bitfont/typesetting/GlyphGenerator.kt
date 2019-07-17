package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.Attribute

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
class GlyphGenerator(string: AttributedString, val fallbackFonts: List<Bitfont>): AbstractGlyphGenerator(string) {
    init {
        if(fallbackFonts.isEmpty())
            throw IllegalArgumentException("There must be at least one fallback font")
    }

    override fun refillBuffer() {
        if(isEnd) return

        val font = attributeValue(Attribute.font)
            ?.takeIf { codepoint in it.glyphs }
            ?: fallbackFonts.find { codepoint in it.glyphs }
            ?: attributeValue(Attribute.font)
            ?: fallbackFonts.first()
        val glyph = font.glyphs[codepoint] ?: font.defaultGlyph
        push(AttributedGlyph(codepoint, glyph, string, index, offset))
        advance()
    }
}