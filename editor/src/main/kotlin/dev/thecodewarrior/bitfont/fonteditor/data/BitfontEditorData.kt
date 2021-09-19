package dev.thecodewarrior.bitfont.fonteditor.data

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.data.file.BitfontFile
import dev.thecodewarrior.bitfont.data.file.BitfontFileFormat
import java.io.InputStream

class BitfontEditorData(val font: Bitfont) {
    companion object {
        fun open(stream: InputStream): BitfontEditorData {
            val file = BitfontFile.unpack(stream)
            val font = BitfontFileFormat.unpack(file)
            // custom data will be unpacked here
            return BitfontEditorData(font)
        }
    }
}