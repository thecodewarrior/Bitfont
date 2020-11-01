package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
public class GlyphGenerator(string: AttributedString, public val font: Bitfont): AbstractGlyphGenerator(string) {
    override fun refillBuffer() {
        if(isEnd) return

        val textObject: TextObject = attributeValue(TextAttribute.textEmbed)
            ?: findFont().let { it.glyphs[codepoint] ?: it.defaultGlyph }
        push(TypesetGlyph(0, 0, codepoint, textObject, string, index, offset))
        advance()
    }

    private fun findFont(): Bitfont {
        return attributeValue(TextAttribute.font)
            ?.takeIf { codepoint in it.glyphs }
            ?: font.takeIf { codepoint in it.glyphs }
            ?: attributeValue(TextAttribute.font)
            ?: font
    }
}