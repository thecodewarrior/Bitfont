package games.thecodewarrior.bitfont.file

import games.thecodewarrior.bitfont.stream.BinarySerializable
import games.thecodewarrior.bitfont.stream.IndexSize
import games.thecodewarrior.bitfont.stream.ReadStream
import games.thecodewarrior.bitfont.stream.WriteStream

class UCDFile(
    /**
     * The unicode blocks
     */
    var blocks: MutableList<Block> = mutableListOf(),
    /**
     * The glyphs and their names
     */
    var codepoints: MutableList<Codepoint> = mutableListOf()
): BinarySerializable {
    constructor(stream: ReadStream) : this() {
        blocks = stream.readObjectList(IndexSize.SHORT)
        codepoints = stream.readObjectList(IndexSize.SHORT)
    }

    override fun write(stream: WriteStream) {
        stream.writeObjectList(blocks, IndexSize.SHORT)
        stream.writeObjectList(codepoints, IndexSize.SHORT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UCDFile) return false

        if (blocks != other.blocks) return false
        if (codepoints != other.codepoints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blocks.hashCode()
        result = 31 * result + codepoints.hashCode()
        return result
    }

    class Block(
        /**
         * The filename to use for this block
         */
        var filename: String = "",
        /**
         * The human-readable name of this block
         */
        var name: String = "",
        /**
         * The minimum codepoint in this block (inclusive)
         */
        var min: ULong = 0u,
        /**
         * The maximum codepoint in this block (inclusive)
         */
        var max: ULong = 0u
    ): BinarySerializable {
        constructor(stream: ReadStream) : this() {
            filename = stream.readString(IndexSize.BYTE)
            name = stream.readString(IndexSize.BYTE)
            min = stream.readULong()
            max = stream.readULong()
        }

        override fun write(stream: WriteStream) {
            stream.writeString(filename, IndexSize.BYTE)
            stream.writeString(name, IndexSize.BYTE)
            stream.writeULong(min)
            stream.writeULong(max)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Block) return false

            if (filename != other.filename) return false
            if (name != other.name) return false
            if (min != other.min) return false
            if (max != other.max) return false

            return true
        }

        override fun hashCode(): Int {
            var result = filename.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + min.hashCode()
            result = 31 * result + max.hashCode()
            return result
        }
    }

    class Codepoint(
        /**
         * The codepoint number
         */
        var codepoint: ULong = 0u,
        /**
         * The human-readable name of the codepoint
         */
        var name: String = ""
    ): BinarySerializable {
        constructor(stream: ReadStream) : this() {
            codepoint = stream.readULong()
            name = stream.readString(IndexSize.BYTE)
        }

        override fun write(stream: WriteStream) {
            stream.writeULong(codepoint)
            stream.writeString(name, IndexSize.BYTE)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Codepoint) return false

            if (codepoint != other.codepoint) return false
            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            var result = codepoint.hashCode()
            result = 31 * result + name.hashCode()
            return result
        }
    }
}
