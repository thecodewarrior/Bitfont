package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.utils.AttributeMap
import games.thecodewarrior.bitfont.utils.RangeMap
import games.thecodewarrior.bitfont.utils.TreeRangeMap

open class AttributedString internal constructor(
    open val plaintext: String,
    protected open val attributes: Map<Attribute<*>, RangeMap<Int, Any>>
) {

    constructor(other: AttributedString): this(
        other.plaintext,
        other.getAllAttributes()
    )
    constructor(plaintext: String): this(plaintext, mutableMapOf())
    constructor(plaintext: String, attributes: AttributeMap): this(plaintext, attributes.map.mapValues { (_, value) ->
        TreeRangeMap<Int, Any>().also { map -> map[0, plaintext.length] = value }
    })
    constructor(plaintext: String, vararg attributes: Pair<Attribute<*>, Any>): this(plaintext, AttributeMap(*attributes))

    open fun getAllAttributes(): Map<Attribute<*>, RangeMap<Int, Any>> {
        return attributes.mapValues { it.value.copy() }
    }

    open fun getAttributes(index: Int): AttributeMap {
        val attrs = AttributeMap()
        attributes.forEach { key, value ->
            value[index]?.also {
                @Suppress("UNCHECKED_CAST")
                attrs[key as Attribute<Any>] = it
            }
        }
        return attrs
    }

    open operator fun <T> get(attr: Attribute<T>, index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return attributes[attr]?.get(index) as T?
    }

    open fun substring(start: Int, end: Int): AttributedString {
        return AttributedString(
            plaintext.substring(start, end),
            attributes.mapValues {
                val map = it.value.copy { it - start }
                map.clear(Int.MIN_VALUE, 0)
                map.clear(end-start, Int.MAX_VALUE)
                map
            }
        )
    }

    open fun staticCopy(): AttributedString {
        return AttributedString(this)
    }

    open fun mutableCopy(): MutableAttributedString {
        return MutableAttributedString(this)
    }

    override fun toString(): String {
        return plaintext
    }
}
