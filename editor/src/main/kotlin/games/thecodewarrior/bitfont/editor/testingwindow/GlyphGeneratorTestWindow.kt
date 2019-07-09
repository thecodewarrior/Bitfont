package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.GlyphGenerator
import glm_.vec2.Vec2

class GlyphGeneratorTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Glyph Generator Test") {
    var text: AttributedString = AttributedString("")

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawCanvas() {
        val glyphs = GlyphGenerator(text, listOf(document.bitfont))

        val height = document.bitfont.ascent + document.bitfont.descent
        val columns = canvas.width.toInt() / (height * scale)
        val rows = canvas.height.toInt() / (height * scale)
        val min = canvas.min + Vec2(height/4, (document.bitfont.ascent * 1.5).toInt())

        for(i in 0 until columns) {
            val pos = min + Vec2(height, 0) * i * scale
            drawList.addLine(
                pos,
                pos + Vec2(0, canvas.height),
                Colors.yellow.u32,
                1f
            )
        }

        for(i in 0 until rows) {
            val pos = min + Vec2(0, height) * i * scale
            drawList.addLine(
                pos,
                pos + Vec2(canvas.width, 0),
                Colors.yellow.u32,
                1f
            )
        }


        for((i, glyph) in glyphs.withIndex()) {
            val line = i / columns + 1
            val column = i % columns
            glyph.draw(min + Vec2(column * height, line * height) * scale, scale, Colors.layoutTest.text.u32)
        }
    }
}