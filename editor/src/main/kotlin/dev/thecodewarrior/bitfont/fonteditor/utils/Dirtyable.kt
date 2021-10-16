package dev.thecodewarrior.bitfont.fonteditor.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Dirtyable {
    fun markDirty()

    fun <V> dirtying(initialValue: V): ReadWriteProperty<Any?, V> {
        return object : ReadWriteProperty<Any?, V> {
            var value: V = initialValue

            override fun getValue(thisRef: Any?, property: KProperty<*>): V = value
            override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
                val changed = this.value != value
                this.value = value
                if(changed) markDirty()
            }
        }
    }
}