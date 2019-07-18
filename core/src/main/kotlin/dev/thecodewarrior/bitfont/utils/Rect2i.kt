package dev.thecodewarrior.bitfont.utils

import kotlin.math.sqrt

data class Rect2i(val x: Int, val y: Int, val width: Int, val height: Int) {
    val maxX: Int get() = x + width
    val maxY: Int get() = y + height
}

