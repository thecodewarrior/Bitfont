package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.math.vec
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.GlyphGenerator
import games.thecodewarrior.bitfont.typesetting.Typesetter

class TypesetterTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Typesetter Test") {
    var text: AttributedString = AttributedString("")

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawCanvas(imgui: ImGui) {
        val glyphs = GlyphGenerator(text, listOf(document.bitfont))
        val typesetter = Typesetter(glyphs)

        val height = document.bitfont.ascent + document.bitfont.descent
        val min = canvas.min + vec(height/4 * scale, height * scale)

        for(typesetGlyph in typesetter) {
            typesetGlyph.glyph.draw(imgui,
                min + vec(typesetGlyph.posX, typesetGlyph.posY) * scale, scale, Colors.layoutTest.text.rgb)
        }
    }
}