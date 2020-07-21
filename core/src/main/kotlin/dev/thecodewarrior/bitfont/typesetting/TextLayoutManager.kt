package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.math.max

open class TextLayoutManager(val fallbackFonts: List<Bitfont>) {
    //TODO: multiple text containers
    val textContainers: MutableList<TextContainer> = mutableListOf()
    var typesetterOptions = Typesetter.Options()

    var attributedString: AttributedString = AttributedString("")

    fun layoutText() {
        val glyphGenerator = GlyphGenerator(attributedString, fallbackFonts)
        val typesetter = Typesetter(glyphGenerator)
        typesetter.options = typesetterOptions

        textContainers.forEach { it.lines.clear() }

        val breakIterator = lineBreakIterator
        breakIterator.setText(attributedString.plaintext)

        val container = textContainers.first()

        val pushBackTypesetter = object: BufferedIterator<GraphemeCluster>() {
            private var pushingBack = false

            override fun refillBuffer() {
                if(!pushingBack && typesetter.hasNext())
                    push(typesetter.next())
            }

            fun pushBack(glyphs: List<GraphemeCluster>): List<GraphemeCluster> {
                pushingBack = true
                val mutable = glyphs.toMutableList()
                this.forEach { mutable.add(it) }
                mutable.forEach { push(it) }
                pushingBack = false
                return mutable
            }
        }

        var line = LineFragment(0, 0, container.size.x, 0)
        container.lines.add(line)
        var nextFragment: LineFragment? = null
        /**
         * Whether glyphs were consumed on this line. If this is false at least one glyph will be consumed, even if it
         * doesn't fit.
         */
        var consumedGlyphs = false
        var continuedBaseline: Int? = null

        // used when wrapping. Allocated once and reused for efficiency
        val pushBack = mutableListOf<GraphemeCluster>()

        for(glyph in pushBackTypesetter) {
            line.glyphs.add(glyph)
            if(line.height < glyph.height) {
                line.height = glyph.height
                nextFragment = container.fixLineFragment(line)
            }

            val canBeEmpty = nextFragment != null || consumedGlyphs
            var goToNextFragment = false
            if(glyph.codepoint == '\r'.toInt() || glyph.codepoint == '\n'.toInt()) {
                // if this is a \r\n pair...
                if(glyph.codepoint == '\r'.toInt() && pushBackTypesetter.hasNext() &&
                    pushBackTypesetter.peekNext().codepoint == '\n'.toInt()) {
                    pushBackTypesetter.next() // consume the \n
                }

                // don't go to the next fragment on this same line, create a new line
                nextFragment = null
                goToNextFragment = true

                // If the new cursor pos is beyond the end of the line, _and_ we have glyphs we can wrap, try to wrap.
                // We only wrap if this glyph is not the first in the line, since zero-glyph lines will often lead to
                // infinite wrapping
            } else if(!glyph.isWhitespace && glyph.afterX > line.width - container.lineFragmentPadding * 2 && (canBeEmpty || line.glyphs.size > 1)) {
                var wrapPoint =
                    if (breakIterator.isBoundary(glyph.characterIndex))
                        glyph.characterIndex
                    else
                        breakIterator.preceding(glyph.characterIndex)
                // if we couldn't find a suitable wrap point inside the line, wrap on the character instead.
                if (wrapPoint <= line.characterRange.first) {
                    if(canBeEmpty)
                        wrapPoint = line.characterRange.first
                    else
                        wrapPoint = glyph.characterIndex
                }

                val iter = line.glyphs.iterator()
                if (!canBeEmpty)
                    iter.next()
                for (toWrap in iter) {
                    if (toWrap.characterIndex >= wrapPoint) {
                        pushBack.add(toWrap)
                        iter.remove()
                    }
                }

                goToNextFragment = true
            }

            if(goToNextFragment || !pushBackTypesetter.hasNext()) {
                val bufferedGlyphs = pushBackTypesetter.pushBack(pushBack)
                val xOffset = bufferedGlyphs.firstOrNull()?.posX ?: 0

                bufferedGlyphs.forEach {
                    it.posX -= xOffset
                }
                typesetter.resetCursor(bufferedGlyphs.lastOrNull()?.afterX ?: 0)
                val baselineShift = max(
                    line.glyphs.map { it.glyph.font?.ascent ?: 0 }.max() ?: 0,
                    continuedBaseline ?: 0
                )
                continuedBaseline = baselineShift
                line.glyphs.forEach {
                    it.posX += container.lineFragmentPadding
                    it.posY += baselineShift
                }
            }
            if(goToNextFragment) {
                if(nextFragment != null) {
                    consumedGlyphs = consumedGlyphs || line.glyphs.isNotEmpty()
                    line = nextFragment
                    nextFragment = container.fixLineFragment(line)
                } else {
                    line = LineFragment(0, line.posY + line.height, container.size.x, 0)
                    continuedBaseline = null
                    consumedGlyphs = false
                }
                container.lines.add(line)
                pushBack.clear()
            }
        }
    }

    private val TypesetGlyph.afterX: Int
        get() = this.posX + this.glyph.calcAdvance()
    private val TypesetGlyph.height: Int
        get() = (this.glyph.font?.height ?: 0) + 1 //TODO +1 line spacing shouldn't be hard-coded
    private val Bitfont.height: Int
        get() = this.ascent + this.descent
}

class LineFragment(var posX: Int, var posY: Int, var width: Int, var height: Int) {
    val maxX: Int get() = posX + width
    val maxY: Int get() = posY + height
    val glyphs: MutableList<GraphemeCluster> = mutableListOf()

    val codepointRange: IntRange
        get() = if(glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().codepointIndex .. glyphs.last().codepointIndex
    val characterRange: IntRange
        get() = if(glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().characterIndex .. glyphs.last().characterIndex
}