@file:Suppress("UnstableApiUsage")

package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.RangeMap
import java.awt.Color

class AttributedString(val string: String) {
    private val attributes = mutableMapOf<Attribute<*>, RangeMap<Int, Any>>()

    private fun attrMap(attr: Attribute<*>): RangeMap<Int, Any> {
        @Suppress("UNCHECKED_CAST")
        return attributes.getOrPut(attr) { RangeMap() }
    }

    fun setAttributesForRange(range: IntRange, values: Map<Attribute<*>, Any>) {
        values.forEach { key, value ->
            val map = attrMap(key)
            map[range.start, range.endInclusive+1] = value
        }
    }

    fun attributesFor(character: Int): Map<Attribute<*>, Any> {
        val attrs = mutableMapOf<Attribute<*>, Any>()
        attributes.forEach { key, value ->
            value[character]?.also {
                attrs[key] = it
            }
        }
        return attrs
    }

    operator fun <T> get(attr: Attribute<T>, index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return attributes[attr]?.get(index) as T?
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
        val font = get<Bitfont>("font")
    }
}