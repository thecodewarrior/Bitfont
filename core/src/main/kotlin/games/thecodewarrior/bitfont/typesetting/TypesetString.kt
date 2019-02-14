package games.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import com.ibm.icu.lang.UProperty
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.utils.AttributeMap
import games.thecodewarrior.bitfont.utils.CombiningClass
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.characterBreakIterator
import games.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Important note for subclasses: You must call [typeset] yourself at the end of your constructor after you have
 * initialized any fields needed for your typesetting process. The default call in [TypesetString]'s constructor is
 * not run for subclasses in order to allow for clean inheritance.
 */
open class TypesetString(
    /**
     * The default font.
     */
    val defaultFont: Bitfont,
    /**
     * The string to typeset.
     */
    val attributedString: AttributedString,
    /**
     * The width to wrap to. TODO: Allow dynamic wrap width (for wrapping around images and stuff)
     */
    val wrapWidth: Int = -1,
    /**
     * The number of pixels to insert between lines
     */
    val lineSpacing: Int = 1
) {
    val string: String = attributedString.plaintext
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
    open var lines: List<Line> = emptyList()
        protected set
    open var glyphMap: Map<Int, GlyphRender> = emptyMap()
        protected set

    fun closestCharacter(pos: Vec2i): Int {
        val absoluteTop = lines.firstOrNull()?.let { it.baseline-it.maxAscent } ?: 0
        val absoluteBottom = lines.lastOrNull()?.let { it.baseline+it.maxDescent } ?: 0
        if(pos.y < absoluteTop) {
            return 0
        }
        if(pos.y > absoluteBottom) {
            return string.length
        }

        val closestLine = lines.minBy {
            val top = it.baseline-it.maxAscent
            val bottom = it.baseline+it.maxDescent
            if(pos.y in top until bottom) {
                0
            } else {
                min(abs(pos.y - top), abs(pos.y - bottom))
            }
        } ?: return 0
        return closestLine.closestCharacter(pos.x)
    }

    protected open fun fontFor(index: Int): Bitfont {
        return attributedString[Attribute.font, codepointIndices[index]]?.let {
            if(codepoints[index] in it.glyphs) it else null
        } ?: defaultFont
    }

    protected open fun glyphFor(index: Int): Glyph {
        val font = fontFor(index)
        return font.glyphs[codepoints[index]] ?: font.defaultGlyph
    }

    protected open fun advanceFor(index: Int): Int {
        val combiningClass = UCharacter.getCombiningClass(codepoints[index])
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
        val runGlyphs = runRanges.mapIndexed { i, range -> range to layoutRun(i, range) }
        val lineGlyphs = mutableListOf<MutableList<GlyphRender>>()
        val lines = mutableListOf<Line>()
        var y = 0
        runGlyphs.forEachIndexed { i, (range, run) ->
            var maxAscent = 0
            var maxDescent = 0
            range.forEach {
                val font = fontFor(it)
                maxAscent = max(maxAscent, font.ascent)
                maxDescent = max(maxDescent, font.descent)
            }
            run.forEach {
                maxAscent = max(maxAscent, -(it.pos.y + it.glyph.bearingY))
                maxDescent = max(maxDescent, it.glyph.image.height + it.pos.y + it.glyph.bearingY)
            }

            y += maxAscent
            val offsetGlyphs = run.map {
                it.copy(
                    pos = Vec2i(it.pos.x, it.pos.y + y),
                    posAfter = Vec2i(it.posAfter.x, it.pos.y + y)
                )
            }
            lineGlyphs.lastOrNull()?.also { prevLine ->
                val lineStart = offsetGlyphs.first().pos
                if(prevLine.last().codepoint in newlines) {
                    prevLine[prevLine.size-1] = prevLine.last().copy(posAfter = lineStart)
                }
            }
            lineGlyphs.add(offsetGlyphs.toMutableList())
            lines.add(Line(
                i, y, maxAscent, maxDescent,
                offsetGlyphs.first().pos.x, offsetGlyphs.last().posAfter.x,
                emptyList()
            ))
            y += maxDescent + lineSpacing
        }

        // set the posAfter for trailing newlines
        lineGlyphs.lastOrNull()?.also { prevLine ->
            if(prevLine.last().codepoint in newlines) {
                y += prevLine.last().font.ascent
                prevLine[prevLine.size-1] = prevLine.last().copy(posAfter = Vec2i(0, y))
            }
        }
        this.glyphs = lineGlyphs.flatten()
        this.lines = lines.mapIndexed { i, it ->
            it.copy(glyphs = lineGlyphs[i])
        }
        this.glyphMap = glyphs.associateBy { it.characterIndex }
    }

    protected open fun layoutRun(line: Int, range: IntRange): List<GlyphRender> {
        val list = mutableListOf<GlyphRender>()
        var cursor = 0
        var combiningGap = 0
        var combiningRectMin = Vec2i(0, 0)
        var combiningRectMax = Vec2i(0, 0)
        var combiningPosAfter = Vec2i(0, 0)
        range.forEach {
            val glyph = glyphFor(it)
            val font = fontFor(it)
            val advance = glyph.calcAdvance(font.spacing)
            val combiningClass = CombiningClass[UCharacter.getCombiningClass(codepoints[it])]

            if(combiningClass == CombiningClass.NOT_REORDERED) {
                combiningGap = max(1, font.capHeight / 8)
                combiningRectMin = Vec2i(
                    cursor + glyph.bearingX, glyph.bearingY
                )
                combiningRectMax = Vec2i(
                    cursor + glyph.bearingX + glyph.image.width, glyph.bearingY + glyph.image.height
                )
                combiningPosAfter = Vec2i(cursor + advance, 0)
                list.add(
                    GlyphRender(codepointIndices[it], it, codepoints[it], line,
                        font, glyph,
                        Vec2i(cursor, 0), Vec2i(cursor + advance, 0),
                        attributedString.getAttributes(codepointIndices[it]))
                )
                cursor += advance
            } else {
                val cls = combiningClass
                val rectMin = combiningRectMin
                val rectMax = combiningRectMax
                val gapX = if(cls.attached || cls.yAlign != 0) 0 else combiningGap
                val gapY = if(cls.attached && cls.yAlign != 0) 0 else combiningGap

                val newX = when(cls.xAlign) {
                    -2 -> {
                        rectMin.x - glyph.bearingX - glyph.image.width - gapX
                    }
                    -1 -> {
                        rectMin.x - glyph.bearingX
                    }
                    0 -> {
                        rectMin.x - glyph.bearingX + (rectMax.x - rectMin.x - glyph.image.width)/2
                    }
                    1 -> {
                        rectMax.x - glyph.bearingX - glyph.image.width
                    }
                    2 -> {
                        rectMax.x - glyph.bearingX + gapX
                    }
                    3 -> {
                        rectMax.x - glyph.bearingX - (glyph.image.width)/2
                    }
                    else -> rectMin.x
                }
                val newY: Int
                when(cls.yAlign) {
                    -1 -> {
                        newY = rectMin.y - gapY - glyph.image.height - glyph.bearingY
                        combiningRectMin -= Vec2i(0, glyph.image.height + gapY)
                    }
                    0 -> {
                        newY = rectMin.y - glyph.bearingY + (rectMax.y - rectMin.y - glyph.image.height)/2
                    }
                    1 -> {
                        newY = rectMax.y + gapY - glyph.bearingY
                        combiningRectMax += Vec2i(0, glyph.image.height + gapY)
                    }
                    else -> {
                        newY = 0
                    }
                }

                list.add(
                    GlyphRender(codepointIndices[it], it, codepoints[it], line,
                        font, glyph,
                        Vec2i(newX, newY), combiningPosAfter,
                        attributedString.getAttributes(codepointIndices[it]))
                )
            }
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
            lines.add(lineStart until pos)
            nextPos = pos
            lineStart = pos
            x = 0
        }

        fun findBreak(pos: Int) {
            var charBreak = if(characterBreakIterator.isBoundary(pos)) pos else characterBreakIterator.preceding(pos)
            if(charBreak == lineStart) // this would lead to 0 characters on a line and likely an infinite loop
                charBreak = characterBreakIterator.following(pos)
            addBreak(charBreak)
        }

        while(i < codepoints.size) {
            if(codepoints[i] in newlines) {
                val isCrBeforeLf = codepoints[i] == 0x000D && codepoints.getOrNull(i+1) == 0x000A
                if(isCrBeforeLf)
                    addBreak(i+2)
                else
                    addBreak(i+1)
            } else {
                val combiningClass = CombiningClass[UCharacter.getCombiningClass(codepoints[i])]
                if(combiningClass == CombiningClass.NOT_REORDERED || combiningClass.xAlign == -2 || combiningClass.xAlign == 2)
                    x += advanceFor(i)
                // if we are past the end of the line and aren't whitespace, wrap.
                // (whitespace shouldn't wrap to the beginning of a line)
                if(wrapEnabled && x > wrapWidth && !UCharacter.isWhitespace(codepoints[i])) {
                    val lineBreak = lineBreakIterator.preceding(i)
                    if(lineBreak <= lineStart) { // no available break points on this line, so we have to split by character
                        if(i == lineStart) {
                            findBreak(i + 1)
                        } else {
                            findBreak(i)
                        }
                    } else {
                        findBreak(lineBreak)
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
        val characterIndex: Int, val codepointIndex: Int, val codepoint: Int, val line: Int,
        val font: Bitfont, val glyph: Glyph,
        val pos: Vec2i, val posAfter: Vec2i, val attributes: AttributeMap) {
    }

    data class Line(
        val lineNumber: Int, val baseline: Int,
        val maxAscent: Int, val maxDescent: Int,
        val startX: Int, val endX: Int,
        val glyphs: List<GlyphRender>
    ) {
        fun closestCharacter(pos: Int): Int {
            return glyphs.minBy { abs(pos - it.pos.x) }?.characterIndex ?: 0
        }
    }

    companion object {
        val newlines = listOf(0x000a, 0x000b, 0x000c, 0x000d, 0x00085, 0x2028, 0x2029)
    }
}

