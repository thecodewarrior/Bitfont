package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.Rect2i
import dev.thecodewarrior.bitfont.utils.Vec2i

open class TextContainer {
    var size: Vec2i = Vec2i(0, 0)
    val exclusionPaths: MutableList<Any /*BezierPath*/> get() = TODO("not implemented")
    var lineBreakMode: LineBreakMode = LineBreakMode.WRAP_WORDS
    var maxLineCount: Int = Int.MAX_VALUE
    var lineFragmentPadding: Int = 1
    /**
     * True if this is a simple rectangle. This is to optimize calls to [createLineFragment]
     */
    var isSimpleRectangle: Boolean = true

    /**
     * https://developer.apple.com/documentation/uikit/nstextcontainer/1444555-linefragmentrect
     */
    fun createLineFragment(proposedRect: Rect2i/*, characterIndex: Int*/) {
        TODO("not implemented")
    }
}