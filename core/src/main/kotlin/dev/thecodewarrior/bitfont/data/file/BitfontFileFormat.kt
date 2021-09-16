package dev.thecodewarrior.bitfont.data.file

import dev.thecodewarrior.bitfont.data.Bitfont
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import java.lang.UnsupportedOperationException

public abstract class BitfontFileFormat {
    /**
     * True if this format is no longer writable (e.g. because the internal data structure has changed incompatibly)
     */
    public abstract val readOnly: Boolean

    public abstract fun packTables(font: Bitfont, file: BitfontFile)
    public abstract fun unpackTables(file: BitfontFile): Bitfont

    public inline fun packToBytes(block: MessagePacker.() -> Unit): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()
        block(packer)
        return packer.toByteArray()
    }

    public inline fun <T> MessageUnpacker.unpack(block: MessageUnpacker.() -> T): T {
        return this.block()
    }

    public inline fun MessagePacker.pack(block: MessagePacker.() -> Unit) {
        this.block()
    }

    public companion object {
        private val magic = "BITFONT"
        private val magicBytes = magic.toByteArray()
        private val formats: List<BitfontFileFormat> = listOf(Version0, BitfontFormatV1)

        /**
         * Writes the specified font to a new [BitfontFile]
         *
         * @param font The font to write to the packer
         * @param version A specific file version to write with. Some versions may support read-only
         */
        @JvmStatic
        @JvmOverloads
        public fun pack(font: Bitfont, version: Int = formats.lastIndex): BitfontFile {
            if (version !in 1..formats.lastIndex) {
                throw FileFormatException("Can't write file version $version. Only versions in the range " +
                    "[1, ${formats.lastIndex}] are supported")
            }
            val format = formats[version]
            if(format.readOnly) {
                throw FileFormatException("Can't write file version $version. That version is now read-only")
            }

            val file = BitfontFile(version)
            format.packTables(font, file)

            return file
        }

        /**
         * Reads a font from the passed [MessageUnpacker].
         */
        @JvmStatic
        public fun unpack(file: BitfontFile): Bitfont {
            if (file.version !in 1..formats.lastIndex) {
                throw FileFormatException("Unsupported file version ${file.version}. Only versions in the range " +
                    "[1, ${formats.lastIndex}] are supported")
            }
            val format = formats[file.version]

            return format.unpackTables(file)
        }

        // because for some reason I started versions at 1, and I don't want to special-case a -1 in the array index
        private object Version0: BitfontFileFormat() {
            override val readOnly: Boolean = false

            override fun packTables(font: Bitfont, file: BitfontFile) {
                throw UnsupportedOperationException("Version 0 is invalid")
            }

            override fun unpackTables(file: BitfontFile): Bitfont {
                throw UnsupportedOperationException("Version 0 is invalid")
            }
        }

    }

    /**
     * A packer/unpacker that modifies an existing value when unpacking
     */
    public interface MutableFormat<T> {
        public fun pack(packer: MessagePacker, value: T)
        public fun unpack(unpacker: MessageUnpacker, value: T)

        public fun packToBytes(value: T): ByteArray {
            val packer = MessagePack.newDefaultBufferPacker()
            pack(packer, value)
            return packer.toByteArray()
        }
    }

    /**
     * A packer/unpacker that modifies an existing value when unpacking and receives a context
     */
    public interface ContextAwareMutableFormat<T, C> {
        public fun pack(packer: MessagePacker, value: T, context: C)
        public fun unpack(unpacker: MessageUnpacker, value: T, context: C)

        public fun packToBytes(value: T, context: C): ByteArray {
            val packer = MessagePack.newDefaultBufferPacker()
            pack(packer, value, context)
            return packer.toByteArray()
        }
    }

    /**
     * A packer/unpacker that creates a new value when unpacking
     */
    public interface ImmutableFormat<T> {
        public fun pack(packer: MessagePacker, value: T)
        public fun unpack(unpacker: MessageUnpacker): T

        public fun packToBytes(value: T): ByteArray {
            val packer = MessagePack.newDefaultBufferPacker()
            pack(packer, value)
            return packer.toByteArray()
        }
    }

    /**
     * A packer/unpacker that creates a new value when unpacking and receives a context
     */
    public interface ContextAwareImmutableFormat<T, C> {
        public fun pack(packer: MessagePacker, value: T, context: C)
        public fun unpack(unpacker: MessageUnpacker, context: C): T

        public fun packToBytes(value: T, context: C): ByteArray {
            val packer = MessagePack.newDefaultBufferPacker()
            pack(packer, value, context)
            return packer.toByteArray()
        }
    }
}
