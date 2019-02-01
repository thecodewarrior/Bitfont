package games.thecodewarrior.bitfont.editor.utils.serialization

import org.msgpack.core.MessagePacker
import org.msgpack.core.MessageUnpacker

interface JsonWritable<J> {
    fun writeJson(): J
}

interface JsonReadable<J, T> {
    fun readJson(j: J): T
}

interface MsgPackable {
    fun pack(packer: MessagePacker)
}

interface MsgUnpackable<T> {
    fun unpack(unpacker: MessageUnpacker): T
}
