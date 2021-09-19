package dev.thecodewarrior.bitfont.data.file

import org.msgpack.core.MessageInsufficientBufferException
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import org.msgpack.core.buffer.MessageBuffer
import org.msgpack.core.buffer.MessageBufferInput
import java.io.InputStream
import java.lang.IllegalArgumentException

public class BitfontFile(public val version: Int) {
    public val tables: MutableMap<String, ByteArray> = mutableMapOf()

    /**
     * Creates a new table with the specified name using the passed block or throws if a table with the same name
     * already exists.
     */
    public inline fun createTable(name: String, block: MessagePacker.() -> Unit) {
        MessagePack.newDefaultBufferPacker().use {
            it.block()
            tables[name] = it.toByteArray()
        }
    }

    /**
     * Gets an unpacker for the specified table, or null if no such table exists
     */
    public fun getTable(name: String): MessageUnpacker? {
        return tables[name]?.let { MessagePack.newDefaultUnpacker(it) }
    }

    /**
     * Applies the passed block to an unpacker for the specified table or throws if the table doesn't exist
     *
     * @throws IllegalArgumentException if the specified table doesn't exist
     */
    public inline fun <T> useTable(name: String, block: MessageUnpacker.() -> T): T {
        return (getTable(name) ?: throw IllegalArgumentException("No such table '$name'")).use { it.block() }
    }

    public fun pack(packer: MessagePacker) {
        packer.apply {
            writePayload(magicBytes)
            packInt(version)

            val sorted = tables.entries.sortedBy { it.key }
            packArrayHeader(tables.size)
            sorted.forEach { (name, buffer) ->
                packString(name)
                packInt(buffer.size)
                writePayload(buffer)
            }
        }
    }

    public fun packToBytes(): ByteArray {
        MessagePack.newDefaultBufferPacker().use {
            pack(it)
            return it.toByteArray()
        }
    }

    public companion object {
        public val magic: String = "BITFONT"
        private val magicBytes: ByteArray = magic.toByteArray()

        @JvmStatic
        public fun unpack(unpacker: MessageUnpacker): BitfontFile {
            val fileMagic = (try {
                unpacker.readPayload(magicBytes.size)
            } catch (e: MessageInsufficientBufferException) {
                ByteArray(0)
            } catch (e: Exception) {
                throw e
            })!!

            if (!fileMagic.contentEquals(magicBytes))
                throw FileFormatException("Passed data is not a bitfont file. Missing magic constant `$magic` " +
                    "at the start of the file.")

            val version = unpacker.unpackInt()

            val file = BitfontFile(version)

            val tableCount = unpacker.unpackArrayHeader()
            repeat(tableCount) {
                file.tables[unpacker.unpackString()] = unpacker.readPayload(unpacker.unpackInt())
            }

            return file
        }

        @JvmStatic
        public fun unpack(bytes: ByteArray): BitfontFile = MessagePack.newDefaultUnpacker(bytes).use { unpack(it) }
        @JvmStatic
        public fun unpack(inputStream: InputStream): BitfontFile = MessagePack.newDefaultUnpacker(inputStream).use { unpack(it) }
    }
}