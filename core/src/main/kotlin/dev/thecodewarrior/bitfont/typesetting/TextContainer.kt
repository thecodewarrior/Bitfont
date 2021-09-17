package dev.thecodewarrior.bitfont.typesetting

import java.util.Collections

public open class TextContainer @JvmOverloads constructor(
    /**
     * The width of the container
     */
    public var width: Int,
    /**
     * The height of the container. Defaults to [Int.MAX_VALUE]
     */
    public var height: Int = Int.MAX_VALUE,
    /**
     * The maximum number of lines that this container can have. Defaults to [Int.MAX_VALUE]
     */
    public var maxLines: Int = Int.MAX_VALUE
) {
    public val lines: MutableList<TypesetLine> = mutableListOf()

    /**
     * The index of the first character in this container.
     */
    public val startIndex: Int get() = lines.firstOrNull()?.startIndex ?: 0
    /**
     * The index after the last character in this container.
     */
    public val endIndex: Int get() = lines.lastOrNull()?.endIndex ?: 0

    public val glyphs: Sequence<PositionedGlyph>
        get() = lines.asSequence().flatMap { it.glyphs }

    /**
     * "fixes" the passed line fragment to allow text to wrap around exclusion areas. The same input fragment may be
     * sent multiple times as taller characters are laid out.
     *
     * ```
     *          input: |---------------|
     *      exclusion: ###            ##
     * modified input:   >|----------|<
     * ```
     */
    public open fun fixLineFragment(line: LineBounds) {
    }

    public data class LineBounds(val spacing: Int, var posX: Int, var posY: Int, var width: Int, val height: Int)

    public class TypesetLine(
        public val posX: Int, public val posY: Int, public val baseline: Int,
        public val width: Int, public val height: Int,
        public val clusters: MutableList<GraphemeCluster>
    ) {
        public val startIndex: Int get() = clusters.minOfOrNull { it.index } ?: 0
        public val endIndex: Int get() = clusters.maxOfOrNull { it.afterIndex } ?: 0
        public val baselineStart: Int get() = clusters.minOfOrNull { it.baselineStart } ?: 0
        public val baselineEnd: Int get() = clusters.maxOfOrNull { it.baselineEnd } ?: 0

        public val glyphs: Sequence<PositionedGlyph>
            get() = clusters.asSequence().flatMap { it.glyphs }
    }
}