package games.thecodewarrior.bitfont.editor.utils

import kotlin.math.max
import kotlin.math.min

fun Byte.clamp(min: Byte, max: Byte) = if(this < min) min else if(this > max) max else this
fun Short.clamp(min: Short, max: Short) = if(this < min) min else if(this > max) max else this
fun Int.clamp(min: Int, max: Int) = if(this < min) min else if(this > max) max else this
fun Float.clamp(min: Float, max: Float) = if(this < min) min else if(this > max) max else this
fun Double.clamp(min: Double, max: Double) = if(this < min) min else if(this > max) max else this

fun <T: Comparable<T>> T.clamp(min: T, max: T) = if(this < min) min else if(this > max) max else this
