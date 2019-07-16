package dev.thecodewarrior.bitfont.editor.testingwindow

import dev.thecodewarrior.bitfont.editor.BitfontDocument
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.imgui.withNative
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.Typesetter

class TypesetterTestWindow(document: dev.thecodewarrior.bitfont.editor.BitfontDocument): AbstractTestWindow(document, "Typesetter Test") {
    var text: dev.thecodewarrior.bitfont.typesetting.AttributedString = dev.thecodewarrior.bitfont.typesetting.AttributedString("")
    val options = dev.thecodewarrior.bitfont.typesetting.Typesetter.Options()

    override fun stringInput(string: String) {
        text = dev.thecodewarrior.bitfont.typesetting.AttributedString(string)
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
        val typesetter = dev.thecodewarrior.bitfont.typesetting.Typesetter(glyphs)
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