package dev.thecodewarrior.bitfont.editor.jimgui.testingwindow

import dev.thecodewarrior.bitfont.editor.jimgui.BitfontDocument
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.utils.Colors
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.u32
import dev.thecodewarrior.bitfont.editor.jimgui.utils.math.vec
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.AbstractTestWindow
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator

class GlyphGeneratorTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Glyph Generator Test") {
    var text: AttributedString = AttributedString("")

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawCanvas(imgui: ImGui) {
        val glyphs = GlyphGenerator(text, document.bitfont)

        val size = document.bitfont.ascent + document.bitfont.descent + 4
        val columns = canvas.width.toInt() / (size * scale)
        val rows = canvas.height.toInt() / (size * scale)
        val min = canvas.min + vec(size/4, (document.bitfont.ascent * 1.5).toInt())

        /*
        for(i in 0 until columns) {
            val pos = min + vec(size, 0) * i * scale
            drawList.addLine(
                pos,
                pos + vec(0, canvas.height),
                Colors.yellow.u32,
                1f
            )
        }

        for(i in 0 until rows) {
            val pos = min + vec(0, size) * i * scale
            drawList.addLine(
                pos,
                pos + vec(canvas.width, 0),
                Colors.yellow.u32,
                1f
            )
        }
         */

        for((i, glyph) in glyphs.withIndex()) {
            val line = i / columns + 1
            val column = i % columns
//            glyph.draw(imgui, min + vec(column * size, line * size) * scale, scale, Colors.layoutTest.text.u32)
        }
    }
}