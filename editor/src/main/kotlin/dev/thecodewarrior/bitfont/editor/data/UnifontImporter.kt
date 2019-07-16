package dev.thecodewarrior.bitfont.editor.data

import dev.thecodewarrior.bitfont.data.BitGrid
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.data.Glyph
import java.io.File

object UnifontImporter {
    fun import(lines: Iterable<String>): dev.thecodewarrior.bitfont.data.Bitfont {
        val font = dev.thecodewarrior.bitfont.data.Bitfont(
            name = "Unifont",
            ascent = 14,
            descent = 2,
            capHeight = 10,
            xHeight = 8,
            spacing = 2
        )

        lines.forEach { line ->
            val (codepointString, glyphString) = line.split(":", limit = 2)
            val codepoint = codepointString.toInt(16)
            val width: Int
            if(glyphString.length == 32) {
                width = 8
            } else if(glyphString.length == 64) {
                width = 16
            } else {
                throw IllegalStateException("Illegal glyph data length of ${glyphString.length}")
            }
            val grid = dev.thecodewarrior.bitfont.data.BitGrid(width, 16)
            glyphString.chunkedSequence(2)
                .map {
                    // read a byte and put it at the top of an int, reverse the bits in that int, then take the byte back off the bottom
                    Integer.reverse(it.toInt(16) shl 24).toUByte()
                }
                .forEachIndexed { i, v -> grid.data[i] = v }
            val glyph = dev.thecodewarrior.bitfont.data.Glyph(font)
            glyph.image = grid
            glyph.bearingX = 0
            glyph.bearingY = -14
            glyph.advance = width + 1
            font.glyphs[codepoint] = glyph
        }

        return font
    }
}