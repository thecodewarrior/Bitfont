package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter

public class GraphemeCluster(public val main: TypesetGlyph) {
    /**
     * Offset the X position of [main] and all its [attachments]
     */
    public fun offsetX(offset: Int) {
        main.posX += offset
        attachments.forEach {
            it.posX += offset
        }
    }

    /**
     * Offset the Y position of [main] and move all its [attachments]
     */
    public fun offsetY(offset: Int) {
        main.posY += offset
        attachments.forEach {
            it.posY += offset
        }
    }

    /**
     * Any combining characters attached to [main]. These are positioned absolutely, not relatively.
     */
    public val attachments: MutableList<TypesetGlyph> = mutableListOf()

    /**
     * Whether this glyph has any visible display. This returns true if the represented codepoint is whitespace and this
     * glyph has no [attachments]
     */
    public val isInvisible: Boolean
        get() = UCharacter.isWhitespace(main.codepoint) && attachments.isEmpty()
}
