package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.utils.Vec2i
import dev.thecodewarrior.bitfont.utils.clamp
import kotlin.math.max
import kotlin.math.min

class Glyph(var font: Bitfont?) {
    var bearingX: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var bearingY: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var bearing: Vec2i
        get() = Vec2i(bearingX, bearingY)
        set(value) {
            bearingX = value.x
            bearingY = value.y
        }
    var advance: Int? = null
        set(value) {
            field = value?.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }

    fun calcAdvance(): Int = calcAdvance(font?.spacing ?: 0)
    fun calcAdvance(spacing: Int): Int = advance ?: if (image.isEmpty()) 0 else bearingX + image.width + spacing

    var image: BitGrid = BitGrid(1, 1)

    fun isEmpty(): Boolean = image.isEmpty() && advance == null

    fun crop() {
        if(image.isEmpty())  {
            image = BitGrid(1, 1)
            bearingX = 0
            bearingY = 0
            return
        }
        var minX = Int.MAX_VALUE
        var maxX = 0
        var minY = Int.MAX_VALUE
        var maxY = 0
        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                if(image[x, y]) {
                    minX = min(minX, x)
                    maxX = max(maxX, x)
                    minY = min(minY, y)
                    maxY = max(maxY, y)
                }
            }
        }
        val grid = BitGrid(maxX - minX + 1, maxY - minY + 1)
        for(x in 0 until image.width) {
            for(y in 0 until image.height) {
                if(image[x, y]) {
                    grid[x - minX, y - minY] = true
                }
            }
        }
        image = grid
        bearingX += minX
        bearingY += minY
    }
}