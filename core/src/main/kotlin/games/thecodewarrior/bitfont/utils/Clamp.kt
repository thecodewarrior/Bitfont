package games.thecodewarrior.bitfont.utils

import kotlin.math.max
import kotlin.math.min

internal fun Byte.clamp(min: Byte, max: Byte) = if(this < min) min else if(this > max) max else this
internal fun Short.clamp(min: Short, max: Short) = if(this < min) min else if(this > max) max else this
internal fun Int.clamp(min: Int, max: Int) = if(this < min) min else if(this > max) max else this
internal fun Float.clamp(min: Float, max: Float) = if(this < min) min else if(this > max) max else this
internal fun Double.clamp(min: Double, max: Double) = if(this < min) min else if(this > max) max else this

internal fun <T: Comparable<T>> T.clamp(min: T, max: T) = if(this < min) min else if(this > max) max else this

internal fun <T: Comparable<T>> min(a: T, b: T) = if(b < a) b else a
internal fun <T: Comparable<T>> max(a: T, b: T) = if(b > a) b else a
