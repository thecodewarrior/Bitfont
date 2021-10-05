package dev.thecodewarrior.bitfont.fonteditor.utils

public data class Rect2i(val x: Int, val y: Int, val width: Int, val height: Int) {
    val minX: Int get() = x
    val minY: Int get() = y
    val maxX: Int get() = x + width
    val maxY: Int get() = y + height

    val xf: Float get() = x.toFloat()
    val yf: Float get() = y.toFloat()
    val xd: Double get() = x.toDouble()
    val yd: Double get() = y.toDouble()

    val widthf: Float get() = width.toFloat()
    val heightf: Float get() = height.toFloat()
    val widthd: Double get() = width.toDouble()
    val heightd: Double get() = height.toDouble()

    val minXf: Float get() = minX.toFloat()
    val minYf: Float get() = minY.toFloat()
    val minXd: Double get() = minX.toDouble()
    val minYd: Double get() = minY.toDouble()
    val maxXf: Float get() = maxX.toFloat()
    val maxYf: Float get() = maxY.toFloat()
    val maxXd: Double get() = maxX.toDouble()
    val maxYd: Double get() = maxY.toDouble()

    operator fun contains(vec: Vec2i): Boolean {
        return vec.x in minX .. maxX && vec.y in minY .. maxY
    }
}

