package games.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.extensions.characterBreakIterator
import games.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.streams.toList

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
    val font: Bitfont,
    /**
     * The string to typeset. TODO: Create attributed string
     */
    val string: String,
    /**
     * The width to wrap to. TODO: Allow dynamic wrap width (for wrapping around images and stuff)
     */
    val wrapWidth: Int = -1
) {
    protected val codepoints = string.codePoints().toList()

    open var glyphs: List<GlyphRender> = emptyList()
        protected set
    open var cursorEnd: Vec2i = Vec2i(0, 0)
        protected set

    protected open fun glyphFor(codepoint: Int): Glyph {
        return font.glyphs[codepoint] ?: font.defaultGlyph
    }

    protected open fun advanceFor(codepoint: Int): Int {
        return glyphFor(codepoint).calcAdvance(font.spacing)
    }

    init {
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
        runGlyphs.forEach { run ->
            run.forEach {
                glyphs.add(it.copy(
                    pos = Vec2i(it.pos.x, y),
                    posAfter = Vec2i(it.posAfter.x, y)
                ))
            }
            y += font.lineHeight
        }
        this.glyphs = glyphs
    }

    protected open fun layoutRun(range: IntRange): List<GlyphRender> {
        val list = mutableListOf<GlyphRender>()
        var cursor = 0
        range.forEach {
            val glyph = glyphFor(codepoints[it])
            val advance = glyph.calcAdvance(font.spacing)
            list.add(
                GlyphRender(it, codepoints[it], glyph, Vec2i(cursor, 0), Vec2i(cursor + advance, 0))
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
                x += advanceFor(codepoints[i])
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
        val index: Int, val codepoint: Int, val glyph: Glyph,
        val pos: Vec2i, val posAfter: Vec2i)

    companion object {
        val newlines = listOf(0x000a, 0x000b, 0x000c, 0x000d, 0x00085, 0x2028, 0x2029)
    }
}

data class Vec2i(val x: Int, val y: Int) {
    operator fun plus(other: Vec2i): Vec2i {
        return Vec2i(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2i): Vec2i {
        return Vec2i(x - other.x, y - other.y)
    }

    operator fun times(other: Vec2i): Vec2i {
        return Vec2i(x * other.x, y * other.y)
    }

    operator fun times(other: Int): Vec2i {
        return Vec2i(x * other, y * other)
    }
}

