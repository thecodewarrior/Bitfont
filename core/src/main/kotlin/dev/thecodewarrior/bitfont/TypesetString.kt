package dev.thecodewarrior.bitfont

import com.ibm.icu.lang.UCharacter
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.font
import dev.thecodewarrior.bitfont.utils.Attribute
import dev.thecodewarrior.bitfont.utils.AttributeMap
import dev.thecodewarrior.bitfont.utils.CombiningClass
import dev.thecodewarrior.bitfont.utils.ExperimentalBitfont
import dev.thecodewarrior.bitfont.utils.Vec2i
import dev.thecodewarrior.bitfont.utils.extensions.characterBreakIterator
import dev.thecodewarrior.bitfont.utils.extensions.lineBreakIterator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.toList

/**
 *
 * Important note for subclasses: You must call [typeset] yourself at the end of your constructor after you have
 * initialized any fields needed for your typesetting process. The default call in [TypesetString]'s constructor is
 * not run for subclasses in order to allow for clean inheritance.
 */
@ExperimentalBitfont
open class TypesetString(
    /**
     * The default font.
     */
    val defaultFont: dev.thecodewarrior.bitfont.data.Bitfont,
    /**
     * The string to typeset.
     */
    val attributedString: dev.thecodewarrior.bitfont.typesetting.AttributedString,
    /**
     * The width to wrap to.
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

    open var glyphs: List<dev.thecodewarrior.bitfont.TypesetString.GlyphRender> = emptyList()
        protected set
    open var lines: List<dev.thecodewarrior.bitfont.TypesetString.Line> = emptyList()
        protected set
    open var glyphMap: Map<Int, dev.thecodewarrior.bitfont.TypesetString.GlyphRender> = emptyMap()
        protected set

    fun closestLine(y: Int): Int {
        if(lines.isEmpty()) return 0

        val absoluteTop = lines.first().let { it.baseline-it.maxAscent }
        val absoluteBottom = lines.last().let { it.baseline+it.maxDescent }
        if(y < absoluteTop) {
            return -1
        }
        if(y > absoluteBottom) {
            return lines.size
        }

        val closestLine = lines.minBy {
            val top = it.baseline-it.maxAscent
            val bottom = it.baseline+it.maxDescent
            if(y in top until bottom) {
                0
            } else {
                min(abs(y - top), abs(y - bottom))
            }
        }!!

        return closestLine.lineNumber
    }

    fun closestCharacter(pos: Vec2i): Pair<Int, Boolean> {
        val lineIndex = closestLine(pos.y)
        if(lineIndex == lines.size) return string.length to false
        val line = lines.getOrNull(lineIndex) ?: return 0 to false
        return line.closestCharacter(pos.x)
    }

    fun containingCharacter(pos: Vec2i): Int {
        val lineIndex = closestLine(pos.y)
        if(lineIndex == lines.size) return string.length
        val line = lines.getOrNull(lineIndex) ?: return 0
        return line.containingCharacter(pos.x)
    }

    protected open fun fontFor(index: Int): dev.thecodewarrior.bitfont.data.Bitfont {
        return attributedString[Attribute.font, codepointIndices[index]]?.let {
            if(codepoints[index] in it.glyphs) it else null
        } ?: defaultFont
    }

    protected open fun glyphFor(index: Int): dev.thecodewarrior.bitfont.data.Glyph {
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
        if(this.javaClass == dev.thecodewarrior.bitfont.TypesetString::class.java)
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
        val lineGlyphs = mutableListOf<MutableList<dev.thecodewarrior.bitfont.TypesetString.GlyphRender>>()
        val lines = mutableListOf<dev.thecodewarrior.bitfont.TypesetString.Line>()
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
                if(prevLine.last().codepoint in dev.thecodewarrior.bitfont.TypesetString.Companion.newlineInts) {
                    prevLine[prevLine.size-1] = prevLine.last().copy(posAfter = lineStart)
                }
            }
            lineGlyphs.add(offsetGlyphs.toMutableList())
            lines.add(dev.thecodewarrior.bitfont.TypesetString.Line(
                i, y, maxAscent, maxDescent,
                offsetGlyphs.first().pos.x, offsetGlyphs.last().posAfter.x,
                emptyList()
            ))
            y += maxDescent + lineSpacing
        }

        // set the posAfter for trailing newlines
        lineGlyphs.lastOrNull()?.also { prevLine ->
            if(prevLine.last().codepoint in dev.thecodewarrior.bitfont.TypesetString.Companion.newlineInts) {
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

    protected open fun layoutRun(line: Int, range: IntRange): List<dev.thecodewarrior.bitfont.TypesetString.GlyphRender> {
        val list = mutableListOf<dev.thecodewarrior.bitfont.TypesetString.GlyphRender>()
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
                    dev.thecodewarrior.bitfont.TypesetString.GlyphRender(codepointIndices[it], it, codepoints[it], line,
                        font, glyph,
                        Vec2i(cursor, 0), Vec2i(cursor + advance, 0),
                        attributedString.getAttributes(codepointIndices[it]))
                )
                cursor += advance
            } else {
                val cls = combiningClass
                val rectMin = combiningRectMin
                val rectMax = combiningRectMax
                val gapX = if(cls.attached || cls.yAlign != CombiningClass.YAlignment.CENTER) 0 else combiningGap
                val gapY = if(cls.attached && cls.yAlign != CombiningClass.YAlignment.CENTER) 0 else combiningGap

                val newX = when(cls.xAlign) {
                    CombiningClass.XAlignment.LEFT -> {
                        rectMin.x - glyph.bearingX - glyph.image.width - gapX
                    }
                    CombiningClass.XAlignment.LEFT_CORNER -> {
                        rectMin.x - glyph.bearingX
                    }
                    CombiningClass.XAlignment.CENTER -> {
                        rectMin.x - glyph.bearingX + (rectMax.x - rectMin.x - glyph.image.width)/2
                    }
                    CombiningClass.XAlignment.RIGHT_CORNER -> {
                        rectMax.x - glyph.bearingX - glyph.image.width
                    }
                    CombiningClass.XAlignment.RIGHT -> {
                        rectMax.x - glyph.bearingX + gapX
                    }
                    CombiningClass.XAlignment.DOUBLE -> {
                        rectMax.x - glyph.bearingX - (glyph.image.width)/2
                    }
                    else -> rectMin.x
                }
                val newY: Int
                when(cls.yAlign) {
                    CombiningClass.YAlignment.ABOVE -> {
                        newY = rectMin.y - gapY - glyph.image.height - glyph.bearingY
                        combiningRectMin -= Vec2i(0, glyph.image.height + gapY)
                    }
                    CombiningClass.YAlignment.CENTER -> {
                        newY = rectMin.y - glyph.bearingY + (rectMax.y - rectMin.y - glyph.image.height)/2
                    }
                    CombiningClass.YAlignment.BELOW -> {
                        newY = rectMax.y + gapY - glyph.bearingY
                        combiningRectMax += Vec2i(0, glyph.image.height + gapY)
                    }
                }

                list.add(
                    dev.thecodewarrior.bitfont.TypesetString.GlyphRender(codepointIndices[it], it, codepoints[it], line,
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
            if(codepoints[i] in dev.thecodewarrior.bitfont.TypesetString.Companion.newlineInts) {
                val isCrBeforeLf = codepoints[i] == 0x000D && codepoints.getOrNull(i+1) == 0x000A
                if(isCrBeforeLf)
                    addBreak(i+2)
                else
                    addBreak(i+1)
            } else {
                val combiningClass = CombiningClass[UCharacter.getCombiningClass(codepoints[i])]
                if(combiningClass == CombiningClass.NOT_REORDERED ||
                    combiningClass.xAlign == CombiningClass.XAlignment.LEFT ||
                    combiningClass.xAlign == CombiningClass.XAlignment.RIGHT)
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
        val font: dev.thecodewarrior.bitfont.data.Bitfont, val glyph: dev.thecodewarrior.bitfont.data.Glyph,
        var pos: Vec2i, var posAfter: Vec2i, val attributes: AttributeMap) {
    }

    data class Line(
        val lineNumber: Int, val baseline: Int,
        val maxAscent: Int, val maxDescent: Int,
        val startX: Int, val endX: Int,
        val glyphs: List<dev.thecodewarrior.bitfont.TypesetString.GlyphRender>
    ) {
        var offset: Vec2i = Vec2i(0, 0)
            set(value) {
                val offsetDelta = value - field
                glyphs.forEach {
                    it.pos += offsetDelta
                    it.posAfter += offsetDelta
                }
                field = value
            }

        /**
         * Returns the closest character index to the passed X index
         */
        fun closestCharacter(x: Int): Pair<Int, Boolean> {
            if(glyphs.isEmpty())
                return 0 to false
            val glyph = glyphs.minBy { abs(x - it.pos.x) }
            val endDistance = abs(x - glyphs.last().posAfter.x)
            val glyphDistance = glyph?.let { abs(x - it.pos.x) }
            if(glyphDistance != null && glyphDistance < endDistance) {
                return glyph.characterIndex to false
            }
            return glyphs.last().characterIndex + 1 to true
        }
        fun containingCharacter(x: Int): Int {
            return glyphs.lastOrNull { it.pos.x <= x }?.characterIndex ?: 0
        }
    }

    companion object {
        val newlines = "\u000a\u000b\u000c\u000d\u0085\u2028\u2029"
        val newlineInts = dev.thecodewarrior.bitfont.TypesetString.Companion.newlines.chars().toList()
    }
}

