@file:Suppress("UnstableApiUsage")

package games.thecodewarrior.bitfont.typesetting

import com.google.common.collect.RangeMap
import com.google.common.collect.TreeRangeMap
import games.thecodewarrior.bitfont.utils.extensions.toGuava
import java.awt.Color

class AttributedString(val string: String) {
    private val attributes = mutableMapOf<Attribute<*>, TreeRangeMap<Int, Any>>()

    private fun attrMap(attr: Attribute<*>): TreeRangeMap<Int, Any> {
        @Suppress("UNCHECKED_CAST")
        return attributes.getOrPut(attr) { TreeRangeMap.create() }
    }

    fun setAttributesForRange(range: IntRange, values: Map<Attribute<*>, Any>) {
        values.forEach { key, value ->
            val map = attrMap(key)
            map.putCoalescing(range.toGuava(), value)
        }
    }

    fun getAttributesForRange(range: IntRange): Map<Attribute<*>, RangeMap<Int, Any>> {
        val subRanges = mutableMapOf<Attribute<*>, RangeMap<Int, Any>>()
        attributes.forEach { key, value ->
            val sub = value.subRangeMap(range.toGuava())
            try {
                sub.span()
                subRanges[key] = sub
            } catch(e: NoSuchElementException) {
                // empty. There seems to be no other way to check this than cause an exception
            }
        }
        return subRanges
    }

    fun attributesFor(character: Int): Map<Attribute<*>, Any> {
        val attrs = mutableMapOf<Attribute<*>, Any>()
        attributes.forEach { key, value ->
            value.get(character)?.also {
                attrs[key] = it
            }
        }
        return attrs
    }
}

class Attribute<T> private constructor(val name: String) {
    companion object {
        private val attributes = mutableMapOf<String, Attribute<*>>()

        fun <T> get(name: String): Attribute<T> {
            @Suppress("UNCHECKED_CAST")
            return attributes.getOrPut(name) {
                Attribute<Any>(name)
            } as Attribute<T>
        }

        val color = get<Color>("color")
    }
}