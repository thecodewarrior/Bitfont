package games.thecodewarrior.bitfont.utils

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.IndexColorModel
import java.util.Arrays

class BitGrid(val width: Int, val height: Int) {
    val data = UByteArray((width*height+7)/8) // (...+7)/8 rounds up

    operator fun get(pos: Pos): Boolean {
        if(pos.x < 0 || pos.x >= width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = ${pos.x}, width = $width")
        if(pos.y < 0 || pos.y >= height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = ${pos.y}, height = $height")
        val i = pos.x + pos.y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        return data[byteIndex].toUInt() and (1u shl bitIndex) != 0u
    }

    operator fun set(pos: Pos, value: Boolean) {
        if(pos.x !in 0 until width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = ${pos.x}, width = $width")
        if(pos.y !in 0 until height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = ${pos.y}, height = $height")
        val i = pos.x + pos.y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        if(value) {
            data[byteIndex] = data[byteIndex] or (1u shl bitIndex).toUByte()
        } else {
            data[byteIndex] = data[byteIndex] and (1u shl bitIndex).inv().toUByte()
        }
    }

    operator fun contains(pos: Pos): Boolean {
        return pos.x in 0 until width && pos.y in 0 until height
    }

    /**
     * Sets an area starting at [pos] to the contents of the passed grid. Out-of-bounds elements will be ignored.
     */
    operator fun set(pos: Pos, grid: BitGrid) {
        for(x in 0 until grid.width) {
            for(y in 0 until grid.height) {
                val src = Pos(x, y)
                val dest = pos + src
                if(pos in this)
                    this[pos] = grid[src]
            }
        }
    }

    fun getImage(background: Color, foreground: Color): LinkedImage {
        return LinkedImage(this, background, foreground)
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

    class LinkedImage(val grid: BitGrid, val background: Color, val foreground: Color) {
        private val colorModel = IndexColorModel(background, foreground)
        private val image = BufferedImage(grid.width, grid.height, BufferedImage.TYPE_BYTE_BINARY, colorModel)
        private val imageData = (image.raster.dataBuffer as DataBufferByte).data.asUByteArray()

        fun updateImage() {
            grid.data.copyInto(imageData)
        }

        fun updateGrid() {
            imageData.copyInto(grid.data)
        }
    }
}