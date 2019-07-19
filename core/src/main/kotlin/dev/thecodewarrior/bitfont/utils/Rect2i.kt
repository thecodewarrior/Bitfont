package dev.thecodewarrior.bitfont.utils

import kotlin.math.sqrt

data class Rect2i(val x: Int, val y: Int, val width: Int, val height: Int) {
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

    val maxXf: Float get() = maxX.toFloat()
    val maxYf: Float get() = maxY.toFloat()
    val maxXd: Double get() = maxX.toDouble()
    val maxYd: Double get() = maxY.toDouble()
}

