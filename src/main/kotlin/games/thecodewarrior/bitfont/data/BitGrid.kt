package games.thecodewarrior.bitfont.data

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonJson
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.utils.IndexColorModel
import games.thecodewarrior.bitfont.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.utils.serialization.JsonWritable
import glm_.vec2.Vec2i
import jdk.nashorn.internal.ir.annotations.Ignore
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.Arrays
import kotlin.math.max

class BitGrid(val width: Int, val height: Int): JsonWritable<JsonObject> {
    val size = Vec2i(width, height)
    val data = UByteArray((width*height+7)/8) // (...+7)/8 rounds up

    operator fun get(pos: Vec2i): Boolean {
        if(pos.x < 0 || pos.x >= width)
            throw IndexOutOfBoundsException("Passed x coordinate is out of bounds. x = ${pos.x}, width = $width")
        if(pos.y < 0 || pos.y >= height)
            throw IndexOutOfBoundsException("Passed y coordinate is out of bounds. y = ${pos.y}, height = $height")
        val i = pos.x + pos.y*width
        val byteIndex = i / 8
        val bitIndex = i % 8
        return data[byteIndex].toUInt() and (1u shl bitIndex) != 0u
    }

    operator fun set(pos: Vec2i, value: Boolean) {
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

    fun getImage(background: Color, foreground: Color): BufferedImage {
        val colorModel = IndexColorModel(background, foreground)
        val image = BufferedImage(max(1, width), max(1, height), BufferedImage.TYPE_BYTE_BINARY, colorModel)
        for(x in 0 until width) {
            for(y in 0 until height) {
                if(this[Vec2i(x, y)])
                    image.setRGB(x, y, foreground.rgb)
            }
        }
        return image
    }

    override fun writeJson(): JsonObject = json {
        obj(
            "size" to array(width, height),
            "data" to data.joinToString("") { it.toString(16).padStart(2, '0') }
        )
    }

    companion object: JsonReadable<JsonObject, BitGrid> {
        override fun readJson(j: JsonObject): BitGrid {
            val size = j.array<Int>("size")!!
            val grid = BitGrid(size[0], size[1])
            j.string("data")!!.chunkedSequence(2).map { it.toUByte(16) }.forEachIndexed { i, v -> grid.data[i] = v }
            return grid
        }
    }
}