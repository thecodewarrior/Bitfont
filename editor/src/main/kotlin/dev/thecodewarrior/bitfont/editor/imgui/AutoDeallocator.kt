package dev.thecodewarrior.bitfont.editor.imgui

import org.ice1000.jimgui.NativeBool
import org.ice1000.jimgui.NativeDouble
import org.ice1000.jimgui.NativeFloat
import org.ice1000.jimgui.NativeInt
import org.ice1000.jimgui.NativeLong
import org.ice1000.jimgui.NativeShort
import org.ice1000.jimgui.cpp.DeallocatableObject
import java.lang.IllegalArgumentException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

open class AutoDeallocator {
    private val objects = mutableListOf<DeallocatableObject>()

    fun finalize() {
        objects.forEach { it.deallocateNativeObject() }
    }

    fun nativeBool(): NativeBool = NativeBool().also { objects.add(it) }
    fun nativeDouble(): NativeDouble = NativeDouble().also { objects.add(it) }
    fun nativeFloat(): NativeFloat = NativeFloat().also { objects.add(it) }
    fun nativeInt(): NativeInt = NativeInt().also { objects.add(it) }
    fun nativeLong(): NativeLong = NativeLong().also { objects.add(it) }
    fun nativeShort(): NativeShort = NativeShort().also { objects.add(it) }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> native(): ReadWriteProperty<Any?, T> {
        when(T::class) {
            Boolean::class -> {
                val native = nativeBool()
                return object : NativeDelegate<Boolean>(native) {
                    override fun getValue(): Boolean = native.accessValue()
                    override fun setValue(value: Boolean) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            Double::class -> {
                val native = nativeDouble()
                return object : NativeDelegate<Double>(native) {
                    override fun getValue(): Double = native.accessValue()
                    override fun setValue(value: Double) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            Float::class -> {
                val native = nativeFloat()
                return object : NativeDelegate<Float>(native) {
                    override fun getValue(): Float = native.accessValue()
                    override fun setValue(value: Float) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            Int::class -> {
                val native = nativeInt()
                return object : NativeDelegate<Int>(native) {
                    override fun getValue(): Int = native.accessValue()
                    override fun setValue(value: Int) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            Long::class -> {
                val native = nativeLong()
                return object : NativeDelegate<Long>(native) {
                    override fun getValue(): Long = native.accessValue()
                    override fun setValue(value: Long) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            Short::class -> {
                val native = nativeShort()
                return object : NativeDelegate<Short>(native) {
                    override fun getValue(): Short = native.accessValue()
                    override fun setValue(value: Short) = native.modifyValue(value)
                } as ReadWriteProperty<Any?, T>
            }
            else -> throw IllegalArgumentException("Can't create a native ${T::class.simpleName}")
        }
    }

    @JvmName("getNativeBoolDelegate")
    fun native(property: KProperty0<Boolean>): NativeBool {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeBool
    }
    @JvmName("getNativeDoubleDelegate")
    fun native(property: KProperty0<Double>): NativeDouble {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeDouble
    }
    @JvmName("getNativeFloatDelegate")
    fun native(property: KProperty0<Float>): NativeFloat {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeFloat
    }
    @JvmName("getNativeIntDelegate")
    fun native(property: KProperty0<Int>): NativeInt {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeInt
    }
    @JvmName("getNativeLongDelegate")
    fun native(property: KProperty0<Long>): NativeLong {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeLong
    }
    @JvmName("getNativeShortDelegate")
    fun native(property: KProperty0<Short>): NativeShort {
        property.isAccessible = true
        return (property.getDelegate() as NativeDelegate<*>).native as NativeShort
    }

    abstract class NativeDelegate<T>(val native: Any): ReadWriteProperty<Any?, T> {
        abstract fun getValue(): T
        abstract fun setValue(value: T)

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setValue(value)
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
    }
}