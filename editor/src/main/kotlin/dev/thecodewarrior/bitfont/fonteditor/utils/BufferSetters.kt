@file:Suppress("NOTHING_TO_INLINE")

package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.PointerBuffer
import org.lwjgl.system.Struct
import org.lwjgl.system.StructBuffer
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.ShortBuffer

inline operator fun ByteBuffer.set(index: Int, b: Byte): ByteBuffer = this.put(index, b)
inline operator fun ShortBuffer.set(index: Int, s: Short): ShortBuffer = this.put(index, s)
inline operator fun CharBuffer.set(index: Int, c: Char): CharBuffer = this.put(index, c)
inline operator fun IntBuffer.set(index: Int, i: Int): IntBuffer = this.put(index, i)
inline operator fun LongBuffer.set(index: Int, l: Long): LongBuffer = this.put(index, l)
inline operator fun FloatBuffer.set(index: Int, f: Float): FloatBuffer = this.put(index, f)
inline operator fun DoubleBuffer.set(index: Int, d: Double): DoubleBuffer = this.put(index, d)
inline operator fun PointerBuffer.set(index: Int, p: Long): PointerBuffer = this.put(index, p)
inline operator fun <T: Struct, B: StructBuffer<T, B>> B.set(index: Int, s: T): B = this.put(index, s)

