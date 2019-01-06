package games.thecodewarrior.bitfonteditor.util

import griffon.core.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.codehaus.griffon.runtime.core.AbstractObservable
import java.lang.reflect.Method
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.reflect

fun <T> Observable.observable(initialValue: T): ReadWriteProperty<Observable, T> {
    return GriffonObservableDelegate(initialValue)
}

fun <T> Observable.lateObservable(): ReadWriteProperty<Observable, T> {
    return GriffonObservableDelegate()
}

class GriffonObservableDelegate<T>(): ReadWriteProperty<Observable, T> {
    private var initialized = false
    private var storage: T? = null

    constructor(value: T) : this() {
        initialized = true
        storage = value
    }

    override operator fun getValue(thisRef: Observable, property: KProperty<*>): T {
        if(!initialized)
            throw UninitializedPropertyAccessException("lateinit property " + property.name + " has not been initialized")
        @Suppress("UNCHECKED_CAST")
        return storage as T
    }

    override operator fun setValue(thisRef: Observable, property: KProperty<*>, value: T) {
        initialized = true
        val old = storage
        storage = value
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
