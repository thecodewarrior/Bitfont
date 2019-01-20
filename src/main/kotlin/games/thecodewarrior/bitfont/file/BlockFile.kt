package games.thecodewarrior.bitfont.file

import games.thecodewarrior.bitfont.stream.BinarySerializable
import games.thecodewarrior.bitfont.stream.IndexSize
import games.thecodewarrior.bitfont.stream.ReadStream
import games.thecodewarrior.bitfont.stream.WriteStream
import games.thecodewarrior.bitfont.utils.BitGrid
import java.util.TreeMap

class BlockFile(
    /**
     * The list of defined glyphs in this block. This list is sparse. For the full list of defined codepoints look in
     * [UCDFile].
     */
    var glyphs: TreeMap<UInt, Glyph> = TreeMap()
): BinarySerializable {

    constructor(stream: ReadStream) : this() {
        glyphs = stream.readObjectList<Glyph>(IndexSize.SHORT).associateByTo(TreeMap()) { it.codepoint }
    }

    override fun write(stream: WriteStream) {
        stream.writeObjectList(glyphs.values.toList(), IndexSize.SHORT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockFile) return false

        if (glyphs != other.glyphs) return false

        return true
    }

    override fun hashCode(): Int {
        return glyphs.hashCode()
    }

    class Glyph(
        /**
         * The codepoint number
         */
        var codepoint: UInt = 0u,
        /**
         * The distance between the cursor and the leading edge of this glyph's image
         */
        var bearingX: Short = 0,
        /**
         * The distance between the baseline and the top of this glyph's image
         */
        var bearingY: Short = 0,
        /**
         * The distance the cursor moves along the baseline after placing this glyph
         */
        var advance: UShort = 0u,
        /**
         * The image for the glyph
         */
        var image: BitGrid = BitGrid(0, 0)
    ): BinarySerializable {

        constructor(stream: ReadStream) : this() {
            codepoint = stream.readUInt()
            bearingX = stream.readShort()
            bearingY = stream.readShort()
            advance = stream.readUShort()
            val width = stream.readUShort().toInt()
            val height = stream.readUShort().toInt()
            image = BitGrid(width, height)
            stream.readUBytes(image.data.size).copyInto(image.data)
        }

        override fun write(stream: WriteStream) {
            stream.writeUInt(codepoint)
            stream.writeShort(bearingX)
            stream.writeShort(bearingY)
            stream.writeUShort(advance)
            stream.writeUShort(image.width.toUShort())
            stream.writeUShort(image.height.toUShort())
            stream.writeUBytes(image.data)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Glyph) return false

            if (codepoint != other.codepoint) return false
            if (bearingX != other.bearingX) return false
            if (bearingY != other.bearingY) return false
            if (advance != other.advance) return false
            if (image != other.image) return false

            return true
        }

        override fun hashCode(): Int {
            var result = codepoint.hashCode()
            result = 31 * result + bearingX.hashCode()
            result = 31 * result + bearingY.hashCode()
            result = 31 * result + advance.hashCode()
            result = 31 * result + image.hashCode()
            return result
        }
    }
}