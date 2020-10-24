package dev.thecodewarrior.bitfont.utils

/**
 * An iterator that allows you to push values back onto it to be re-iterated.
 */
public class PushBackIterator<T>(private val wrapped: Iterator<T>): Iterator<T> {
    private val buffer = ArrayDeque<T>()

    override fun hasNext(): Boolean {
        return buffer.isNotEmpty() || wrapped.hasNext()
    }

    override fun next(): T {
        if(buffer.isEmpty())
            return wrapped.next()
        else
            return buffer.removeLast()
    }

    fun peekNext(): T {
        val value = next()
        pushBack(value)
        return value
    }

    /**
     * Pushes values back to be re-iterated. The passed value will be re-iterated on a FIFO basis.
     */
    public fun pushBack(value: T) {
        buffer.addLast(value)
    }
}