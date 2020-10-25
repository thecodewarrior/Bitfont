package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Glyph

/**
 * An object that can be laid out like text. The only built-in type of text object is [Glyph], with the abstract
 * [TextEmbed] class being another notable subclass.
 */
public interface TextObject {
    /**
     * The advance distance for this text object
     */
    public val advance: Int

    /**
     * The ascent of this text object above the baseline. This is used to calculate line heights when typesetting
     */
    public val ascent: Int

    /**
     * The ascent of this text object. This is used to calculate line heights when typesetting
     */
    public val descent: Int

    /**
     * The offset from this text object's position to the left side of its visual representation
     */
    public val bearingX: Int

    /**
     * The offset from the baseline to the top of this text object's visual representation
     */
    public val bearingY: Int

    /**
     * The width of this text object's visual representation
     */
    public val width: Int

    /**
     * The height of this text object's visual representation
     */
    public val height: Int
}