package dev.thecodewarrior.bitfont.editor.utils

import java.util.Collections
import java.util.IdentityHashMap

object GlobalAllocations {
    private val objects: MutableSet<Freeable> = Collections.newSetFromMap(IdentityHashMap())

    fun remove(obj: Freeable) {
        objects.remove(obj)
    }

    fun add(obj: Freeable) {
        objects.add(obj)
    }

    fun add(block: () -> Unit) {
        this.add(object : Freeable {
            override fun free() {
                block()
            }
        })
    }

    @JvmStatic
    fun free() {
        objects.forEach { it.free() }
    }
}