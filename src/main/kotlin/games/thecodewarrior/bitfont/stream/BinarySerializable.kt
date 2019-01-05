package games.thecodewarrior.bitfont.stream

import games.thecodewarrior.bitfont.stream.ReadStream
import games.thecodewarrior.bitfont.stream.WriteStream

interface BinarySerializable {
    // constructor(stream: ReadStream)
    fun write(stream: WriteStream)
}