package dev.thecodewarrior.bitfont.typesetting

import com.sun.tools.corba.se.idl.InterfaceGen
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.extensions.lineBreakIterator

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
        var line = LineFragment(0, 0, attributedString)
        container.lines.add(line)

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

        for(glyph in pushBackTypesetter) {
            line.glyphs.add(glyph)

            // If the new cursor pos is beyond the end of the line, _and_ we have glyphs we can wrap, try to wrap.
            // We only wrap if this glyph is not the first in the line, since zero-glyph lines will often lead to
            // infinite wrapping
            if(glyph.afterX > container.size.x && line.glyphs.size > 1) {
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

                line = LineFragment(0, container.lines.size * 10, attributedString)
                container.lines.add(line)
            }
        }
    }

    private val TypesetGlyph.afterX: Int
        get() = posX + glyph.calcAdvance()
}

class LineFragment(val posX: Int, val posY: Int, val source: AttributedString) {
    val glyphs: MutableList<GraphemeCluster> = mutableListOf()

    val codepointRange: IntRange
        get() = if(glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().codepointIndex .. glyphs.last().codepointIndex
    val characterRange: IntRange
        get() = if(glyphs.isEmpty()) IntRange.EMPTY else glyphs.first().characterIndex .. glyphs.last().characterIndex
}