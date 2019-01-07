package games.thecodewarrior.bitfonteditor.util

import java.util.Arrays
import java.lang.reflect.Method
import java.util.Collections
import java.util.HashSet
import java.util.LinkedList
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun Class<*>.getAnyMethod(name: String, vararg parameters: Class<*>): Method {
    val parameterList = parameters.toList()
    val allMethods = this.allMethods
    val func = allMethods.find {
        it.name == name &&
            it.parameterTypes.toList() == parameterList
    }

    return func
        ?: throw NoSuchMethodException("${this.name}.$name(${parameters.joinToString { it.name }})");
}

private val allMethodCache = mutableMapOf<Class<*>, List<Method>>()

val Class<*>.allMethods: List<Method>
    get() = allMethodCache.getOrPut(this) {
        val interfaces = mutableSetOf<Class<*>>()
        val allMethods = mutableSetOf<Method>()

        var clazz: Class<*>? = this
        while(clazz != null) {
            allMethods.addAll(clazz.declaredMethods)
            interfaces.addAll(clazz.interfaces)
            clazz = clazz.superclass
        }

        interfaces.forEach {
            allMethods.addAll(it.declaredMethods)
        }

        return Collections.unmodifiableList(allMethods.toList())
    }

operator fun <T> KProperty0<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return this.get()
}

operator fun <T> KMutableProperty0<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return this.get()
}

operator fun <T> KMutableProperty0<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.set(value)
}

operator fun <T, R> KProperty1<T, R>.getValue(thisRef: T, property: KProperty<*>): R {
    return this.get(thisRef)
}

operator fun <T, R> KMutableProperty1<T, R>.getValue(thisRef: T, property: KProperty<*>): R {
    return this.get(thisRef)
}

operator fun <T, R> KMutableProperty1<T, R>.setValue(thisRef: T, property: KProperty<*>, value: R) {
    this.set(thisRef, value)
}
