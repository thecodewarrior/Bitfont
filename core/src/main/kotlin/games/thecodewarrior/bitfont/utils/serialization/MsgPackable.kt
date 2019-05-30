package games.thecodewarrior.bitfont.utils.serialization

import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

interface MsgPackable {
    fun pack(packer: MessagePacker)

    @JvmDefault
    fun packToBytes(): ByteArray {
        return packToBytes(::pack)
    }

    companion object {
        fun packToBytes(pack: (MessagePacker) -> Unit): ByteArray {
            val packer = MessagePack.newDefaultBufferPacker()
            pack(packer)
            return packer.toByteArray()
        }
    }
}

interface MsgUnpackable<T> {
    fun unpack(unpacker: MessageUnpacker): T

    @JvmDefault
    fun unpack(bytes: ByteArray): T {
        return unpack(bytes, ::unpack)
    }

    companion object {
        fun <T> unpack(bytes: ByteArray, unpack: (MessageUnpacker) -> T): T {
            val unpacker = MessagePack.newDefaultUnpacker(bytes)
            return unpack(unpacker)
        }
    }
}
