package games.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter
import games.thecodewarrior.bitfont.utils.BufferedIterator
import games.thecodewarrior.bitfont.utils.CombiningClass
import kotlin.math.max

class Typesetter(val glyphs: BufferedIterator<AttributedGlyph>): BufferedIterator<GraphemeCluster>() {
    private var cursorX: Int = 0
    private var cursorY: Int = 0
    private var previousGlyph: GraphemeCluster? = null

    override fun refillBuffer() {
        if(!glyphs.hasNext())
            return

        val mainGlyph = glyphs.next()
        val cluster = GraphemeCluster(TypesetGlyph(cursorX, cursorY, mainGlyph))

        val combiningGap = max(1, (mainGlyph.glyph.font?.capHeight ?: 0) / 8)
        var top = mainGlyph.glyph.bearingY
        var bottom = mainGlyph.glyph.bearingY + mainGlyph.glyph.image.height
        val left = mainGlyph.glyph.bearingX
        val right = mainGlyph.glyph.bearingX + mainGlyph.glyph.image.width

        while(glyphs.hasNext()) {
            val next = glyphs.peekNext()
            val combiningClass = CombiningClass[UCharacter.getCombiningClass(next.codepoint)]
            if(combiningClass == CombiningClass.NOT_REORDERED)
                break

            val attachments = cluster.attachments ?: mutableListOf()
            cluster.attachments = attachments

            val attachment = glyphs.next()
            val width = attachment.glyph.image.width
            val height = attachment.glyph.image.height

            val cls = combiningClass
            val gapX = if(cls.attached || cls.yAlign != 0) 0 else combiningGap
            val gapY = if(cls.attached && cls.yAlign != 0) 0 else combiningGap

            val attachmentX = when(cls.xAlign) {
                -2 -> {
                    left - width - gapX
                }
                -1 -> {
                    left
                }
                0 -> {
                    left + (right - left - width)/2
                }
                1 -> {
                    right - width
                }
                2 -> {
                    right + gapX
                }
                3 -> {
                    right - (width)/2
                }
                else -> left
            } - attachment.glyph.bearingX

            var attachmentY: Int
            when(cls.yAlign) {
                -1 -> {
                    attachmentY = top - gapY - height
                    top -= height + gapY
                }
                0 -> {
                    attachmentY = top + (bottom - top - height)/2
                }
                1 -> {
                    attachmentY = bottom + gapY
                    bottom += height + gapY
                }
                else -> {
                    attachmentY = 0
                }
            }
            attachmentY -= attachment.glyph.bearingY

            attachments.add(TypesetGlyph(attachmentX, attachmentY, attachment))
        }

//        if(mainGlyph.glyph.advance != null) {
//            cursorX += mainGlyph.glyph.advance!!
//        } else {
        cursorX += mainGlyph.glyph.calcAdvance(mainGlyph.glyph.font?.spacing ?: 0)
//        }

        previousGlyph = cluster
        push(cluster)
    }
}

class GraphemeCluster(val mainGlyph: TypesetGlyph) {
    var attachments: MutableList<TypesetGlyph>? = null
}

data class TypesetGlyph(val posX: Int, val posY: Int, val attributedGlyph: AttributedGlyph)
