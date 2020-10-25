package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.text.BreakIterator
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.PushBackIterator
import kotlin.math.max

public open class TextLayoutManager(font: Bitfont, vararg containers: TextContainer) {
    public val textContainers: MutableList<TextContainer> = mutableListOf(*containers)
    public val options: Options = Options(font)
    public var attributedString: AttributedString = AttributedString("")

    public enum class Alignment {
        LEFT, CENTER, RIGHT
    }

    public data class Options(
        /**
         * The default font to use
         */
        public var font: Bitfont,
        /**
         * The horizontal text alignment
         */
        public var alignment: Alignment,
        /**
         * The padding on the left and right side of lines
         */
        public var linePadding: Int,
        /**
         * The spacing between lines
         */
        public var lineSpacing: Int,
        /**
         * Additional typesetter options
         */
        public var typesetterOptions: Typesetter.Options,
        /**
         * Whether to swallow blank lines between containers. If this is true, when the text overflows into another
         * container any leading blank lines will be ignored.
         *
         * This avoids the following situation when wrapping paragraphs that have blank lines between them.
         *
         * Text:
         * ```
         * Lorem ipsum dolor
         *
         * sit amet consectetur
         *
         * adipiscing elit
         * ```
         * Layout:
         * ```
         *   Container A     Container B
         * |Lorem ipsum  | |             |
         * |dolor        | |adipiscing   |
         * |             | |elit         |
         * |sit amet     | |             |
         * |consectetur  | |             |
         * ```
         */
        public var swallowBlanksBetweenContainers: Boolean,
        /**
         * If the text couldn't fit, this string is inserted at the end. This will overwrite characters as necessary in
         * order to fit.
         *
         * For example, with a truncation string of `"..."`
         * ```
         *      text:  This doesn't fit in the container
         * container: |                              |
         *    result:  This doesn't fit in the con...
         * ```
         */
        public var truncationString: AttributedString?
    ) {
        public constructor(font: Bitfont, alignment: Alignment): this(
            font,
            alignment,
            0, 1,
            Typesetter.Options(),
            true,
            null
        )

        public constructor(font: Bitfont): this(
            font,
            Alignment.LEFT
        )
    }

    /**
     * This is replaced with a thread-local iterator and configured with the current text before running any layout.
     */
    protected val breakIterator: BreakIterator = BreakIterator.getLineInstance()

    /**
     * The glyphs being laid out
     */
    protected var pushBackIterator: PushBackIterator<GraphemeCluster> = PushBackIterator(emptyList<GraphemeCluster>().iterator())

    public open fun layoutText() {
        if(textContainers.isEmpty())
            return

        textContainers.forEach { it.lines.clear() }

        // setting up state and inputs
        breakIterator.setText(attributedString.plaintext)
        val glyphGenerator = GlyphGenerator(attributedString, options.font)
        val typesetter = Typesetter(glyphGenerator)
        typesetter.options = options.typesetterOptions
        pushBackIterator = PushBackIterator(typesetter)

        // we store the internal line fragments ourself first, then add them to the containers at the very end
        val containerLayouts = mutableListOf<ContainerLayout>()

        // lay out the text into the containers
        for ((i, container) in textContainers.withIndex()) {
            val layout = layoutForContainer(
                container,
                options.swallowBlanksBetweenContainers && i != 0
            )
            containerLayouts.add(layout)
            if(!pushBackIterator.hasNext())
                break // everything has been laid out
        }

        // truncate the last line if we ran out of space and truncation is enabled
        if(pushBackIterator.hasNext() && options.truncationString != null) {
            truncateLastLine(containerLayouts.flatMap { it.lines })
        }


        // add the lines to the containers
        for((i, layout) in containerLayouts.withIndex()) {
            val container = textContainers[i]
            layout.lines.forEach { line ->
                // fix positions *after* truncation
                fixGlyphPositions(line)
                container.lines.add(line.toLineFragment())
            }
        }
    }

    /**
     * Applies truncation to the last line in the given list.
     *
     * This will:
     * - typeset the truncation string
     * - add back any glyphs that were wrapped, filling the line to its maximum
     * - remove glyphs from the end until the truncation string fits
     * - append the truncation string
     */
    protected open fun truncateLastLine(allLines: List<LineLayout>) {
        // === preconditions ===
        val truncationString = options.truncationString ?: return
        if(allLines.isEmpty())
            return

        val line = allLines.last()

        // === typeset the truncation string ===
        val glyphGenerator = GlyphGenerator(truncationString, options.font)
        val typesetter = Typesetter(glyphGenerator)
        typesetter.options = options.typesetterOptions
        val truncationGlyphs = typesetter.asSequence().toList()
        if(truncationGlyphs.isEmpty())
            return

        // === add back wrapped glyphs ===
        line.glyphs.addAll(line.wrappedGlyphs)

        // === remove glyphs to make space ===
        val truncationLength = truncationGlyphs.last().afterX
        while(line.glyphs.isNotEmpty() && line.contentWidth > line.innerWidth - truncationLength) {
            line.glyphs.removeLast()
        }

        // === append truncation string ===
        val truncationStartX = line.glyphs.lastOrNull()?.afterX ?: 0
        truncationGlyphs.forEach {
            it.posX += truncationStartX
        }
        line.glyphs.addAll(truncationGlyphs)
    }

    /**
     * Lay out text into the provided container. If [swallowLeadingBlanks] is true, any blank lines at the start of
     * the container should be ignored.
     *
     * @param container The container to lay text into
     * @param swallowLeadingBlanks Whether to swallow leading blanks
     */
    protected open fun layoutForContainer(container: TextContainer, swallowLeadingBlanks: Boolean): ContainerLayout {
        val layout = ContainerLayout()
        var lineY = 0

        // try to add more lines until we run out of text
        while(pushBackIterator.hasNext()) {
            if(layout.lines.size >= container.maxLines)
                break // we're out of space
            val line = layoutLine(container, lineY)
                ?: break // we're out of space

            val isBlank = line.glyphs.all { it.isInvisible }

            if(isBlank && layout.lines.isEmpty() && swallowLeadingBlanks)
                continue // if we're still swallowing leading blanks, continue
            lineY = line.posY + line.height + options.lineSpacing

            layout.lines.add(line)
        }

        return layout
    }

    /**
     * Lay out a single line at [lineY] for the provided container.
     *
     * - Any glyphs that don't get used (e.g. were wrapped or the line didn't fit in the container) should be pushed
     * back onto [pushBackIterator]
     * - If the line didn't fit in the container this should return null
     *
     * The returned line will not have valid glyph positions (see [fixGlyphPositions]). That is calculated after truncation
     * alignment applied (see [applyAlignment]).
     *
     * @return The laid out line, or null if it didn't fit in the container.
     */
    protected open fun layoutLine(container: TextContainer, lineY: Int): LineLayout? {
        var maxAscent = 0
        var maxDescent = 0

        val line = LineLayout(options.linePadding, options.lineSpacing, 0, lineY, container.width, 0)
        for (glyph in pushBackIterator) {
            line.glyphs.add(glyph)
            maxAscent = max(maxAscent, glyph.glyph.font?.ascent ?: 0)
            maxDescent = max(maxDescent, glyph.glyph.font?.descent ?: 0)

            if (line.height < maxAscent + maxDescent) {
                val bounds = TextContainer.LineBounds(options.lineSpacing,
                    0, lineY,
                    container.width, maxAscent + maxDescent
                )
                container.fixLineFragment(bounds)
                line.setBounds(bounds)
            }

            // the line doesn't fit in the container
            if (line.posY + line.height > container.height) {
                // push everything back onto the iterator
                while (line.glyphs.isNotEmpty()) {
                    pushBackIterator.pushBack(line.glyphs.removeLast())
                }

                return null // a null return signals that we ran out of space
            }

            // an explicit line break occurred
            if (glyph.codepoint == '\r'.toInt() || glyph.codepoint == '\n'.toInt()) {
                // if this is a \r\n pair, consume the \n
                if (glyph.codepoint == '\r'.toInt() && pushBackIterator.hasNext() &&
                    pushBackIterator.peekNext().codepoint == '\n'.toInt()) {
                    pushBackIterator.next()
                }

                break // we're done with this line
            }

            // line breaking
            if (
                !glyph.isInvisible && // don't perform line breaks in the middle of trailing whitespace
                line.contentWidth > line.innerWidth
            ) {
                if (line.glyphs.size > 1) { // don't actually wrap single-character lines
                    // find the closest wrap point. Either on this character or before it
                    var wrapPoint =
                        if (breakIterator.isBoundary(glyph.characterIndex))
                            glyph.characterIndex
                        else
                            breakIterator.preceding(glyph.characterIndex)
                    // if we couldn't find a suitable wrap point inside the line, wrap on the character instead.
                    if (wrapPoint <= line.glyphs.first().characterIndex) {
                        wrapPoint = glyph.characterIndex
                    }

                    // roll back to the wrap point, pushing everything back into the iterator, and add them to the
                    // wrapped glyphs
                    while (line.glyphs.isNotEmpty()) {
                        if (line.glyphs.last().characterIndex < wrapPoint) break
                        val dropped = line.glyphs.removeLast()
                        pushBackIterator.pushBack(dropped)
                        line.wrappedGlyphs.add(dropped)
                    }
                    line.wrappedGlyphs.reverse() // the glyphs were dropped in reverse order
                }

                break // we're done with this line
            }
        }

        return line
    }

    /**
     * After typesetting the glyph positions in a line are incorrect, so we need to shift them into place. Text
     * alignment is also performed at this point.
     *
     * - The typesetter spits out glyphs off to infinity so for every line but the first we have to shift the glyphs
     * left so they start at 0.
     * - The glyphs are positioned with their baseline at 0, but 0 for a line is their top-left corner, so we have to
     * shift the glyphs down.
     */
    protected open fun fixGlyphPositions(line: LineLayout) {
        // === shift glyphs to 0 ===
        // the amount to shift glyphs left in order for them to start at 0
        var startX = line.glyphs.first().posX

        // === apply line padding ===
        startX -= options.linePadding

        // === apply alignment ===
        // compute the _visual_ right edge.
        val rightEdge = line.glyphs.last().let {
            it.posX + it.glyph.bearingX + it.glyph.image.width
        }
        val visualWidth = rightEdge - line.glyphs.first().posX
        val remainingSpace = line.innerWidth - visualWidth
        // offset the starting point
        startX -= when (options.alignment) {
            Alignment.LEFT -> 0
            Alignment.RIGHT -> remainingSpace
            Alignment.CENTER -> remainingSpace / 2
        }

        // === baseline correction ===
        // compute the baseline offset (lines are positioned based on their top-left corner, but glyphs are
        // positioned based on on their baseline)
        line.baseline = line.glyphs.map { it.glyph.font?.ascent ?: 0 }.maxOrNull() ?: 0

        // === applying shifts ===
        // shift all the glyphs into the correct locations
        line.glyphs.forEach {
            it.posX -= startX
            it.posY += line.baseline
        }
    }

    protected class ContainerLayout {
        public val lines: MutableList<LineLayout> = mutableListOf()
    }

    protected class LineLayout(
        public val padding: Int,
        public val spacing: Int,
        public var posX: Int,
        public var posY: Int,
        public var width: Int,
        public var height: Int
    ) {
        /**
         * The capacity of the line taking into account padding
         */
        public val innerWidth: Int
            get() = width - padding * 2
        public val contentWidth: Int
            get() = if (glyphs.isEmpty()) 0 else glyphs.last().afterX - glyphs.first().posX
        public var baseline: Int = 0

        /**
         * The glyphs in this line
         */
        public val glyphs: MutableList<GraphemeCluster> = mutableListOf()

        /**
         * The glyphs that were wrapped off this line. This is used when truncating text. When truncating we want to ignore
         * wrapping and just display everything up until the suffix.
         *
         * We need to store this in the line because when a line won't fit we need to go back and apply truncation to the
         * previous line.
         */
        public val wrappedGlyphs: MutableList<GraphemeCluster> = mutableListOf()

        public fun setBounds(bounds: TextContainer.LineBounds) {
            posX = bounds.posX
            posY = bounds.posY
            width = bounds.width
            height = bounds.height
        }

        public fun toLineFragment(): LineFragment {
            return LineFragment(posX, posY, width, height, glyphs)
        }
    }

}

public class LineFragment(
    public var posX: Int, public var posY: Int,
    public var width: Int, public var height: Int,
    public val glyphs: List<GraphemeCluster>
) {
    public val contentWidth: Int
        get() = if (glyphs.isEmpty()) 0 else glyphs.last().afterX - glyphs.first().posX
    public var baseline: Int = 0
}