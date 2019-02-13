package games.thecodewarrior.bitfont.utils

class Attribute<T> private constructor(val name: String) {
    // here for ease of use in Java code
    infix fun to(other: T): Pair<Attribute<T>, T> {
        return Pair(this, other)
    }

    companion object {
        private val attributes = mutableMapOf<String, Attribute<*>>()

        fun <T> get(name: String): Attribute<T> {
            @Suppress("UNCHECKED_CAST")
            return attributes.getOrPut(name) {
                Attribute<Any>(name)
            } as Attribute<T>
        }
    }
}

class AttributeMap() {
    val map = mutableMapOf<Attribute<*>, Any>()

    constructor(vararg attributes: Pair<Attribute<*>, Any>): this() {
        map.putAll(attributes)
    }
    constructor(attributes: Map<Attribute<*>, Any>): this() {
        map.putAll(attributes)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T: Any> get(key: Attribute<T>): T? = map[key] as T?
    operator fun <T: Any> set(key: Attribute<T>, value: T): Any? = put(key, value)

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> remove(key: Attribute<T>): T? = map.remove(key) as T?

    fun <T: Any> put(key: Attribute<T>, value: T): Any? = map.put(key, value)

    val size: Int
        get() = map.size

    fun isEmpty(): Boolean = map.isEmpty()

    val keys: MutableSet<Attribute<*>>
        get() = map.keys
    val values: MutableCollection<Any>
        get() = map.values

    fun clear() = map.clear()

    fun putAll(from: AttributeMap) = map.putAll(from.map)

}
