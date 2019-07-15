package games.thecodewarrior.bitfont.editor.imgui

import org.ice1000.jimgui.NativeBool
import org.ice1000.jimgui.NativeDouble
import org.ice1000.jimgui.NativeFloat
import org.ice1000.jimgui.NativeInt
import org.ice1000.jimgui.NativeLong
import org.ice1000.jimgui.NativeShort
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

inline fun withNative(value: Boolean, callback: (NativeBool) -> Unit): Boolean {
    val native = NativeBool()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

inline fun withNative(value: Double, callback: (NativeDouble) -> Unit): Double {
    val native = NativeDouble()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

inline fun withNative(value: Float, callback: (NativeFloat) -> Unit): Float {
    val native = NativeFloat()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

inline fun withNative(value: Int, callback: (NativeInt) -> Unit): Int {
    val native = NativeInt()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

inline fun withNative(value: Long, callback: (NativeLong) -> Unit): Long {
    val native = NativeLong()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

inline fun withNative(value: Short, callback: (NativeShort) -> Unit): Short {
    val native = NativeShort()
    native.modifyValue(value)
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}

// properties ==========================================================================================================

@JvmName("withNativeBoolean")
inline fun withNative(value: KProperty0<Boolean>, callback: (NativeBool) -> Unit): Boolean {
    val native = NativeBool()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Boolean>.withNative(callback: (NativeBool) -> Unit): Boolean = withNative(this, callback)

@JvmName("withNativeDouble")
inline fun withNative(value: KProperty0<Double>, callback: (NativeDouble) -> Unit): Double {
    val native = NativeDouble()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Double>.withNative(callback: (NativeDouble) -> Unit): Double = withNative(this, callback)

@JvmName("withNativeFloat")
inline fun withNative(value: KProperty0<Float>, callback: (NativeFloat) -> Unit): Float {
    val native = NativeFloat()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Float>.withNative(callback: (NativeFloat) -> Unit): Float = withNative(this, callback)

@JvmName("withNativeInt")
inline fun withNative(value: KProperty0<Int>, callback: (NativeInt) -> Unit): Int {
    val native = NativeInt()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Int>.withNative(callback: (NativeInt) -> Unit): Int = withNative(this, callback)

@JvmName("withNativeLong")
inline fun withNative(value: KProperty0<Long>, callback: (NativeLong) -> Unit): Long {
    val native = NativeLong()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Long>.withNative(callback: (NativeLong) -> Unit): Long = withNative(this, callback)

@JvmName("withNativeShort")
inline fun withNative(value: KProperty0<Short>, callback: (NativeShort) -> Unit): Short {
    val native = NativeShort()
    native.modifyValue(value.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    return newValue
}
inline fun KProperty0<Short>.withNative(callback: (NativeShort) -> Unit): Short = withNative(this, callback)

// mutable properties ==================================================================================================

@JvmName("withNativeBoolean")
inline fun withNative(property: KMutableProperty0<Boolean>, callback: (NativeBool) -> Unit) {
    val native = NativeBool()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeBooleanExt")
inline fun KMutableProperty0<Boolean>.withNative(callback: (NativeBool) -> Unit) = withNative(this, callback)

@JvmName("withNativeDouble")
inline fun withNative(property: KMutableProperty0<Double>, callback: (NativeDouble) -> Unit) {
    val native = NativeDouble()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeDoubleExt")
inline fun KMutableProperty0<Double>.withNative(callback: (NativeDouble) -> Unit) = withNative(this, callback)

@JvmName("withNativeFloat")
inline fun withNative(property: KMutableProperty0<Float>, callback: (NativeFloat) -> Unit) {
    val native = NativeFloat()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeFloatExt")
inline fun KMutableProperty0<Float>.withNative(callback: (NativeFloat) -> Unit) = withNative(this, callback)

@JvmName("withNativeInt")
inline fun withNative(property: KMutableProperty0<Int>, callback: (NativeInt) -> Unit) {
    val native = NativeInt()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeIntExt")
inline fun KMutableProperty0<Int>.withNative(callback: (NativeInt) -> Unit) = withNative(this, callback)

@JvmName("withNativeLong")
inline fun withNative(property: KMutableProperty0<Long>, callback: (NativeLong) -> Unit) {
    val native = NativeLong()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeLongExt")
inline fun KMutableProperty0<Long>.withNative(callback: (NativeLong) -> Unit) = withNative(this, callback)

@JvmName("withNativeShort")
inline fun withNative(property: KMutableProperty0<Short>, callback: (NativeShort) -> Unit) {
    val native = NativeShort()
    native.modifyValue(property.get())
    callback(native)
    val newValue = native.accessValue()
    native.deallocateNativeObject()
    property.set(newValue)
}
@JvmName("withNativeShortExt")
inline fun KMutableProperty0<Short>.withNative(callback: (NativeShort) -> Unit) = withNative(this, callback)
