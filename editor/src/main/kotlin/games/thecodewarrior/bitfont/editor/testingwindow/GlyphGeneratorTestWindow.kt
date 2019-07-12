package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.math.vec
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.GlyphGenerator

class GlyphGeneratorTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Glyph Generator Test") {
    var text: AttributedString = AttributedString("")

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawCanvas(imgui: ImGui) {
        val glyphs = GlyphGenerator(text, listOf(document.bitfont))

        val height = document.bitfont.ascent + document.bitfont.descent
        val columns = canvas.width.toInt() / (height * scale)
        val rows = canvas.height.toInt() / (height * scale)
        val min = canvas.min + vec(height/4, (document.bitfont.ascent * 1.5).toInt())

        for(i in 0 until columns) {
            val pos = min + vec(height, 0) * i * scale
            drawList.addLine(
                pos,
                pos + vec(0, canvas.height),
                Colors.yellow.rgb,
                1f
            )
        }

        for(i in 0 until rows) {
            val pos = min + vec(0, height) * i * scale
            drawList.addLine(
                pos,
                pos + vec(canvas.width, 0),
                Colors.yellow.rgb,
                1f
            )
        }


        for((i, glyph) in glyphs.withIndex()) {
            val line = i / columns + 1
            val column = i % columns
            glyph.draw(imgui, min + vec(column * height, line * height) * scale, scale, Colors.layoutTest.text.rgb)
        }
    }
}