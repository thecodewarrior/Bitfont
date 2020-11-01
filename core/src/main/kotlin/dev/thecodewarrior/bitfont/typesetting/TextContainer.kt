package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.BufferedIterator

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
     * "fixes" the passed line fragment to allow text to wrap around exclusion areas. The same fragment may be sent
     * multiple times as taller characters are laid out.
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
        public val posX: Int, public val posY: Int,
        public val width: Int, public val height: Int,
        public val clusters: List<GraphemeCluster>
    ): Iterable<TypesetGlyph> {
        override fun iterator(): Iterator<TypesetGlyph> {
            return Iter()
        }

        private inner class Iter: BufferedIterator<TypesetGlyph>() {
            val clusterIterator = clusters.iterator()

            override fun refillBuffer() {
                if(clusterIterator.hasNext()) {
                    val cluster = clusterIterator.next()
                    push(cluster.main)
                    cluster.attachments.forEach {
                        push(it)
                    }
                }
            }
        }
    }
}