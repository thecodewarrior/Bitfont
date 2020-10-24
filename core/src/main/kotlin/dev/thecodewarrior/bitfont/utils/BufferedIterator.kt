package dev.thecodewarrior.bitfont.utils

public abstract class BufferedIterator<T>: Iterator<T> {
    private val buffer = ArrayQueue<T>()
    protected val size: Int get() = buffer.size()

    protected abstract fun refillBuffer()

    protected open fun refillBufferIfNeeded() {
        if(buffer.isEmpty)
            refillBuffer()
    }

    protected fun push(value: T) {
        buffer.enqueue(value)
    }

    override fun hasNext(): Boolean {
        refillBufferIfNeeded()
        return !buffer.isEmpty
    }

    public fun peekNext(): T {
        refillBufferIfNeeded()
        if(buffer.isEmpty)
            throw NoSuchElementException()
        return buffer.peek()
    }

    override fun next(): T {
        refillBufferIfNeeded()
        if(buffer.isEmpty)
            throw NoSuchElementException()
        return buffer.dequeue()
    }
}
