package games.thecodewarrior.bitfonteditor.util

import griffon.core.Observable
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.codehaus.griffon.runtime.core.AbstractObservable
import java.beans.PropertyChangeListener
import java.lang.reflect.Method
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.reflect

@Suppress("UNCHECKED_CAST")
fun <T: Observable, R> T.listen(property: KProperty1<T, R>, function: (old: R, new: R) -> Unit): Listener {
    val listener = PropertyChangeListener { e ->
        function(e.oldValue as R, e.newValue as R)
    }
    this.addPropertyChangeListener(property.name, listener)
    return Listener(this, property.name, listener)
}

class Listener(private val target: Observable, private val name: String, private val listener: PropertyChangeListener) {
    fun remove() {
        target.removePropertyChangeListener(name, listener)
    }
}

fun <T> Observable.observableBy(delegate: KMutableProperty0<T>): ReadWriteProperty<Observable, T> {
    return GriffonObservableDelegate(delegate)
}

fun <T> Observable.observable(initialValue: T): ReadWriteProperty<Observable, T> {
    return GriffonObservableDelegate(initialValue)
}

fun <T> Observable.lateObservable(): ReadWriteProperty<Observable, T> {
    return GriffonObservableDelegate()
}

private class GriffonObservableDelegate<T> private constructor(
    private var initialized: Boolean,
    private var storage: T?,
    private val delegate: Pair<() -> T, (T) -> Unit>?
): ReadWriteProperty<Observable, T> {

    constructor() : this(false, null, null)
    constructor(value: T) : this(true, value, null)
    constructor(property: KMutableProperty0<T>) : this(true, null, { property.get() } to { v -> property.set(v) })
    constructor(get: () -> T, set: (T) -> Unit) : this(true, null, get to set)

    override operator fun getValue(thisRef: Observable, property: KProperty<*>): T {
        if(!initialized)
            throw UninitializedPropertyAccessException("lateinit property " + property.name + " has not been initialized")
        @Suppress("UNCHECKED_CAST")
        return if(delegate != null) delegate.first() else storage as T
    }

    override operator fun setValue(thisRef: Observable, property: KProperty<*>, value: T) {
        initialized = true
        val old: T?
        if(delegate != null) {
            old = delegate.first()
            delegate.second(value)
        } else {
            old = if(initialized) storage else value
            storage = value
        }
        thisRef.(method(thisRef::class))(property.name, old, value)
    }

    private companion object {
        val fireMethods = mutableMapOf<KClass<*>, Observable.(name: String, oldValue: Any?, newValue: Any?) -> Unit>()

        fun method(clazz: KClass<*>): Observable.(name: String, oldValue: Any?, newValue: Any?) -> Unit {
            return fireMethods.getOrPut(clazz) {
                val func = clazz.java.getAnyMethod("firePropertyChange", String::class.java, Any::class.java, Any::class.java)

                func.isAccessible = true
                return@getOrPut { name, oldValue, newValue ->
                    func.invoke(this, name, oldValue, newValue)
                }
            }
        }

    }
}
