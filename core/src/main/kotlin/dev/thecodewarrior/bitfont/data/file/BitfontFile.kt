package dev.thecodewarrior.bitfont.data.file

import org.msgpack.core.MessageInsufficientBufferException
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker
import org.msgpack.core.buffer.MessageBuffer
import org.msgpack.core.buffer.MessageBufferInput
import java.io.InputStream
import java.lang.IllegalArgumentException

class BitfontFile(val version: Int) {
    val tables: MutableMap<String, MessageBuffer> = mutableMapOf()

    /**
     * Creates a new table with the specified name using the passed block or throws if a table with the same name
     * already exists.
     */
    inline fun createTable(name: String, block: MessagePacker.() -> Unit) {
        MessagePack.newDefaultBufferPacker().use {
            it.block()
            tables[name] = it.toMessageBuffer()
        }
    }

    /**
     * Gets an unpacker for the specified table, or null if no such table exists
     */
    fun getTable(name: String): MessageUnpacker? {
        return tables[name]?.let { MessagePack.newDefaultUnpacker(SingleMessageBufferInput(it)) }
    }

    /**
     * Applies the passed block to an unpacker for the specified table or throws if the table doesn't exist
     *
     * @throws IllegalArgumentException if the specified table doesn't exist
     */
    inline fun <T> useTable(name: String, block: MessageUnpacker.() -> T): T {
        return (getTable(name) ?: throw IllegalArgumentException("No such table '$name'")).use { it.block() }
    }

    fun pack(packer: MessagePacker) {
        packer.apply {
            writePayload(magicBytes)
            packInt(version)

            val sorted = tables.entries.sortedBy { it.key }
            packArrayHeader(tables.size)
            sorted.forEach { (name, buffer) ->
                packString(name)
                val data = buffer.toByteArray()
                packInt(data.size)
                writePayload(data)
            }
        }
    }

    fun packToBytes(): ByteArray {
        MessagePack.newDefaultBufferPacker().use {
            pack(it)
            return it.toByteArray()
        }
    }

    companion object {
        val magic = "BITFONT"
        val magicBytes = magic.toByteArray()

        @JvmStatic
        fun unpack(unpacker: MessageUnpacker): BitfontFile {
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
                file.tables[unpacker.unpackString()] = unpacker.readPayloadAsReference(unpacker.unpackInt())
            }

            return file
        }

        @JvmStatic
        fun unpack(bytes: ByteArray) = MessagePack.newDefaultUnpacker(bytes).use { unpack(it) }
        @JvmStatic
        fun unpack(inputStream: InputStream) = MessagePack.newDefaultUnpacker(inputStream).use { unpack(it) }
    }

    private class SingleMessageBufferInput(buffer: MessageBuffer): MessageBufferInput {
        var buffer: MessageBuffer? = buffer

        override fun next(): MessageBuffer? {
            return buffer.also { buffer = null }
        }

        override fun close() {
            // nop
        }
    }
}