package dev.thecodewarrior.bitfont.utils.extensions

//fun Byte.replace(src: Byte, dest: Byte): Byte = if(this == src) dest else this
//fun Short.replace(src: Short, dest: Short): Short = if(this == src) dest else this
//fun Int.replace(src: Int, dest: Int): Int = if(this == src) dest else this
//fun Long.replace(src: Long, dest: Long): Long = if(this == src) dest else this
//fun Float.replace(src: Float, dest: Float): Float = if(this == src) dest else this
//fun Double.replace(src: Double, dest: Double): Double = if(this == src) dest else this

@Suppress("NOTHING_TO_INLINE")
inline fun <T: Number> T.replace(src: T, dest: T): T = if(this == src) dest else this

