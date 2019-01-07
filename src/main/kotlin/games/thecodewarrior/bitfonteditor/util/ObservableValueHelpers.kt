package games.thecodewarrior.bitfonteditor.util

import javafx.beans.value.ObservableValueBase
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun <T> observableValue(getter: () -> T): ObservableValueKt<T> {
    return ObservableValueKt(getter)
}

fun <T> mutableObservableValueOf(initialValue: T, alwaysFire: Boolean = false): MutableObservableValueKt<T> {
    var value = initialValue
    return MutableObservableValueKt(alwaysFire, {
        value
    }, {
        value = it
    })
}

fun <T> mutableObservableValueBy(property: KMutableProperty0<T>, alwaysFire: Boolean = false): MutableObservableValueKt<T> {
    return MutableObservableValueKt(alwaysFire, { property.get() }, { v -> property.set(v) })
}

class ObservableValueKt<T>(private val getter: () -> T): ObservableValueBase<T>(), ReadOnlyProperty<Any, T> {
    override fun getValue(): T {
        return getter()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    fun fire() {
        fireValueChangedEvent()
    }
}

class MutableObservableValueKt<T>(private val alwaysFire: Boolean, private val getter: () -> T, private val setter: (T) -> Unit)
    : ObservableValueBase<T>(), ReadWriteProperty<Any, T> {
    override fun getValue(): T {
        return getter()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if(alwaysFire || this.value != value)
            fireValueChangedEvent()
        setter(value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    fun fire() {
        fireValueChangedEvent()
    }
}
