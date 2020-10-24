package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.PushBackIterator
import dev.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.math.max

public open class TextLayoutManager(public val fallbackFonts: List<Bitfont>) {
    //TODO: multiple text containers
    public val textContainers: MutableList<TextContainer> = mutableListOf()
    public var typesetterOptions: Typesetter.Options = Typesetter.Options()

    public var attributedString: AttributedString = AttributedString("")
    public var linePadding: Int = 0
    public var lineSpacing: Int = 1

    public fun layoutText() {
        textContainers.forEach { it.lines.clear() }

        val breakIterator = lineBreakIterator
        breakIterator.setText(attributedString.plaintext)
        val glyphGenerator = GlyphGenerator(attributedString, fallbackFonts)
        val typesetter = Typesetter(glyphGenerator)
        typesetter.options = typesetterOptions
        val pushBackIterator = PushBackIterator(typesetter)

        var containerIndex = 0
        var container = textContainers[containerIndex]

        /**
         * The current line's "target" Y position. When the line height changes and we need the text container to fix
         * it, we want to reset the line to its initial state first. Containers can shift a line vertically though, so
         * we need to keep track of its "ideal" position to reset it before each request.
         */
        var lineY = 0

        var line = LineFragment(0, lineY, container.width, 0)

        fun newLine(wrapped: Boolean) {
            val isBlank = line.glyphs.all { it.isInvisible }
            if (!isBlank) { // skip blank lines
                val originX = line.glyphs.first().posX
                line.baseline = line.glyphs.map { it.glyph.font?.ascent ?: 0 }.maxOrNull() ?: 0
                line.glyphs.forEach {
                    it.posX += linePadding - originX
                    it.posY += line.baseline
                }
                container.lines.add(line)
            }
            // create the next line
            if (!(isBlank && container.lines.isEmpty())) // ignore blank lines at the start of containers
                lineY = line.posY + line.height + lineSpacing
            line = LineFragment(0, lineY, container.width, 0)
        }

        for (glyph in pushBackIterator) {
            line.glyphs.add(glyph)
            if (line.height < glyph.height) {
                line.posX = 0
                line.posY = lineY
                line.width = container.width
                line.height = glyph.height
                container.fixLineFragment(line)
            }

            if (line.posY + line.height > container.height) {
                // grab the next container
                containerIndex++
                if (containerIndex >= textContainers.size) {
                    break // abort if we run out of space
                }
                container = textContainers[containerIndex]

                // it's simpler to just push the line back into the iterator and start over. If the next container is
                // narrower then this one we may have to search back farther for the break point
                while (line.glyphs.isNotEmpty()) {
                    pushBackIterator.pushBack(line.glyphs.removeLast())
                }

                lineY = 0
                line = LineFragment(0, lineY, container.width, 0)
                continue
            }

            if (glyph.codepoint == '\r'.toInt() || glyph.codepoint == '\n'.toInt()) {
                // if this is a \r\n pair...
                if (glyph.codepoint == '\r'.toInt() && pushBackIterator.hasNext() &&
                    pushBackIterator.peekNext().codepoint == '\n'.toInt()) {
                    pushBackIterator.next() // consume the \n
                }

                newLine(false)
            } else if (
                !glyph.isInvisible && // don't perform line breaks in the middle of trailing whitespace
                line.contentWidth > line.width - linePadding * 2
            ) {
                if (line.glyphs.size > 1) { // don't actually wrap single-character lines
                    var wrapPoint =
                        if (breakIterator.isBoundary(glyph.characterIndex))
                            glyph.characterIndex
                        else
                            breakIterator.preceding(glyph.characterIndex)
                    // if we couldn't find a suitable wrap point inside the line, wrap on the character instead.
                    if (wrapPoint <= line.characterRange.first) {
                        wrapPoint = glyph.characterIndex
                    }

                    // roll back to the wrap point, pushing everything back into the iterator
                    while (line.glyphs.isNotEmpty()) {
                        if (line.glyphs.last().characterIndex < wrapPoint) break
                        pushBackIterator.pushBack(line.glyphs.removeLast())
                    }
                }

                newLine(true)
            }
        }
        if (containerIndex < textContainers.size) // Don't try to put the final line in if we ran out of space
            newLine(false)
    }

    private val TypesetGlyph.height: Int
        get() = this.glyph.font?.height ?: 0
    private val Bitfont.height: Int
        get() = this.ascent + this.descent
}

public class LineFragment(public var posX: Int, public var posY: Int, public var width: Int, public var height: Int) {
    public val contentWidth: Int
        get() = if (glyphs.isEmpty()) 0 else glyphs.last().afterX - glyphs.first().posX
    public var baseline: Int = 0
        @JvmSynthetic
        internal set

    val glyphs: MutableList<GraphemeCluster> = mutableListOf()

    public val codepointRange: IntRange
        get() = if (glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().codepointIndex..glyphs.last().codepointIndex
    public val characterRange: IntRange
        get() = if (glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().characterIndex..glyphs.last().characterIndex
}