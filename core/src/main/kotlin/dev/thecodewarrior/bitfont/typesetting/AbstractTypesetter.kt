package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.CombiningClass
import kotlin.math.max
import kotlin.math.min

public class Typesetter(private val glyphs: BufferedIterator<AttributedGlyph>): BufferedIterator<GraphemeCluster>() {
    public var options: Options = Options()

    public data class Options(
        public val enableKerning: Boolean,
        public val enableCombiningCharacters: Boolean
    ) {
        public constructor() : this(false, true)
        // callbacks for more advanced behavior? (e.g. dynamic leading)
        // maybe a system to transform attributes? (e.g. bold -> leading++)
    }

    private var cursorX: Int = 0
    private var previousGlyph: GraphemeCluster? = null

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return

        var typesetGlyph = TypesetGlyph(cursorX, 0, glyphs.next())
        val previous = previousGlyph
        // only kern if neither glyph has an explicit advance width.
        if(options.enableKerning && previous != null &&
            previous.glyph.advance == null && typesetGlyph.glyph.advance == null) {
            val gap = getKernGap(previous, typesetGlyph) - TARGET_GAP
            if(gap > 0) {
                cursorX -= gap
                typesetGlyph = TypesetGlyph(cursorX, 0, typesetGlyph)
            }
        }

        val cluster = GraphemeCluster(typesetGlyph)

        if(options.enableCombiningCharacters)
            addAttachments(cluster)

        cursorX += cluster.glyph.calcAdvance()

        previousGlyph = cluster
        push(cluster)
    }

    /**
     * Consume any combining characters and add them to the passed grapheme cluster. Once a non-combining character
     * is found, this method returns without consuming it
     */
    fun addAttachments(cluster: GraphemeCluster) {
        // how far to space combining characters apart for this font
        val combiningGap = max(1, (cluster.glyph.font?.capHeight ?: 0) / 8)

        // the bounding box of the parent
        val parentTop = cluster.glyph.bearingY
        val parentHeight = cluster.glyph.image.height
        val parentLeft = cluster.glyph.bearingX
        val parentRight = cluster.glyph.bearingX + cluster.glyph.image.width

        // the attachment points, relative to the glyph origin
        // these will be updated as more characters are attached
        var aboveY = parentTop
        var belowY = parentTop + parentHeight

        while(glyphs.hasNext()) {
            val next = glyphs.peekNext()
            val combiningClass = CombiningClass[next.codepoint]
            if(combiningClass == CombiningClass.NOT_REORDERED)
                break

            val attachments = cluster.attachments ?: mutableListOf()
            cluster.attachments = attachments

            val attachment = glyphs.next()
            val attachmentWidth = attachment.glyph.image.width
            val attachmentHeight = attachment.glyph.image.height

            val attachmentX = when(combiningClass.xAlign) {
                CombiningClass.XAlignment.LEFT -> {
                    parentLeft - attachmentWidth - combiningGap
                }
                CombiningClass.XAlignment.LEFT_CORNER -> {
                    parentLeft
                }
                CombiningClass.XAlignment.CENTER -> {
                    parentLeft + (parentRight - parentLeft - attachmentWidth)/2
                }
                CombiningClass.XAlignment.RIGHT_CORNER -> {
                    parentRight - attachmentWidth
                }
                CombiningClass.XAlignment.RIGHT -> {
                    parentRight + combiningGap
                }
                CombiningClass.XAlignment.DOUBLE -> {
                    parentRight - (attachmentWidth)/2
                }
            }

            val attachmentY = when(combiningClass.yAlign) {
                CombiningClass.YAlignment.ABOVE -> {
                    aboveY -= attachmentHeight + combiningGap
                    aboveY
                }
                CombiningClass.YAlignment.CENTER -> {
                    parentTop + (parentHeight - attachmentHeight)/2
                }
                CombiningClass.YAlignment.BELOW -> {
                    belowY += attachmentHeight + combiningGap
                    belowY - attachmentHeight
                }
            }

            attachments.add(TypesetGlyph(
                attachmentX - attachment.glyph.bearingX,
                attachmentY - attachment.glyph.bearingY,
                attachment
            ))
        }
    }

    // kerning constants that need to be moved to the font
    private val Y_EXPANSION = 1
    private val TARGET_GAP = 2
    private val MAX_KERN_DEPTH = 3

    fun getKernGap(first: TypesetGlyph, second: TypesetGlyph): Int {
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

    fun getKernGap(first: TypesetGlyph, second: TypesetGlyph, row: Int): Int {
        // these should be parameters in the font
        val secondFontYExpansion = Y_EXPANSION
        val firstFontYExpansion = Y_EXPANSION
        return min(
            getProfileX(second, true, row, secondFontYExpansion) - getProfileX(first, false, row, 0),
            getProfileX(second, true, row, 0) - getProfileX(first, false, row, firstFontYExpansion)
        )
    }

    fun getProfileX(glyph: TypesetGlyph, left: Boolean, row: Int, yExpansion: Int): Int {
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

open class GraphemeCluster(
    posX: Int, posY: Int,
    codepoint: Int,
    glyph: Glyph,
    source: AttributedString,
    codepointIndex: Int,
    characterIndex: Int
): TypesetGlyph(posX, posY, codepoint, glyph, source, codepointIndex, characterIndex) {

    constructor(mainGlyph: TypesetGlyph): this(
        mainGlyph.posX,
        mainGlyph.posY,
        mainGlyph.codepoint,
        mainGlyph.glyph,
        mainGlyph.source,
        mainGlyph.codepointIndex,
        mainGlyph.characterIndex
    )

    constructor(posX: Int, posY: Int, cluster: GraphemeCluster): this(
        posX, posY,
        cluster.codepoint,
        cluster.glyph,
        cluster.source,
        cluster.codepointIndex,
        cluster.characterIndex
    ) {
        this.attachments = cluster.attachments?.toMutableList()
    }

    var attachments: MutableList<TypesetGlyph>? = null

    /**
     * Whether this glyph has any visible display. This returns true if the represented codepoint is whitespace and this
     * glyph has no [attachments]
     */
    val isInvisible: Boolean
        get() = UCharacter.isWhitespace(codepoint) && attachments.isNullOrEmpty()
}

public open class TypesetGlyph(
    public var posX: Int, public var posY: Int,
    codepoint: Int,
    glyph: Glyph,
    source: AttributedString,
    codepointIndex: Int,
    characterIndex: Int
): AttributedGlyph(codepoint, glyph, source, codepointIndex, characterIndex) {
    public constructor(posX: Int, posY: Int, attributedGlyph: AttributedGlyph): this(
        posX, posY,
        attributedGlyph.codepoint,
        attributedGlyph.glyph,
        attributedGlyph.source,
        attributedGlyph.codepointIndex,
        attributedGlyph.characterIndex
    )

    public val afterX: Int
        get() = posX + glyph.calcAdvance()
}
