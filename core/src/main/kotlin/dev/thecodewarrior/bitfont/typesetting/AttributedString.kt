package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.RangeMap
import dev.thecodewarrior.bitfont.utils.TreeRangeMap

public open class AttributedString internal constructor(
    public open val plaintext: String,
    protected open val attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>
) {
    /**
     * A version that monotonically increases with each modification of this string. For non-mutable strings this
     * generally stays zero.
     */
    public var version: Int = 0
        protected set

    public constructor(other: AttributedString): this(
        other.plaintext,
        other.getAllAttributes()
    )
    public constructor(plaintext: String): this(plaintext, mutableMapOf())
    public constructor(plaintext: String, attributes: AttributeMap): this(plaintext, attributes.map.mapValues { (_, value) ->
        TreeRangeMap<Int, Any>().also { map -> map[0, plaintext.length] = value }
    })
    public constructor(plaintext: String, vararg attributes: Pair<TextAttribute<*>, Any>): this(plaintext, AttributeMap(*attributes))

    public val length: Int
        get() = plaintext.length
    public fun isEmpty(): Boolean = length == 0
    public fun isNotEmpty(): Boolean = !isEmpty()

    public open fun getAllAttributes(): Map<TextAttribute<*>, RangeMap<Int, Any>> {
        return attributes.mapValues { it.value.copy() }
    }

    public open fun getAttributes(index: Int): AttributeMap {
        val attrs = AttributeMap()
        attributes.forEach { key, value ->
            value[index]?.also {
                @Suppress("UNCHECKED_CAST")
                attrs[key as TextAttribute<Any>] = it
            }
        }
        return attrs
    }

    public open operator fun <T> get(attr: TextAttribute<T>, index: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return attributes[attr]?.get(index) as T?
    }

    public open fun substring(start: Int, end: Int): AttributedString {
        return AttributedString(
            plaintext.substring(start, end),
            attributes.mapValues {
                val map = it.value.copy { it - start }
                map.clear(Int.MIN_VALUE, 0)
                map.clear(end - start, Int.MAX_VALUE)
                map
            }
        )
    }

    public open fun staticCopy(): AttributedString {
        return AttributedString(this)
    }

    public open fun mutableCopy(): MutableAttributedString {
        return MutableAttributedString(this)
    }

    override fun toString(): String {
        return plaintext
    }

    /**
     * Companion object so conversion extensions can be made (e.g. `AttributedString.fromMC` for Minecraft formatting
     * -> AttributedString)
     */
    public companion object
}
