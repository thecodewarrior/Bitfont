package games.thecodewarrior.bitfont.utils

import java.util.TreeMap
import java.util.TreeSet

class RangeMap<K: Comparable<K>, V: Any> {
    private val tree = TreeMap<K, RangeEntry<K, V>>()

    private class RangeEntry<K: Comparable<K>, V>(var end: K, val value: V)

    operator fun get(key: K): V? {
        val entry = tree.floorEntry(key) ?: return null
        if(key >= entry.value.end) return null
        return entry.value.value
    }

    operator fun set(min: K, max: K, value: V?) {
        var needsInsert = true
        @Suppress("NAME_SHADOWING") var max = max
        @Suppress("NAME_SHADOWING") var min = min

        val entryAfter = tree.floorEntry(max)
        val entryBefore = tree.floorEntry(min)
        if(entryBefore != null && entryBefore.value.end >= min) tree.remove(entryBefore.key)
        tree.subMap(min, true, max, true).clear()

        if(entryAfter == null) {
            if(value != null) tree[min] = RangeEntry(max, value)
            return
        }
        if(entryAfter.key <= min && entryAfter.value.end >= max && entryAfter.value.value == value) return

        if(entryBefore.value.end >= min) {
            if(entryBefore.value.value == value) {
                min = entryBefore.key
            } else {
                tree[entryBefore.key] = RangeEntry(min, entryBefore.value.value)
            }
        }
        if(entryAfter.value.end > max) {
            if(entryAfter.value.value == value) {
                max = entryAfter.value.end
            } else {
                tree[max] = RangeEntry(entryAfter.value.end, entryAfter.value.value)
            }
        }
        if(value != null) tree[min] = RangeEntry(max, value)
    }

    fun remove(min: K, max: K) {
        this[min, max] = null
    }
}
