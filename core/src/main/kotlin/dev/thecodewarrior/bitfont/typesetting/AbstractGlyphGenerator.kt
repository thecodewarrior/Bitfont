package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.Attribute
import dev.thecodewarrior.bitfont.utils.BufferedIterator

/**
 * Generates a sequence of glyphs for the given attributed string. Behavior is undefined if the string is mutated
 * during iteration.
 */
abstract class AbstractGlyphGenerator(val string: dev.thecodewarrior.bitfont.typesetting.AttributedString): BufferedIterator<dev.thecodewarrior.bitfont.typesetting.AttributedGlyph>() {
    /**
     * True if the entire string has been consumed
     */
    protected var isEnd: Boolean = string.isEmpty()
        private set

    /**
     * The index of the current codepoint
     */
    protected var index: Int = 0
        private set

    /**
     * The offset of the current codepoint within the string
     */
    protected var offset = 0
        private set

    /**
     * The current codepoint
     */
    protected val codepoint: Int
        get() = if(isEnd)
            throw NoSuchElementException()
        else
            string.plaintext.codePointAt(offset)

    protected fun <T> attributeValue(attribute: Attribute<T>): T? = string[attribute, offset]

    /**
     * Returns the current codepoint and advances to the next one
     */
    protected fun advance(): Int {
        if(isEnd)
            throw NoSuchElementException()
        val old = codepoint
        offset += Character.charCount(old)
        index++
        isEnd = index == string.plaintext.length
        return old
    }

    override fun refillBufferIfNeeded() {
        val oldOffset = offset
        val oldSize = size
        super.refillBufferIfNeeded()
        if(offset == oldOffset && oldSize != size) {
            throw IllegalStateException("Glyph generator stalled at index $offset. " +
                "It created glyphs without advancing.")
        }
    }
}