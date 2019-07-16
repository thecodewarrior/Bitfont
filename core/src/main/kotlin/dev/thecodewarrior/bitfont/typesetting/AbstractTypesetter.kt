package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.CombiningClass
import kotlin.math.max
import kotlin.math.min

class Typesetter(val glyphs: BufferedIterator<dev.thecodewarrior.bitfont.typesetting.AttributedGlyph>): BufferedIterator<dev.thecodewarrior.bitfont.typesetting.GraphemeCluster>() {
    var options: dev.thecodewarrior.bitfont.typesetting.Typesetter.Options = dev.thecodewarrior.bitfont.typesetting.Typesetter.Options()

    class Options {
        var enableKerning: Boolean = true
        var enableCombiningCharacters: Boolean = true
    }

    private var cursorX: Int = 0
    private var cursorY: Int = 0
    private var previousGlyph: dev.thecodewarrior.bitfont.typesetting.GraphemeCluster? = null

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return

        var typesetGlyph = dev.thecodewarrior.bitfont.typesetting.TypesetGlyph(cursorX, cursorY, glyphs.next())
        val previous = previousGlyph
        // only kern if neither glyph has an explicit advance width.
        if(options.enableKerning && previous != null &&
            previous.glyph.advance == null && typesetGlyph.glyph.advance == null) {
            val gap = getKernGap(previous, typesetGlyph) - TARGET_GAP
            if(gap > 0) {
                cursorX -= gap
                typesetGlyph = dev.thecodewarrior.bitfont.typesetting.TypesetGlyph(cursorX, cursorY, typesetGlyph)
            }
        }

        val cluster = dev.thecodewarrior.bitfont.typesetting.GraphemeCluster(typesetGlyph)

        if(options.enableCombiningCharacters)
            addAttachments(cluster)

        cursorX += cluster.glyph.calcAdvance(cluster.glyph.font?.spacing ?: 0)

        previousGlyph = cluster
        push(cluster)
    }

    /**
     * Consume any combining characters and add them to the passed grapheme cluster. Once a non-combining character
     * is found, this method returns without consuming it
     */
    fun addAttachments(cluster: dev.thecodewarrior.bitfont.typesetting.GraphemeCluster) {
        // how far to space combining characters apart
        val combiningGap = max(1, (cluster.glyph.font?.capHeight ?: 0) / 8)

        // the attachment points, relative to the glyph origin
        var top = cluster.glyph.bearingY
        var bottom = cluster.glyph.bearingY + cluster.glyph.image.height
        val left = cluster.glyph.bearingX
        val right = cluster.glyph.bearingX + cluster.glyph.image.width

        while(glyphs.hasNext()) {
            val next = glyphs.peekNext()
            val combiningClass = CombiningClass[next.codepoint]
            if(combiningClass == CombiningClass.NOT_REORDERED)
                break

            val attachments = cluster.attachments ?: mutableListOf()
            cluster.attachments = attachments

            val attachment = glyphs.next()
            val width = attachment.glyph.image.width
            val height = attachment.glyph.image.height

            // "attached" glyphs have no space, so they merge with the glyph they're attached to.
            val gapX =
                if(combiningClass.attached || combiningClass.yAlign != CombiningClass.YAlignment.CENTER)
                    0
                else
                    combiningGap
            val gapY =
                if(combiningClass.attached && combiningClass.yAlign != CombiningClass.YAlignment.CENTER)
                    0
                else
                    combiningGap

            val attachmentX = when(combiningClass.xAlign) {
                CombiningClass.XAlignment.LEFT -> {
                    left - width - gapX
                }
                CombiningClass.XAlignment.LEFT_CORNER -> {
                    left
                }
                CombiningClass.XAlignment.CENTER -> {
                    left + (right - left - width)/2
                }
                CombiningClass.XAlignment.RIGHT_CORNER -> {
                    right - width
                }
                CombiningClass.XAlignment.RIGHT -> {
                    right + gapX
                }
                CombiningClass.XAlignment.DOUBLE -> {
                    right - (width)/2
                }
            } - attachment.glyph.bearingX

            val attachmentY = when(combiningClass.yAlign) {
                CombiningClass.YAlignment.ABOVE -> {
                    top -= height + gapY
                    top
                }
                CombiningClass.YAlignment.CENTER -> {
                    top + (bottom - top - height)/2
                }
                CombiningClass.YAlignment.BELOW -> {
                    bottom += height + gapY
                    bottom - height
                }
            } - attachment.glyph.bearingY

            attachments.add(dev.thecodewarrior.bitfont.typesetting.TypesetGlyph(attachmentX, attachmentY, attachment))
        }
    }

    // kerning constants that need to be moved to the font
    private val Y_EXPANSION = 1
    private val TARGET_GAP = 2
    private val MAX_KERN_DEPTH = 3

    fun getKernGap(first: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph, second: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph): Int {
        // these should be parameters in the font
        val secondFontYExpansion = Y_EXPANSION
        val firstFontYExpansion = Y_EXPANSION

        val top = min(
            first.posY + first.glyph.bearingY - firstFontYExpansion,
            second.posY + second.glyph.bearingY - secondFontYExpansion
        )

        val bottom = min(
            first.posY + first.glyph.bearingY + first.glyph.image.height + firstFontYExpansion,
            second.posY + second.glyph.bearingY + second.glyph.image.height + secondFontYExpansion
        )

        var minGap = Int.MAX_VALUE
        for(row in top .. bottom) {
            minGap = min(minGap, getKernGap(first, second, row))
        }
        return max(0, minGap)
    }

    fun getKernGap(first: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph, second: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph, row: Int): Int {
        // these should be parameters in the font
        val secondFontYExpansion = Y_EXPANSION
        val firstFontYExpansion = Y_EXPANSION
        return min(
            getProfileX(second, true, row, secondFontYExpansion) - getProfileX(first, false, row, 0),
            getProfileX(second, true, row, 0) - getProfileX(first, false, row, firstFontYExpansion)
        )
    }

    fun getProfileX(glyph: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph, left: Boolean, row: Int, yExpansion: Int): Int {
        val glyphRow = row - (glyph.posY + glyph.glyph.bearingY)
        val image = glyph.glyph.image
        val edge = if (left)
            glyph.posX + glyph.glyph.bearingX
        else
            glyph.posX + glyph.glyph.bearingX + image.width
        val maxKernDepth = min(image.width, MAX_KERN_DEPTH)
        val kernLimitX = if (left) edge + MAX_KERN_DEPTH else edge - MAX_KERN_DEPTH

        if ((glyphRow + yExpansion) < 0 || (glyphRow - yExpansion) >= image.height)
            return kernLimitX

        for (depth in 0 until maxKernDepth) {
            for (testRow in glyphRow - yExpansion..glyphRow + yExpansion) {
                if (testRow < 0 || testRow >= image.height)
                    continue

                if (left) {
                    if (image[depth, testRow]) {
                        return edge + depth
                    }
                } else {
                    if (image[image.width - 1 - depth, testRow]) {
                        return edge - depth
                    }
                }
            }
        }

        return kernLimitX
    }
}

open class GraphemeCluster(mainGlyph: dev.thecodewarrior.bitfont.typesetting.TypesetGlyph): dev.thecodewarrior.bitfont.typesetting.TypesetGlyph(
    mainGlyph.posX,
    mainGlyph.posY,
    mainGlyph.codepoint,
    mainGlyph.glyph,
    mainGlyph.source,
    mainGlyph.codepointIndex,
    mainGlyph.characterIndex
) {
    var attachments: MutableList<dev.thecodewarrior.bitfont.typesetting.TypesetGlyph>? = null
}

open class TypesetGlyph(
    val posX: Int, val posY: Int,
    codepoint: Int,
    glyph: dev.thecodewarrior.bitfont.data.Glyph,
    source: dev.thecodewarrior.bitfont.typesetting.AttributedString,
    codepointIndex: Int,
    characterIndex: Int
): dev.thecodewarrior.bitfont.typesetting.AttributedGlyph(codepoint, glyph, source, codepointIndex, characterIndex) {
    constructor(posX: Int, posY: Int, attributedGlyph: dev.thecodewarrior.bitfont.typesetting.AttributedGlyph): this(
        posX, posY,
        attributedGlyph.codepoint,
        attributedGlyph.glyph,
        attributedGlyph.source,
        attributedGlyph.codepointIndex,
        attributedGlyph.characterIndex
    )
}
