package games.thecodewarrior.bitfont.utils

import java.util.Collections
import java.util.NavigableMap
import java.util.TreeMap

interface RangeMap<K: Comparable<K>, V> {
    val entries: Collection<Entry<K, V>>

    operator fun get(key: K): V?
    operator fun set(min: K, max: K, value: V?)

    fun clear(min: K, max: K)

    fun copy(): RangeMap<K, V>
    /**
     * Copies this map, applying the passed transform to all keys
     */
    fun copy(transform: (K) -> K): RangeMap<K, V>

    /**
     * Shifts the passed range by passing each key through the passed transform. Anything in the destination range
     * will be removed in this process.
     */
    fun shift(min: K, max: K, transform: (K) -> K)

    interface Entry<K: Comparable<K>, V> {
        val start: K
        val end: K
        val value: V

        @JvmDefault
        operator fun component1() = start
        @JvmDefault
        operator fun component2() = end
        @JvmDefault
        operator fun component3() = value
    }
}

class TreeRangeMap<K: Comparable<K>, V> private constructor(
    private val tree: NavigableMap<K, Entry<K, V>>
): RangeMap<K, V> {
    override val entries: Collection<RangeMap.Entry<K, V>> = Collections.unmodifiableCollection(tree.values)

    constructor(): this(TreeMap<K, Entry<K, V>>())

    override operator fun get(key: K): V? {
        return entryAt(key)?.value
    }

    override operator fun set(min: K, max: K, value: V?) {
        clear(min, max)

        if(value == null) return

        var mergedMin = min
        var mergedMax = max

        floorEntry(min)?.also {
            if(it.end == min && it.value == value) {
                removeEntry(it)
                mergedMin = it.start
            }
        }

        entryAt(max)?.also {
            if(it.value == value) {
                removeEntry(it)
                mergedMax = it.end
            }
        }

        insertEntry(Entry(mergedMin, mergedMax, value))
    }

    override fun clear(min: K, max: K) {
        slice(min)
        slice(max)
        tree.subMap(min, true, max, false).clear()
    }

    override fun shift(min: K, max: K, transform: (K) -> K) {
        slice(min)
        slice(max)

        val subMap = tree.subMap(min, true, max, false)
        val entries = subMap.values.toList()
        subMap.clear()
        clear(transform(min), transform(max))

        entries.map {
            this[transform(it.start), transform(it.end)] = it.value
        }
    }

    override fun copy(): RangeMap<K, V> = copy { it }
    override fun copy(transform: (K) -> K): RangeMap<K, V> {
        val newMap = TreeRangeMap<K, V>()
        tree.mapValuesTo(newMap.tree) { Entry(transform(it.value.start), transform(it.value.end), it.value.value) }
        return newMap
    }

    private fun slice(key: K) {
        val entry = entryAt(key) ?: return
        if(entry.start == key) return
        removeEntry(entry)
        insertEntry(Entry(entry.start, key, entry.value))
        insertEntry(Entry(key, entry.end, entry.value))
    }

    private fun removeEntry(entry: Entry<K, V>) {
        tree.remove(entry.start)
    }

    private fun insertEntry(entry: Entry<K, V>) {
        tree[entry.start] = entry
    }

    private fun floorEntry(key: K): Entry<K, V>? {
        return tree.floorEntry(key)?.value
    }

    private fun ceilingEntry(key: K): Entry<K, V>? {
        return tree.ceilingEntry(key)?.value
    }

    private fun entryAt(key: K): Entry<K, V>? {
        val entry = floorEntry(key) ?: return null
        if(key >= entry.end) return null
        return entry
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TreeRangeMap<*, *>) return false

        if (tree != other.tree) return false

        return true
    }

    override fun hashCode(): Int {
        return tree.hashCode()
    }

    class Entry<K: Comparable<K>, V>(
        override val start: K,
        override var end: K,
        override val value: V
    ): RangeMap.Entry<K, V> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry<*, *>) return false

            if (start != other.start) return false
            if (end != other.end) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = start.hashCode()
            result = 31 * result + end.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }
    }
}
