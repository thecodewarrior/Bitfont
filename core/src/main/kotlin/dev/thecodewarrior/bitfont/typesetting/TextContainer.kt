package dev.thecodewarrior.bitfont.typesetting

public interface TextContainer {
    /**
     * The width of the container
     */
    public var width: Int
    /**
     * The height of the container. Defaults to [Int.MAX_VALUE]
     */
    public var height: Int
    /**
     * The maximum number of lines that this container can have. Defaults to [Int.MAX_VALUE]
     */
    public var maxLines: Int

    public val lines: MutableList<TypesetLine>

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
        /**
         * The index of the line within its text container
         */
        public val lineIndex: Int,
        /**
         * The X position of the top-left corner of the line segment
         */
        public val posX: Int,
        /**
         * The Y position of the top-left corner of the line segment
         */
        public val posY: Int,
        /**
         * The Y offset of the baseline from the top of the line segment
         */
        public val baseline: Int,
        /**
         * The width of the line segment
         */
        public val width: Int,
        /**
         * The height of the line segment
         */
        public val height: Int,
        /**
         * The list of grapheme clusters in this line
         */
        public val clusters: MutableList<GraphemeCluster>
    ) {
        /**
         * The Y position of the baseline
         */
        public val baselineY: Int get() = posY + baseline
        /**
         * The line height above the baseline
         */
        public val ascent: Int get() = baseline
        /**
         * The line height below the baseline
         */
        public val descent: Int get() = height - baseline

        public val startIndex: Int get() = clusters.minOfOrNull { it.index } ?: 0
        public val endIndex: Int get() = clusters.maxOfOrNull { it.afterIndex } ?: 0
        public val endIndexNoNewline: Int get() = clusters.maxOfOrNull {
            if(it.codepoint == '\r'.code || it.codepoint == '\n'.code) 0 else it.afterIndex
        } ?: 0
        public val baselineStart: Int get() = clusters.minOfOrNull { it.baselineStart } ?: 0
        public val baselineEnd: Int get() = clusters.maxOfOrNull { it.baselineEnd } ?: 0

        public val glyphs: Sequence<PositionedGlyph>
            get() = clusters.asSequence().flatMap { it.glyphs }

        /**
         * Returns the index at the given column, including the "after the end" column. If the index is out of bounds
         * this will return null.
         */
        public fun columnOf(index: Int): Int? {
            clusters.forEachIndexed { column, cluster ->
                if(index in cluster.index until cluster.afterIndex) {
                    return column
                }
            }
            if(clusters.isNotEmpty() && index == clusters.last().afterIndex) {
                return clusters.size
            }
            return null
        }

        /**
         * Returns the index at the given column, including the "after the end" column. If the index is out of bounds
         * this will return null.
         */
        public fun indexAt(column: Int): Int? {
            if(column !in 0 .. clusters.size)
                return null
            if(column == clusters.size)
                return clusters.last().afterIndex
            return clusters[column].index
        }

        /**
         * Returns the X position at the given column, including the "after the end" column. If the index is out of
         * bounds this will return null.
         */
        public fun positionAt(column: Int): Int? {
            if(column !in 0 .. clusters.size)
                return null
            if(column == clusters.size)
                return clusters.last().baselineEnd
            return clusters[column].baselineStart
        }


    }
}