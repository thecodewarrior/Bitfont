package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.utils.Vec2i
import java.util.Arrays

class BitGrid(val width: Int, val height: Int) {
    val size = Vec2i(width, height)
    val data = UByteArray((width*height+7)/8) // (...+7)/8 rounds up

    fun isEmpty(): Boolean = data.all { it == 0.toUByte() }

    operator fun get(pos: Vec2i): Boolean {
        return this[pos.x, pos.y]
    }

    operator fun set(pos: Vec2i, value: Boolean) {
        this[pos.x, pos.y] = value
    }

    operator fun get(x: Int, y: Int): Boolean {
        if(x < 0 || x >= width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = $x, width = $width")
        if(y < 0 || y >= height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = $y, height = $height")
        val i = x + y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        return data[byteIndex].toUInt() and (1u shl bitIndex) != 0u
    }

    operator fun set(x: Int, y: Int, value: Boolean) {
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

    operator fun contains(pos: Vec2i): Boolean {
        return pos.x in 0 until width && pos.y in 0 until height
    }

    /**
     * Sets an area starting at [pos] to the contents of the passed grid. Out-of-bounds elements will be ignored.
     */
    fun draw(grid: BitGrid, pos: Vec2i) {
        for(x in 0 until grid.width) {
            for(y in 0 until grid.height) {
                val src = Vec2i(x, y)
                val dest = pos + src
                if(dest in this)
                    this[dest] = grid[src]
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