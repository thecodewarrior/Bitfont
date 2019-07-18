package dev.thecodewarrior.bitfont.typesetting

import com.sun.tools.corba.se.idl.InterfaceGen
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.math.max

open class TextLayoutManager(val fallbackFonts: List<Bitfont>) {
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
        var continuedBaseline: Int? = null

        for(glyph in pushBackTypesetter) {
            line.glyphs.add(glyph)
            if(line.height < glyph.height) {
                line.height = glyph.height
                nextFragment = container.fixLineFragment(line)
            }

            // If the new cursor pos is beyond the end of the line, _and_ we have glyphs we can wrap, try to wrap.
            // We only wrap if this glyph is not the first in the line, since zero-glyph lines will often lead to
            // infinite wrapping
            if(glyph.afterX > line.width - 4 && line.glyphs.size > 1) {
                var wrapPoint =
                    if(breakIterator.isBoundary(glyph.characterIndex))
                        glyph.characterIndex
                    else
                        breakIterator.preceding(glyph.characterIndex)
                // if we couldn't find a suitable wrap point inside the line, wrap on the character instead.
                if(wrapPoint <= line.characterRange.first)
                    wrapPoint = glyph.characterIndex

                val pushBack = mutableListOf<GraphemeCluster>()

                val iter = line.glyphs.iterator()
                iter.next() // always keep the first glyph.
                for(toWrap in iter) {
                    if(toWrap.characterIndex >= wrapPoint) {
                        pushBack.add(toWrap)
                        iter.remove()
                    }
                }

                val xOffset = pushBack.first().posX
                val bufferedGlyphs = pushBackTypesetter.pushBack(pushBack)

                bufferedGlyphs.forEach {
                    it.posX -= xOffset
                }
                typesetter.resetCursor(bufferedGlyphs.last().afterX)
                val baselineShift = max(
                    line.glyphs.map { it.glyph.font?.ascent ?: 0 }.max() ?: 0,
                    continuedBaseline ?: 0
                )
                continuedBaseline = baselineShift
                line.glyphs.forEach {
                    it.posX += 2
                    it.posY += baselineShift
                }

                if(nextFragment != null) {
                    line = nextFragment
                    nextFragment = null
                } else {
                    line = LineFragment(0, line.posY + line.height, container.size.x, 0)
                    continuedBaseline = null
                }
                container.lines.add(line)
            }
        }

        val baselineShift = max(
            line.glyphs.map { it.glyph.font?.ascent ?: 0 }.max() ?: 0,
            continuedBaseline ?: 0
        )
        line.glyphs.forEach {
            it.posY += baselineShift
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