package dev.thecodewarrior.bitfont.editor.jimgui.utils.math

class Vec4(val x: Double, val y: Double, val z: Double, val w: Double) {
    operator fun plus(other: Vec4): Vec4 = Vec4(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w)
    operator fun minus(other: Vec4): Vec4 = Vec4(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w)
    operator fun times(other: Vec4): Vec4 = Vec4(this.x * other.x, this.y * other.y, this.z * other.z, this.w * other.w)
    operator fun div(other: Vec4): Vec4 = Vec4(this.x / other.x, this.y / other.y, this.z / other.z, this.w / other.w)
    operator fun unaryMinus(): Vec4 = Vec4(-this.x, -this.y, -this.z, -this.w)
}

@Suppress("NOTHING_TO_INLINE")
inline fun vec(x: Number, y: Number, z: Number, w: Number): Vec4
    = Vec4(x.toDouble(), y.toDouble(), z.toDouble(), w.toDouble())
