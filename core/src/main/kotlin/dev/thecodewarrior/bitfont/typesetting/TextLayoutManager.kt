package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.text.BreakIterator
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.PushBackIterator

public class TextLayoutManager(font: Bitfont, vararg containers: TextContainer) {
    public val textContainers: MutableList<TextContainer> = mutableListOf(*containers)
    public var options: Options = Options(font)

    public var delegate: TextLayoutDelegate? = null

    /**
     * The last [MutableAttributedString] version. The version of non-mutable attributed strings is always zero.
     */
    private var layoutVersion = -1
    public var attributedString: AttributedString = AttributedString("")
        set(value) {
            if(field !== value)
                layoutVersion = -1
            field = value
        }

    /**
     * Whether the [attributedString] is dirty. This does not take into account changes to [options] or container
     * settings.
     */
    public fun isStringDirty(): Boolean {
        return attributedString.version != layoutVersion
    }

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
         * The overall typographic leading, i.e. the amount of space to put between each line. May be negative.
         *
         * The name comes from the practice of placing lead strips between lines of type on a printing press.
         */
        public var leading: Int,
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
    private val breakIterator: BreakIterator = BreakIterator.getLineInstance()

    /**
     * The glyphs being laid out
     */
    private var pushBackIterator: PushBackIterator<GraphemeCluster> = PushBackIterator(emptyList<GraphemeCluster>().iterator())

    public fun layoutText() {
        delegate?.textWillLayout()
        layoutVersion = attributedString.version
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
            layout.lines.forEachIndexed { lineIndex, line ->
                // fix positions *after* truncation
                fixGlyphPositions(line)
                container.lines.add(line.toTypesetLine(lineIndex))
            }
        }
        delegate?.textDidLayout()
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
    private fun truncateLastLine(allLines: List<LineLayout>) {
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
        line.clusters.addAll(line.wrappedGlyphs)

        // === remove glyphs to make space ===
        val truncationLength = truncationGlyphs.last().baselineEnd
        while(line.clusters.isNotEmpty() && line.contentWidth > line.innerWidth - truncationLength) {
            line.clusters.removeLast()
        }

        // === append truncation string ===
        val truncationStartX = line.clusters.lastOrNull()?.baselineEnd ?: 0
        truncationGlyphs.forEach {
            it.offsetX(truncationStartX)
        }
        line.clusters.addAll(truncationGlyphs)
    }

    /**
     * Lay out text into the provided container. If [swallowLeadingBlanks] is true, any blank lines at the start of
     * the container should be ignored.
     *
     * @param container The container to lay text into
     * @param swallowLeadingBlanks Whether to swallow leading blanks
     */
    private fun layoutForContainer(container: TextContainer, swallowLeadingBlanks: Boolean): ContainerLayout {
        val layout = ContainerLayout()
        var lineY = 0

        // try to add more lines until we run out of text
        while(pushBackIterator.hasNext()) {
            if(layout.lines.size >= container.maxLines)
                break // we're out of space
            val line = layoutLine(container, lineY)
                ?: break // we're out of space

            val isBlank = line.clusters.all { it.isBlank }

            if(isBlank && layout.lines.isEmpty() && swallowLeadingBlanks)
                continue // if we're still swallowing leading blanks, continue
            lineY = line.posY + line.height + line.spacing

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
    private fun layoutLine(container: TextContainer, lineY: Int): LineLayout? {
        var maxAscent = 0
        var maxDescent = 0
        var maxLeading: Int? = null

        val line = LineLayout(options.linePadding, options.leading, 0, lineY, container.width, 0)
        for (cluster in pushBackIterator) {
            line.clusters.add(cluster)
            var lineDirty = false
            if(cluster.metrics.ascent > maxAscent) {
                maxAscent = cluster.metrics.ascent
                lineDirty = true
            }
            if(cluster.metrics.descent > maxDescent) {
                maxDescent = cluster.metrics.descent
                lineDirty = true
            }
            for(glyph in cluster.glyphs) {
                val leading = glyph[TextAttribute.leading] ?: continue
                if(maxLeading == null || leading > maxLeading) {
                    maxLeading = leading
                    lineDirty = true
                }
            }

            if (lineDirty) {
                val bounds = TextContainer.LineBounds(
                    options.leading + (maxLeading ?: 0),
                    0, lineY,
                    container.width, maxAscent + maxDescent
                )
                container.fixLineFragment(bounds)
                line.setBounds(bounds)
            }

            // the line doesn't fit in the container
            if (line.posY + line.height > container.height) {
                // push everything back onto the iterator
                while (line.clusters.isNotEmpty()) {
                    pushBackIterator.pushBack(line.clusters.removeLast())
                }

                return null // a null return signals that we ran out of space
            }

            // an explicit line break occurred
            if (cluster.codepoint == '\r'.code || cluster.codepoint == '\n'.code) {
                // if this is a \r\n pair, consume the \n
                if (cluster.codepoint == '\r'.code && pushBackIterator.hasNext() &&
                    pushBackIterator.peekNext().codepoint == '\n'.code) {
                    pushBackIterator.next()
                }

                break // we're done with this line
            }

            // line breaking
            if (
                !cluster.isBlank && // don't perform line breaks in the middle of trailing whitespace
                line.contentWidth > line.innerWidth
            ) {
                if (line.clusters.size > 1) { // don't actually wrap single-character lines
                    // find the closest wrap point. Either on this character or before it
                    var wrapPoint =
                        if (breakIterator.isBoundary(cluster.index))
                            cluster.index
                        else
                            breakIterator.preceding(cluster.index)
                    // if we couldn't find a suitable wrap point inside the line, wrap on the character instead.
                    if (wrapPoint <= line.clusters.first().index) {
                        wrapPoint = cluster.index
                    }

                    // roll back to the wrap point, pushing everything back into the iterator, and add them to the
                    // wrapped glyphs
                    while (line.clusters.isNotEmpty()) {
                        if (line.clusters.last().index < wrapPoint) break
                        val dropped = line.clusters.removeLast()
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
     * - The typesetter spits out glyphs off to infinity, so for every line but the first we have to shift the glyphs
     * left until they start at 0.
     * - The glyphs are positioned with their baseline at 0, so we have to shift the glyphs down to where their line is.
     */
    private fun fixGlyphPositions(line: LineLayout) {
        // === shift glyphs to 0 ===
        // the amount to shift glyphs left in order for them to start at 0
        var offsetX = -line.clusters.first().baselineStart

        // === apply line padding ===
        offsetX += options.linePadding

        // === apply alignment ===
        // compute the _visual_ right edge.
        val rightEdge = line.clusters.lastOrNull { !it.isBlank }?.let {
            it.baselineStart + it.metrics.bearingX + it.metrics.width
        } ?: line.clusters.first().baselineStart
        val visualWidth = rightEdge - line.clusters.first().baselineStart
        val remainingSpace = line.innerWidth - visualWidth
        // offset the starting point
        offsetX += when (options.alignment) {
            Alignment.LEFT -> 0
            Alignment.RIGHT -> remainingSpace
            Alignment.CENTER -> remainingSpace / 2
        }

        // === baseline correction ===
        // compute the baseline offset (lines are positioned based on their top-left corner, but glyphs are
        // positioned based on on their baseline)
        line.baseline = line.clusters.maxOfOrNull { it.metrics.ascent } ?: 0

        // === applying shifts ===
        // shift all the glyphs into the correct locations
        line.clusters.forEach { cluster ->
            cluster.offsetX(line.posX + offsetX)
            cluster.offsetY(line.posY + line.baseline)
        }
    }

    private class ContainerLayout {
        public val lines: MutableList<LineLayout> = mutableListOf()
    }

    private class LineLayout(
        public val padding: Int,
        public var spacing: Int,
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
            get() = if (clusters.isEmpty()) 0 else clusters.last().baselineEnd - clusters.first().baselineStart
        public var baseline: Int = 0

        /**
         * The glyphs in this line
         */
        public val clusters: MutableList<GraphemeCluster> = mutableListOf()

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
            spacing = bounds.leading
        }

        public fun toTypesetLine(lineIndex: Int): TextContainer.TypesetLine {
            return TextContainer.TypesetLine(lineIndex, posX, posY, baseline, width, height, clusters)
        }
    }

}
