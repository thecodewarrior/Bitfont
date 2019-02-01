package games.thecodewarrior.bitfont.editor.data

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonReadable
import games.thecodewarrior.bitfont.editor.utils.serialization.JsonWritable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgPackable
import games.thecodewarrior.bitfont.editor.utils.serialization.MsgUnpackable
import glm_.func.common.clamp
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import kotlin.math.max
import kotlin.math.min

class Glyph(): JsonWritable<JsonObject>, MsgPackable {
    var bearingX: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var bearingY: Int = 0
        set(value) {
            field = value.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }
    var advance: Int? = null
        set(value) {
            field = value?.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        }

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

    override fun writeJson(): JsonObject = json {
        obj(
            "bearing" to array(bearingX, bearingY),
            "advance" to advance,
            "image" to image.writeJson()
        )
    }

    override fun pack(packer: MessagePacker) {
        packer.apply {
            packInt(bearingX)
            packInt(bearingY)
            advance.also {
                if(it == null)
                    packNil()
                else
                    packInt(it)
            }
            image.pack(packer)
        }
    }

    companion object: JsonReadable<JsonObject, Glyph>, MsgUnpackable<Glyph> {
        override fun readJson(j: JsonObject): Glyph {
            val glyph = Glyph()
            j.array<Int>("bearing")!!.also {
                glyph.bearingX = it[0]
                glyph.bearingY = it[1]
            }
            glyph.advance = j.int("advance")
            glyph.image = BitGrid.readJson(j.obj("image")!!)
            return glyph
        }

        override fun unpack(unpacker: MessageUnpacker): Glyph {
            unpacker.apply {
                val glyph = Glyph()
                glyph.bearingX = unpackInt()
                glyph.bearingY = unpackInt()
                glyph.advance = if (tryUnpackNil()) null else unpackInt()
                glyph.image = BitGrid.unpack(unpacker)
                return glyph
            }
        }
    }
}