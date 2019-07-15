package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.imgui.withNative
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.math.vec
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.GlyphGenerator
import games.thecodewarrior.bitfont.typesetting.Typesetter

class TypesetterTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Typesetter Test") {
    var text: AttributedString = AttributedString("")
    val options = Typesetter.Options()

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawControls(imgui: ImGui) {
        imgui.sameLine()
        withNative(options::enableKerning) {
            imgui.checkbox("Kerning", it)
        }
        imgui.sameLine()
        withNative(options::enableCombiningCharacters) {
            imgui.checkbox("Combining Characters", it)
        }
    }

    override fun drawCanvas(imgui: ImGui) {
        val glyphs = GlyphGenerator(text, listOf(document.bitfont))
        val typesetter = Typesetter(glyphs)
        typesetter.options = options

        val height = document.bitfont.ascent + document.bitfont.descent
        val min = canvas.min + vec(height * scale, canvas.heighti / 2)

        for(main in typesetter) {
            main.draw(imgui, min + vec(main.posX, main.posY) * scale, scale, Colors.layoutTest.text.rgb)
            main.attachments?.also { attachments ->
                for(attachment in attachments) {
                    attachment.draw(imgui,
                        min + vec(main.posX + attachment.posX, main.posY + attachment.posY) * scale,
                        scale, Colors.layoutTest.text.rgb
                    )
                }
            }
        }
    }
}