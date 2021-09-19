package dev.thecodewarrior.bitfont.utils

import kotlin.math.sqrt

public data class Vec2i(val x: Int, val y: Int) {
    public operator fun plus(other: Vec2i): Vec2i = Vec2i(x + other.x, y + other.y)
    public operator fun minus(other: Vec2i): Vec2i = Vec2i(x - other.x, y - other.y)
    public operator fun times(other: Vec2i): Vec2i = Vec2i(x * other.x, y * other.y)
    public operator fun times(other: Int): Vec2i = Vec2i(x * other, y * other)
    public fun length(): Double = sqrt((x * x + y * y).toDouble())
}

