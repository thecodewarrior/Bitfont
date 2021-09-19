package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.math

import dev.thecodewarrior.bitfont.utils.Vec2i

@Suppress("NOTHING_TO_INLINE")
class Vec2(val x: Double, val y: Double) {
    constructor(other: Vec2i): this(other.x.toDouble(), other.y.toDouble())
    val xf: Float get() = x.toFloat()
    val yf: Float get() = y.toFloat()
    val xi: Int get() = x.toInt()
    val yi: Int get() = y.toInt()

    operator fun plus(other: Vec2): Vec2 = Vec2(this.x + other.x, this.y + other.y)
    operator fun minus(other: Vec2): Vec2 = Vec2(this.x - other.x, this.y - other.y)
    operator fun times(other: Vec2): Vec2 = Vec2(this.x * other.x, this.y * other.y)
    operator fun div(other: Vec2): Vec2 = Vec2(this.x / other.x, this.y / other.y)
    inline operator fun times(other: Number): Vec2 = Vec2(this.x * other.toDouble(), this.y * other.toDouble())
    inline operator fun div(other: Number): Vec2 = Vec2(this.x / other.toDouble(), this.y / other.toDouble())
    operator fun unaryMinus(): Vec2 = Vec2(-this.x, -this.y)
}

@Suppress("NOTHING_TO_INLINE")
inline fun vec(x: Number, y: Number): Vec2 = Vec2(x.toDouble(), y.toDouble())
