package dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions

import dev.thecodewarrior.bitfont.utils.Vec2i
import kotlin.math.abs

fun Vec2i.lineTo(other: Vec2i): List<Vec2i> {
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
