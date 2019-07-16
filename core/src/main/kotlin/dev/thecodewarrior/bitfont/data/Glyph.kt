package dev.thecodewarrior.bitfont.data

import dev.thecodewarrior.bitfont.utils.Vec2i
import dev.thecodewarrior.bitfont.utils.clamp
import dev.thecodewarrior.bitfont.utils.serialization.MsgPackable
import dev.thecodewarrior.bitfont.utils.serialization.MsgUnpackable
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import kotlin.math.max
import kotlin.math.min

class Glyph(var font: dev.thecodewarrior.bitfont.data.Bitfont?): MsgPackable {
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

    fun calcAdvance(spacing: Int): Int = advance ?: if (image.isEmpty()) 0 else bearingX + image.width + spacing

    var image: dev.thecodewarrior.bitfont.data.BitGrid = dev.thecodewarrior.bitfont.data.BitGrid(1, 1)

    fun isEmpty(): Boolean = image.isEmpty() && advance == null

    fun crop() {
        if(image.isEmpty())  {
            image = dev.thecodewarrior.bitfont.data.BitGrid(1, 1)
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
        val grid = dev.thecodewarrior.bitfont.data.BitGrid(maxX - minX + 1, maxY - minY + 1)
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

    override fun pack(packer: MessagePacker) {
        crop()
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

    companion object: MsgUnpackable<dev.thecodewarrior.bitfont.data.Glyph> {
        override fun unpack(unpacker: MessageUnpacker): dev.thecodewarrior.bitfont.data.Glyph {
            unpacker.apply {
                val glyph = dev.thecodewarrior.bitfont.data.Glyph(null)
                glyph.bearingX = unpackInt()
                glyph.bearingY = unpackInt()
                glyph.advance = if (tryUnpackNil()) null else unpackInt()
                glyph.image = dev.thecodewarrior.bitfont.data.BitGrid.Companion.unpack(unpacker)
                glyph.crop()
                return glyph
            }
        }
    }
}