package games.thecodewarrior.bitfont.editor.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.editor.utils.IndexColorModel
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonWritable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgPackable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgUnpackable
import glm_.vec2.Vec2i
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.Arrays
import kotlin.math.max

class BitGrid(val width: Int, val height: Int): JsonWritable<JsonObject>, MsgPackable {
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

    override fun pack(packer: MessagePacker) {
        packer.apply {
            packInt(width)
            packInt(height)
            writePayload(data.toByteArray())
        }
    }

    companion object: JsonReadable<JsonObject, BitGrid>, MsgUnpackable<BitGrid> {
        override fun readJson(j: JsonObject): BitGrid {
            val size = j.array<Int>("size")!!
            val grid = BitGrid(size[0], size[1])
            j.string("data")!!.chunkedSequence(2).map { it.toUByte(16) }.forEachIndexed { i, v -> grid.data[i] = v }
            return grid
        }

        override fun unpack(unpacker: MessageUnpacker): BitGrid {
            unpacker.apply {
                val width = unpackInt()
                val height = unpackInt()
                val grid = BitGrid(width, height)
                readPayload(grid.data.size).toUByteArray().copyInto(grid.data)
                return grid
            }
        }
    }
}