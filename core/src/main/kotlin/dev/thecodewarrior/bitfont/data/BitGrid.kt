package dev.thecodewarrior.bitfont.data

import java.util.Arrays

public class BitGrid(public val width: Int, public val height: Int) {
    public val data: UByteArray = UByteArray((width*height+7)/8) // (...+7)/8 rounds up

    public fun isEmpty(): Boolean = data.all { it == 0.toUByte() }

    public operator fun get(x: Int, y: Int): Boolean {
        if(x < 0 || x >= width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = $x, width = $width")
        if(y < 0 || y >= height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = $y, height = $height")
        val i = x + y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        return data[byteIndex].toUInt() and (1u shl bitIndex) != 0u
    }

    public operator fun set(x: Int, y: Int, value: Boolean) {
        if(x !in 0 until width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = $x, width = $width")
        if(y !in 0 until height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = $y, height = $height")
        val i = x + y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        if(value) {
            data[byteIndex] = data[byteIndex] or (1u shl bitIndex).toUByte()
        } else {
            data[byteIndex] = data[byteIndex] and (1u shl bitIndex).inv().toUByte()
        }
    }

    public fun contains(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    /**
     * Sets an area starting at [pos] to the contents of the passed grid. Out-of-bounds elements will be ignored.
     */
    public fun draw(grid: BitGrid, posX: Int, posY: Int) {
        for(srcX in 0 until grid.width) {
            for(srcY in 0 until grid.height) {
                if(this.contains(srcX + posX, srcY + posY))
                    this[srcX + posX, srcY + posY] = grid[srcX, srcY]
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitGrid) return false

        if (width != other.width) return false
        if (height != other.height) return false
        if (Arrays.equals(data.toByteArray(), other.data.toByteArray())) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + Arrays.hashCode(data.toByteArray())
        return result
    }
}