package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.Attribute

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
class GlyphGenerator(string: dev.thecodewarrior.bitfont.typesetting.AttributedString, val fallbackFonts: List<dev.thecodewarrior.bitfont.data.Bitfont>): dev.thecodewarrior.bitfont.typesetting.AbstractGlyphGenerator(string) {
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
        push(dev.thecodewarrior.bitfont.typesetting.AttributedGlyph(codepoint, glyph, string, index, offset))
        advance()
    }
}