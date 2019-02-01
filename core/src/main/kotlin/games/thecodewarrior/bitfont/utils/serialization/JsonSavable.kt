package games.thecodewarrior.bitfont.utils.serialization

import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

interface MsgPackable {
    fun pack(packer: MessagePacker)

    fun packToBytes(): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()
        pack(packer)
        return packer.toByteArray()
    }
}

interface MsgUnpackable<T> {
    fun unpack(unpacker: MessageUnpacker): T

    fun unpack(bytes: ByteArray): T {
        val unpacker = MessagePack.newDefaultUnpacker(bytes)
        return unpack(unpacker)
    }
}
