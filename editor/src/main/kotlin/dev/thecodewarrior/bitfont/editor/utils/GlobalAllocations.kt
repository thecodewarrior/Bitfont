package dev.thecodewarrior.bitfont.editor.utils

import org.lwjgl.system.NativeResource
import java.util.Collections
import java.util.IdentityHashMap

object GlobalAllocations {
    private val objects: MutableSet<Freeable> = Collections.newSetFromMap(IdentityHashMap())
    private val resources: MutableSet<NativeResource> = Collections.newSetFromMap(IdentityHashMap())

    fun <T: Freeable> remove(obj: T): T {
        objects.remove(obj)
        return obj
    }

    fun <T: Freeable> add(obj: T): T {
        objects.add(obj)
        return obj
    }

    fun <T: NativeResource> remove(obj: T): T {
        resources.remove(obj)
        return obj
    }

    fun <T: NativeResource> add(obj: T): T {
        resources.add(obj)
        return obj
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
        resources.forEach { it.free() }
    }
}