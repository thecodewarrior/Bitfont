package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont

public class TextAttribute<T> private constructor(public val name: String) {
    // here for ease of use in Java code
    public infix fun to(other: T): Pair<TextAttribute<T>, T> {
        return Pair(this, other)
    }

    public companion object {
        private val attributes = mutableMapOf<String, TextAttribute<*>>()

        @JvmStatic
        public fun <T> get(name: String): TextAttribute<T> {
            @Suppress("UNCHECKED_CAST")
            return attributes.getOrPut(name) {
                TextAttribute<Any>(name)
            } as TextAttribute<T>
        }

        public val font: TextAttribute<Bitfont> = get("font")

        /**
         * Add this to a character to give it custom rendering
         */
        public val textEmbed: TextAttribute<TextEmbed> = get("text_embed")
    }
}

public class AttributeMap() {
    public val map: MutableMap<TextAttribute<*>, Any> = mutableMapOf<TextAttribute<*>, Any>()

    public constructor(vararg attributes: Pair<TextAttribute<*>, Any>): this() {
        map.putAll(attributes)
    }
    public constructor(attributes: Map<TextAttribute<*>, Any>): this() {
        map.putAll(attributes)
    }

    @Suppress("UNCHECKED_CAST")
    public operator fun <T: Any> get(key: TextAttribute<T>): T? = map[key] as T?
    public operator fun <T: Any> set(key: TextAttribute<T>, value: T): Any? = put(key, value)

    @Suppress("UNCHECKED_CAST")
    public fun <T: Any> remove(key: TextAttribute<T>): T? = map.remove(key) as T?

    public fun <T: Any> put(key: TextAttribute<T>, value: T): Any? = map.put(key, value)

    public val size: Int
        get() = map.size

    public fun isEmpty(): Boolean = map.isEmpty()

    public val keys: MutableSet<TextAttribute<*>>
        get() = map.keys
    public val values: MutableCollection<Any>
        get() = map.values

    public fun clear() {
        map.clear()
    }

    public fun putAll(from: AttributeMap) {
        map.putAll(from.map)
    }
}
