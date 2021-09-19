package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.math

class Rect(val min: Vec2, val max: Vec2) {
    val width: Double
        get() = max.x - min.x
    val widthf: Float
        get() = width.toFloat()
    val widthi: Int
        get() = width.toInt()

    val height: Double
        get() = max.y - min.y
    val heightf: Float
        get() = height.toFloat()
    val heighti: Int
        get() = height.toInt()

}

@Suppress("NOTHING_TO_INLINE")
inline fun rect(minX: Number, minY: Number, maxX: Number, maxY: Number): Rect = Rect(vec(minX, minY), vec(maxX, maxY))
@Suppress("NOTHING_TO_INLINE")
inline fun rect(min: Vec2, maxX: Number, maxY: Number): Rect = Rect(min, vec(maxX, maxY))
@Suppress("NOTHING_TO_INLINE")
inline fun rect(minX: Number, minY: Number, max: Vec2): Rect = Rect(vec(minX, minY), max)
@Suppress("NOTHING_TO_INLINE")
inline fun rect(min: Vec2, max: Vec2): Rect = Rect(min, max)
