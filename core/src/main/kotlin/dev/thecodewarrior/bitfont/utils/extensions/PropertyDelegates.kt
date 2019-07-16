package dev.thecodewarrior.bitfont.utils.extensions

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

internal operator fun <T> KMutableProperty0<T>.setValue(target: Any, property: KProperty<*>, value: T) = this.set(value)
internal operator fun <T> KProperty0<T>.getValue(target: Any, property: KProperty<*>): T = this.get()

