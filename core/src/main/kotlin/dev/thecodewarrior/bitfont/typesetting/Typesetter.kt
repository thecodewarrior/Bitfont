package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.CombiningClass
import kotlin.math.max

public class Typesetter(private val glyphs: GlyphGenerator): BufferedIterator<GraphemeCluster>() {
    public var options: Options = Options()

    public data class Options(
        public val enableCombiningCharacters: Boolean
    ) {
        public constructor() : this(true)
        // callbacks for more advanced behavior? (e.g. dynamic leading)
        // maybe a system to transform attributes? (e.g. bold -> leading++)
    }

    private var cursorX: Int = 0

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return

        val glyph = glyphs.next()

        val cluster = GraphemeCluster(
            metrics = glyph.textObject,
            codepoint = glyph.codepoint,
            index = glyph.index,
            afterIndex = glyph.afterIndex,
            baselineStart = 0,
            baselineEnd = glyph.textObject.advance,
        )
        cluster.glyphs.add(PositionedGlyph(glyph, 0, 0))

        if(options.enableCombiningCharacters)
            addAttachments(cluster, glyph.textObject)

        cluster.offsetX(cursorX)
        cursorX += glyph.textObject.advance
        push(cluster)
    }

    /**
     * Consume any combining characters and add them to the passed grapheme cluster. Once a non-combining character
     * is found, this method returns without consuming it
     */
    private fun addAttachments(cluster: GraphemeCluster, main: TextObject) {
        // how far to space combining characters apart for this font
        val combiningGap = max(1, main.ascent / 8)

        // the bounding box of the parent
        val parentTop = main.bearingY
        val parentHeight = main.height
        val parentLeft = main.bearingX
        val parentRight = main.bearingX + main.width

        // the attachment points, relative to the glyph origin
        // these will be updated as more characters are attached
        var aboveY = parentTop
        var belowY = parentTop + parentHeight

        while(glyphs.hasNext()) {
            val next = glyphs.peekNext()
            val combiningClass = CombiningClass[next.codepoint]
            if(combiningClass == CombiningClass.NOT_REORDERED)
                break

            val glyph = glyphs.next()
            val attachmentWidth = glyph.textObject.width
            val attachmentHeight = glyph.textObject.height

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

            cluster.glyphs.add(PositionedGlyph(
                glyph,
                attachmentX - glyph.textObject.bearingX,
                attachmentY - glyph.textObject.bearingY
            ))
            cluster.afterIndex = max(cluster.afterIndex, glyph.afterIndex)
        }
    }
}

