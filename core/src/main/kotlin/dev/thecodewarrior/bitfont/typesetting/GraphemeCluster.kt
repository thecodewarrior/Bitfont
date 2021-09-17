package dev.thecodewarrior.bitfont.typesetting

import com.ibm.icu.lang.UCharacter

/**
 * Keeps track of the "logical" position of a group of glyphs, and acts as a container for a set of positioned glyphs.
 *
 * One assumption we make is that the cluster contains a continuous range of codepoints.
 */
public class GraphemeCluster(
    /**
     * The metrics for this cluster
     */
    public val metrics: TextObject,
    /**
     * The base codepoint this cluster represents
     */
    public val codepoint: Int,
    /**
     * The index of the first glyph in this cluster
     */
    public val index: Int,
    /**
     * The index after the last glyph in this cluster
     */
    public var afterIndex: Int,
    /**
     * The position of this cluster along the baseline.
     */
    public var baselineStart: Int,
    /**
     * The position after this cluster along the baseline
     */
    public var baselineEnd: Int,
) {
    /**
     * The glyphs in this cluster.
     */
    public val glyphs: MutableList<PositionedGlyph> = mutableListOf()

    /**
     * Offset the X position of [baselineStart], [baselineEnd] and all the glyphs in this cluster
     */
    public fun offsetX(offset: Int) {
        baselineStart += offset
        baselineEnd += offset
        for (it in glyphs) {
            it.posX += offset
        }
    }

    /**
     * Offset the Y position of all the glyphs in this cluster
     */
    public fun offsetY(offset: Int) {
        for (it in glyphs) {
            it.posY += offset
        }
    }

    /**
     * Returns true if all the glyphs in the cluster are whitespace.
     */
    public val isBlank: Boolean
        get() = glyphs.all { UCharacter.isWhitespace(it.codepoint) }
}
