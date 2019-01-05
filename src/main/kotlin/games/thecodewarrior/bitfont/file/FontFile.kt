package games.thecodewarrior.bitfont.file

import games.thecodewarrior.bitfont.stream.BinarySerializable
import games.thecodewarrior.bitfont.stream.IndexSize
import games.thecodewarrior.bitfont.stream.ReadStream
import games.thecodewarrior.bitfont.stream.WriteStream

class FontFile @JvmOverloads constructor(
    /**
     * The name of the font
     */
    var name: String = "",
    /**
     * The license the font falls under
     */
    var license: String = "",

    /**
     * The spacing between baselines
     */
    var lineHeight: UShort = 0u,
    /**
     * The ascender distance from the baseline
     */
    var ascender: UShort = 0u,
    /**
     * The height of capital letters (e.g. `X`)
     */
    var capHeight: UShort = 0u,
    /**
     * The height of lowercase letters (e.g. `x`)
     */
    var xHeight: UShort = 0u,
    /**
     * The depth of the descender from the baseline (positive)
     */
    var descender: UShort = 0u,

    /**
     * The blocks that are defined in this font
     */
    var blocks: MutableList<Block> = mutableListOf()
): BinarySerializable {

    constructor(stream: ReadStream) : this() {
        name = stream.readString(IndexSize.BYTE)
        license = stream.readString()

        lineHeight = stream.readUShort()
        ascender = stream.readUShort()
        capHeight = stream.readUShort()
        xHeight = stream.readUShort()
        descender = stream.readUShort()
        blocks = stream.readObjectList(IndexSize.SHORT)
    }

    override fun write(stream: WriteStream) {
        stream.writeString(name, IndexSize.BYTE)
        stream.writeString(license)

        stream.writeUShort(lineHeight)
        stream.writeUShort(ascender)
        stream.writeUShort(capHeight)
        stream.writeUShort(xHeight)
        stream.writeUShort(descender)
        stream.writeObjectList(blocks, IndexSize.SHORT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontFile) return false

        if (name != other.name) return false
        if (license != other.license) return false
        if (lineHeight != other.lineHeight) return false
        if (ascender != other.ascender) return false
        if (capHeight != other.capHeight) return false
        if (xHeight != other.xHeight) return false
        if (descender != other.descender) return false
        if (blocks != other.blocks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + license.hashCode()
        result = 31 * result + lineHeight.hashCode()
        result = 31 * result + ascender.hashCode()
        result = 31 * result + capHeight.hashCode()
        result = 31 * result + xHeight.hashCode()
        result = 31 * result + descender.hashCode()
        result = 31 * result + blocks.hashCode()
        return result
    }

    class Block @JvmOverloads constructor(
        /**
         * The name of the file in the blocks/ directory
         */
        var filename: String = "",
        /**
         * The minimum codepoint (inclusive)
         */
        var min: ULong = 0u,
        /**
         * The maximum codepoint (inclusive)
         */
        var max: ULong = 0u
    ): BinarySerializable {

        constructor(stream: ReadStream) : this() {
            filename = stream.readString(IndexSize.BYTE)
            min = stream.readULong()
            max = stream.readULong()
        }

        override fun write(stream: WriteStream) {
            stream.writeString(filename, IndexSize.BYTE)
            stream.writeULong(min)
            stream.writeULong(max)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Block) return false

            if (filename != other.filename) return false
            if (min != other.min) return false
            if (max != other.max) return false

            return true
        }

        override fun hashCode(): Int {
            var result = filename.hashCode()
            result = 31 * result + min.hashCode()
            result = 31 * result + max.hashCode()
            return result
        }
    }
}
