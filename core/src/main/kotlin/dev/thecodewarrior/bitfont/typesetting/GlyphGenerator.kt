package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
public class GlyphGenerator(string: AttributedString, public val font: Bitfont): AbstractGlyphGenerator(string) {
    override fun refillBuffer() {
        if(isEnd) return

        val font = attributeValue(TextAttribute.font)
            ?.takeIf { codepoint in it.glyphs }
            ?: font.takeIf { codepoint in it.glyphs }
            ?: attributeValue(TextAttribute.font)
            ?: font
        val textObject: TextObject = font.glyphs[codepoint] ?: font.defaultGlyph
        push(TypesetGlyph(0, 0, codepoint, textObject, string, index, offset))
        advance()
    }
}