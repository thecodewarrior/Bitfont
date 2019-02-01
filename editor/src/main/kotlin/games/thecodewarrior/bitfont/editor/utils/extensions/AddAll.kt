package games.thecodewarrior.bitfont.editor.utils.extensions

fun <K, V> MutableMap<K, V>.putAll(vararg values: Pair<K, V>): Unit {
    this.putAll(mapOf(*values))
}

fun <E> MutableCollection<E>.addAll(vararg elements: E): Boolean {
    return this.addAll(elements)
}

