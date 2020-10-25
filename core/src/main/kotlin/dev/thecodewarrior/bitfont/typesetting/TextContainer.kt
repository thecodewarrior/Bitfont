package dev.thecodewarrior.bitfont.typesetting

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

//    var lineBreakMode: LineBreakMode = LineBreakMode.WRAP_WORDS

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
        public var posX: Int, public var posY: Int,
        public var width: Int, public var height: Int,
        public val glyphs: List<GraphemeCluster>
    )
}