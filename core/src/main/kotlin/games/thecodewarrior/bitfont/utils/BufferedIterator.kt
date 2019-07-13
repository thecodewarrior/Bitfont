package games.thecodewarrior.bitfont.utils

abstract class BufferedIterator<T>: Iterator<T> {
    private val buffer = ArrayQueue<T>()
    val size: Int get() = buffer.size()

    abstract fun refillBuffer()

    open fun refillBufferIfNeeded() {
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

    fun peekNext(): T {
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
