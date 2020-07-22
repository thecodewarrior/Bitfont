package dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions

fun <K, V> MutableMap<K, V>.putAll(vararg values: Pair<K, V>): Unit {
    this.putAll(mapOf(*values))
}

fun <E> MutableCollection<E>.addAll(vararg elements: E): Boolean {
    return this.addAll(elements)
}

