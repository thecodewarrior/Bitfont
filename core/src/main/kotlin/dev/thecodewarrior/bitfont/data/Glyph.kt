package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.typesetting.TextObject
import kotlin.math.max
import kotlin.math.min

public class Glyph(public val font: Bitfont): TextObject {
    public override var bearingX: Int = 0
    public override var bearingY: Int = 0
    public override var advance: Int = 0
    override val ascent: Int
        get() = font.ascent
    override val descent: Int
        get() = font.descent
    override val width: Int
        get() = image.width
    override val height: Int
        get() = image.height

    public var image: BitGrid = BitGrid(1, 1)

    public fun isEmpty(): Boolean = image.isEmpty()

    public fun crop() {
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