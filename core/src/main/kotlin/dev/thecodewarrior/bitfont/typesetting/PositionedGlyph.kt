package dev.thecodewarrior.bitfont.typesetting

/**
 * An absolutely positioned glyph
 */
public class PositionedGlyph(
    /**
     * The X position of this glyph. This position is absolute within its [TextContainer]. Moving "container" objects
     * like [GraphemeCluster] and [TextContainer.TypesetLine] just applies offsets to everything inside them.
     */
    public var posX: Int,
    /**
     * The X position of this glyph. This position is absolute within its [TextContainer]. Moving "container" objects
     * like [GraphemeCluster] and [TextContainer.TypesetLine] just applies offsets to everything inside them.
     */
    public var posY: Int,
    /**
     * The index of this codepoint in the [source] string
     */
    public val index: Int,
    /**
     * The codepoint at the index this glyph represents
     */
    public val codepoint: Int,
    /**
     * The text object. This is what defines layout and rendering parameters
     */
    public val textObject: TextObject,
    /**
     * The source string
     */
    public val source: AttributedString,
) {
    public constructor(glyph: GlyphGenerator.Glyph, posX: Int, posY: Int) : this(
        posX, posY,
        glyph.index, glyph.codepoint, glyph.textObject, glyph.source
    )

    /**
     * The index after this codepoint
     */
    public val afterIndex: Int = index + Character.charCount(codepoint)
    /**
     * The X position after this glyph
     */
    public val afterX: Int = posX + textObject.advance

    public operator fun <T> get(attr: TextAttribute<T>): T? = source[attr, index]
}
