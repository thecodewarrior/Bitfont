package games.thecodewarrior.bitfont.utils

class Attribute<T> private constructor(val name: String) {
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
