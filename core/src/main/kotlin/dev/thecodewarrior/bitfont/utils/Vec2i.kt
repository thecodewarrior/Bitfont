package dev.thecodewarrior.bitfont.utils

import kotlin.math.sqrt

data class Vec2i(val x: Int, val y: Int) {
    operator fun plus(other: Vec2i): Vec2i {
        return Vec2i(x + other.x, y + other.y)
    }

    operator fun minus(other: Vec2i): Vec2i {
        return Vec2i(x - other.x, y - other.y)
    }

    operator fun times(other: Vec2i): Vec2i {
        return Vec2i(x * other.x, y * other.y)
    }

    operator fun times(other: Int): Vec2i {
        return Vec2i(x * other, y * other)
    }

    fun length() = sqrt((x * x + y * y).toDouble())
}

