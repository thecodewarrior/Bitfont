package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.BufferedIterator
import dev.thecodewarrior.bitfont.utils.CombiningClass
import kotlin.math.max

public class Typesetter(private val glyphs: BufferedIterator<TypesetGlyph>): BufferedIterator<GraphemeCluster>() {
    public var options: Options = Options()

    public data class Options(
        public val enableCombiningCharacters: Boolean
    ) {
        public constructor() : this(true)
        // callbacks for more advanced behavior? (e.g. dynamic leading)
        // maybe a system to transform attributes? (e.g. bold -> leading++)
    }

    private var cursorX: Int = 0
    private var previousGlyph: GraphemeCluster? = null

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return

        val typesetObject = glyphs.next()
        typesetObject.posX = cursorX

        val cluster = GraphemeCluster(typesetObject)

        if(options.enableCombiningCharacters)
            addAttachments(cluster)

        cursorX += cluster.main.advance

        previousGlyph = cluster
        push(cluster)
    }

    /**
     * Consume any combining characters and add them to the passed grapheme cluster. Once a non-combining character
     * is found, this method returns without consuming it
     */
    private fun addAttachments(cluster: GraphemeCluster) {
        // how far to space combining characters apart for this font
        val combiningGap = max(1, cluster.main.ascent / 8)

        // the bounding box of the parent
        val parentTop = cluster.main.bearingY
        val parentHeight = cluster.main.height
        val parentLeft = cluster.main.bearingX
        val parentRight = cluster.main.bearingX + cluster.main.width

        // the attachment points, relative to the glyph origin
        // these will be updated as more characters are attached
        var aboveY = parentTop
        var belowY = parentTop + parentHeight

        while(glyphs.hasNext()) {
            val next = glyphs.peekNext()
            val combiningClass = CombiningClass[next.codepoint]
            if(combiningClass == CombiningClass.NOT_REORDERED)
                break

            val attachment = glyphs.next()
            val attachmentWidth = attachment.width
            val attachmentHeight = attachment.height

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

            attachment.posX = cluster.main.posX + attachmentX - attachment.bearingX
            attachment.posY = cluster.main.posY + attachmentY - attachment.bearingY
            cluster.attachments.add(attachment)
        }
    }
}

