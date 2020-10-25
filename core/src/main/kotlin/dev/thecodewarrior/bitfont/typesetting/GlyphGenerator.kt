package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.Attribute

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
public class GlyphGenerator(string: AttributedString, public val font: Bitfont): AbstractGlyphGenerator(string) {
    override fun refillBuffer() {
        if(isEnd) return

        val font = attributeValue(Attribute.font)
            ?.takeIf { codepoint in it.glyphs }
            ?: font.takeIf { codepoint in it.glyphs }
            ?: attributeValue(Attribute.font)
            ?: font
        val glyph = font.glyphs[codepoint] ?: font.defaultGlyph
        push(AttributedGlyph(codepoint, glyph, string, index, offset))
        advance()
    }
}