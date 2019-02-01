package games.thecodewarrior.bitfont.editor.utils.extensions

import com.google.common.collect.Range

fun <T: Comparable<T>> ClosedRange<T>.toGuava(): Range<T> {
    return Range.closed(this.start, this.endInclusive)
}