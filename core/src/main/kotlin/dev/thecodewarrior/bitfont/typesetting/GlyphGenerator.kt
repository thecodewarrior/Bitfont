package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.BufferedIterator

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
public open class GlyphGenerator(public val string: AttributedString, public val font: Bitfont): BufferedIterator<GlyphGenerator.Glyph>() {
    public class Glyph(
        public val index: Int,
        public val codepoint: Int,
        public val textObject: TextObject,
        public val source: AttributedString,
    ) {
        public val afterIndex: Int = index + Character.charCount(codepoint)
    }

    override fun refillBuffer() {
        if(isEnd) return

        val textObject: TextObject = attributeValue(TextAttribute.textEmbed)
            ?: findFont().let { it.glyphs[codepoint] ?: it.defaultGlyph }
        push(Glyph(index, codepoint, textObject, string))
        advance()
    }

    private fun findFont(): Bitfont {
        return attributeValue(TextAttribute.font)
            ?.takeIf { codepoint in it.glyphs }
            ?: font.takeIf { codepoint in it.glyphs }
            ?: attributeValue(TextAttribute.font)
            ?: font
    }

    /**
     * True if the entire string has been consumed
     */
    protected val isEnd: Boolean
        get() = string.isEmpty() || index >= string.plaintext.length

    /**
     * The offset of the current codepoint within the string
     */
    protected var index: Int = 0
        private set

    /**
     * The current codepoint
     */
    protected val codepoint: Int
        get() = if(isEnd)
            throw NoSuchElementException()
        else
            string.plaintext.codePointAt(index)

    protected fun <T> attributeValue(attribute: TextAttribute<T>): T? = string[attribute, index]

    /**
     * Returns the current codepoint and advances to the next one
     */
    protected fun advance(): Int {
        if(isEnd)
            throw NoSuchElementException()
        val old = codepoint
        index += Character.charCount(old)
        return old
    }

    override fun refillBufferIfNeeded() {
        val oldOffset = index
        val oldSize = size
        super.refillBufferIfNeeded()
        if(index == oldOffset && oldSize != size) {
            throw IllegalStateException("Glyph generator stalled at index $index. " +
                "It created glyphs without advancing.")
        }
    }
}