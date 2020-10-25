package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter

public class GraphemeCluster(public val main: TypesetGlyph) {
    /**
     * Any combining characters attached to [main]. These are positioned relative to [main].
     */
    public val attachments: MutableList<TypesetGlyph> = mutableListOf()

    /**
     * Whether this glyph has any visible display. This returns true if the represented codepoint is whitespace and this
     * glyph has no [attachments]
     */
    public val isInvisible: Boolean
        get() = UCharacter.isWhitespace(main.codepoint) && attachments.isEmpty()
}
