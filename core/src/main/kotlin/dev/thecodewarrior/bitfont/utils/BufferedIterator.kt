package dev.thecodewarrior.bitfont.utils

public abstract class BufferedIterator<T>: Iterator<T> {
    private val buffer = ArrayDeque<T>()
    protected val size: Int get() = buffer.size

    protected abstract fun refillBuffer()

    protected open fun refillBufferIfNeeded() {
        if(buffer.isEmpty())
            refillBuffer()
    }

    protected fun push(value: T) {
        buffer.addLast(value)
    }

    override fun hasNext(): Boolean {
        refillBufferIfNeeded()
        return buffer.isNotEmpty()
    }

    public fun peekNext(): T {
        refillBufferIfNeeded()
        if(buffer.isEmpty())
            throw NoSuchElementException()
        return buffer.first()
    }

    override fun next(): T {
        refillBufferIfNeeded()
        if(buffer.isEmpty())
            throw NoSuchElementException()
        return buffer.removeFirst()
    }
}
