package dev.thecodewarrior.bitfont.fonteditor.utils

import kotlin.math.abs
import kotlin.math.sqrt

public data class Vec2i(val x: Int, val y: Int) {
    val xf: Float get() = x.toFloat()
    val yf: Float get() = y.toFloat()
    val xd: Double get() = x.toDouble()
    val yd: Double get() = y.toDouble()

    public operator fun plus(other: Vec2i): Vec2i = Vec2i(x + other.x, y + other.y)
    public operator fun minus(other: Vec2i): Vec2i = Vec2i(x - other.x, y - other.y)
    public operator fun times(other: Vec2i): Vec2i = Vec2i(x * other.x, y * other.y)
    public operator fun times(other: Int): Vec2i = Vec2i(x * other, y * other)
    public fun length(): Double = sqrt((x * x + y * y).toDouble())

    fun lineTo(other: Vec2i): List<Vec2i> {
        val x1 = x
        val y1 = y
        val x2 = other.x
        val y2 = other.y

        var d = 0
        val dy = abs(y2 - y1)
        val dx = abs(x2 - x1)
        val dy2 = dy shl 1
        val dx2 = dx shl 1
        val ix = if (x1 < x2)  1 else -1
        val iy = if (y1 < y2)  1 else -1
        var xx = x1
        var yy = y1

        val list = mutableListOf<Vec2i>()
        if (dy <= dx) {
            while (true) {
                list.add(Vec2i(xx, yy))
                if (xx == x2) break
                xx += ix
                d  += dy2
                if (d > dx) {
                    yy += iy
                    d  -= dx2
                }
            }
        }
        else {
            while (true) {
                list.add(Vec2i(xx, yy))
                if (yy == y2) break
                yy += iy
                d  += dx2
                if (d > dy) {
                    xx += ix
                    d  -= dy2
                }
            }
        }
        return list
    }
}

