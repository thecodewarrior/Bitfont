package games.thecodewarrior.bitfont.editor.typesetting

import com.ibm.icu.lang.UCharacter
import games.thecodewarrior.bitfont.editor.data.Bitfont
import games.thecodewarrior.bitfont.editor.data.Glyph
import games.thecodewarrior.bitfont.editor.utils.Vec2i
import games.thecodewarrior.bitfont.editor.utils.extensions.characterBreakIterator
import games.thecodewarrior.bitfont.editor.utils.extensions.lineBreakIterator
import kotlin.math.max

/**
 *
 * Important note for subclasses: You must call [typeset] yourself at the end of your constructor after you have
 * initialized any fields needed for your typesetting process. The default call in [TypesetString]'s constructor is
 * not run for subclasses in order to allow for clean inheritance.
 */
open class TypesetString(
    /**
     * The default font. TODO: If the attributed string has font attributes they will override this.
     */
    val defaultFont: Bitfont,
    /**
     * The string to typeset. TODO: Create attributed string
     */
    val attributedString: AttributedString,
    /**
     * The width to wrap to. TODO: Allow dynamic wrap width (for wrapping around images and stuff)
     */
    val wrapWidth: Int = -1
) {
    val string: String = attributedString.string
    /**
     * An array of codepoints representing [string]
     */
    protected val codepoints: IntArray
    /**
     * An array of equal length to [codepoints] that relates each codepoint to its location in [string]
     */
    protected val codepointIndices: IntArray

    open var glyphs: List<GlyphRender> = emptyList()
        protected set
    open var cursorEnd: Vec2i = Vec2i(0, 0)
        protected set

    protected open fun fontFor(index: Int): Bitfont {
        return attributedString[Attribute.font, index] ?: defaultFont
    }

    protected open fun glyphFor(index: Int): Glyph {
        val font = fontFor(index)
        return font.glyphs[codepoints[index]] ?: font.defaultGlyph
    }

    protected open fun advanceFor(index: Int): Int {
        return glyphFor(index).calcAdvance(fontFor(index).spacing)
    }

    init {
        val codepoints = mutableListOf<Int>()
        val codepointIndices = mutableListOf<Int>()
        var offset = 0
        while (offset < string.length) {
            val codepoint = string.codePointAt(offset)

            codepoints.add(codepoint)
            codepointIndices.add(offset)

            offset += Character.charCount(codepoint)
        }

        this.codepoints = codepoints.toIntArray()
        this.codepointIndices = codepointIndices.toIntArray()

        @Suppress("LeakingThis")
        if(this.javaClass == TypesetString::class.java)
            typeset()
    }

    /**
     * Called in the [TypesetString] constructor unless this object is a subclass, in which case it is the subclass's
     * responsibility to call this. This is done so subclasses can properly initialize their fields before typesetting
     * occurs.
     */
    protected open fun typeset() {
        val runRanges = makeRuns()
        val runGlyphs = runRanges.map { layoutRun(it) }
        val glyphs = mutableListOf<GlyphRender>()
        var y = 0
        runGlyphs.forEachIndexed { i, run ->
            var maxAscent = 0
            var maxDescent = 0
            run.forEach {
                val font = fontFor(it.codepointIndex)
                maxAscent = max(maxAscent, font.ascent)
                maxDescent = max(maxDescent, font.descent)
            }
            y += maxAscent
            run.forEach {
                glyphs.add(it.copy(
                    pos = Vec2i(it.pos.x, y),
                    posAfter = Vec2i(it.posAfter.x, y)
                ))
            }
            y += maxDescent
        }
        this.glyphs = glyphs
    }

    protected open fun layoutRun(range: IntRange): List<GlyphRender> {
        val list = mutableListOf<GlyphRender>()
        var cursor = 0
        range.forEach {
            if(codepoints[it] in newlines) return@forEach
            val glyph = glyphFor(it)
            val advance = glyph.calcAdvance(fontFor(it).spacing)
            list.add(
                GlyphRender(codepointIndices[it], it, codepoints[it], glyph, Vec2i(cursor, 0), Vec2i(cursor + advance, 0),
                    attributedString.attributesFor(codepointIndices[it]))
            )
            cursor += advance
        }
        return list
    }

    /**
     * Creates a list of ranges in [codepoints] to be placed on separate lines.
     */
    protected open fun makeRuns(): List<IntRange> {
        if(string.isEmpty()) return emptyList()

        val lines = mutableListOf<IntRange>()
        val wrapEnabled = wrapWidth > 0
        lineBreakIterator.setText(string)
        characterBreakIterator.setText(string)

        var i = 0
        var x = 0
        var lineStart = 0
        var nextPos: Int? = null

        fun addBreak(pos: Int) {
            var charBreak = if(characterBreakIterator.isBoundary(pos)) pos else characterBreakIterator.preceding(pos)
            if(charBreak == lineStart) // this would lead to 0 characters on a line and likely an infinite loop
                charBreak = characterBreakIterator.following(pos)
            lines.add(lineStart until charBreak)
            nextPos = charBreak
            lineStart = charBreak
            x = 0
        }

        while(i < codepoints.size) {
            if(codepoints[i] in newlines) {
                val isCrBeforeLf = codepoints[i] == 0x000D && codepoints.getOrNull(i+1) == 0x000A
                if(isCrBeforeLf)
                    addBreak(i+2)
                else
                    addBreak(i+1)
            } else {
                x += advanceFor(i)
                // if we are past the end of the line and aren't whitespace, wrap.
                // (whitespace shouldn't wrap to the beginning of a line)
                if(wrapEnabled && x > wrapWidth && !UCharacter.isWhitespace(codepoints[i])) {
                    val lineBreak = lineBreakIterator.preceding(i)
                    if(lineBreak <= lineStart) { // no available break points on this line, so we have to split by character
                        if(i == lineStart) {
                            addBreak(i + 1)
                        } else {
                            addBreak(i)
                        }
                    } else {
                        addBreak(lineBreak)
                    }
                }
            }
            i = nextPos ?: i + 1
            nextPos = null
        }
        if(i > lineStart) // if there are still characters to be pushed
            lines.add(lineStart until i)

        return lines
    }

    data class GlyphRender(
        val characterIndex: Int, val codepointIndex: Int, val codepoint: Int, val glyph: Glyph,
        val pos: Vec2i, val posAfter: Vec2i, val attributes: Map<Attribute<*>, Any>)

    companion object {
        val newlines = listOf(0x000a, 0x000b, 0x000c, 0x000d, 0x00085, 0x2028, 0x2029)
    }
}

