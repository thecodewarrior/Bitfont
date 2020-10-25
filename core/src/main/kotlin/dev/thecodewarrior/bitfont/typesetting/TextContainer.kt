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

//    val exclusionPaths: MutableList<Any /*BezierPath*/> get() = TODO("not implemented")
//    var lineBreakMode: LineBreakMode = LineBreakMode.WRAP_WORDS
//    var maxLineCount: Int = Int.MAX_VALUE
//    /**
//     * True if this is a simple rectangle. This is to optimize calls to [createLineFragment]
//     */
//    var isSimpleRectangle: Boolean = true

    public val lines: MutableList<LineFragment> = mutableListOf()

//    /**
//     * https://developer.apple.com/documentation/uikit/nstextcontainer/1444555-linefragmentrect
//     */
//    fun createLineFragment(proposedRect: Rect2i/*, characterIndex: Int*/) {
//        TODO("not implemented")
//    }

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
}