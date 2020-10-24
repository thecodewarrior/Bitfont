package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.Rect2i
import dev.thecodewarrior.bitfont.utils.Vec2i

public open class TextContainer @JvmOverloads constructor(public var width: Int, public var height: Int = Int.MAX_VALUE) {

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
    public open fun fixLineFragment(line: LineFragment) {
    }
}