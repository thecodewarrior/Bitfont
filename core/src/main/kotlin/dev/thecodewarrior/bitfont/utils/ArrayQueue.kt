package dev.thecodewarrior.bitfont.utils

import java.util.ConcurrentModificationException
import java.util.NoSuchElementException

/**
 * Source: https://codereview.stackexchange.com/a/175868
 */
class ArrayQueue<E>: Iterable<E> {
    @Suppress("UNCHECKED_CAST")
    private var array = arrayOfNulls<Any>(MINIMUM_CAPACITY) as Array<E>
    private var head: Int = 0
    private var tail: Int = 0
    private var size: Int = 0
    private var modificationCount: Int = 0

    val isEmpty: Boolean
        get() = size == 0

    private val isFull: Boolean
        get() = size == array.size

    fun size(): Int {
        return size
    }

    fun enqueue(element: E) {
        if (isFull) {
            resize(2 * array.size)
        }

        array[tail] = element
        tail = tail + 1 and array.size - 1
        size++
        modificationCount++
    }

    fun dequeue(): E {
        if (isEmpty) {
            throw RuntimeException("ArrayQueue is empty.")
        }

        if (size < array.size / 4 && size >= 2 * MINIMUM_CAPACITY) {
            resize(array.size / 2)
        }

        val element = array[head]
        head = head + 1 and array.size - 1
        size--
        modificationCount++
        return element
    }

    fun peek(): E {
        return array[head]
    }

    override fun iterator(): Iterator<E> {
        return ArrayQueueIterator()
    }

    override fun toString(): String {
        val sb = StringBuilder("[")
        var separator = ""

        for (i in 0 until size) {
            sb.append(separator)
            separator = ", "
            sb.append(array[head + i and array.size - 1])
        }

        return sb.append("] capacity = " + array.size).toString()
    }

    private fun resize(capacity: Int) {
        @Suppress("UNCHECKED_CAST")
        val newArray = arrayOfNulls<Any>(capacity) as Array<E>

        for (i in 0 until size) {
            newArray[i] = array[head + i and array.size - 1]
        }

        this.array = newArray
        this.head = 0
        this.tail = size
    }

    private inner class ArrayQueueIterator: Iterator<E> {

        private var iterated = 0
        private val expectedModificationException = this@ArrayQueue.modificationCount

        override fun hasNext(): Boolean {
            checkModificationCount()
            return iterated < size
        }

        override fun next(): E {
            if (!hasNext()) {
                throw NoSuchElementException(
                    "No more elements to iterate.")
            }

            return array[head + iterated++ and array.size - 1]
        }

        private fun checkModificationCount() {
            if (expectedModificationException != this@ArrayQueue.modificationCount) {
                throw ConcurrentModificationException()
            }
        }
    }

    companion object {
        private val MINIMUM_CAPACITY = 4
    }
}
