package games.thecodewarrior.bitfonteditor.util

import java.util.Arrays
import java.lang.reflect.Method
import java.util.Collections
import java.util.HashSet
import java.util.LinkedList

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