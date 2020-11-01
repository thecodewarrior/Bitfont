package dev.thecodewarrior.bitfont.editor.jimgui.testingwindow

import dev.thecodewarrior.bitfont.editor.jimgui.BitfontDocument
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.withNative
import dev.thecodewarrior.bitfont.editor.jimgui.utils.Colors
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.u32
import dev.thecodewarrior.bitfont.editor.jimgui.utils.math.vec
import dev.thecodewarrior.bitfont.editor.jimgui.testingwindow.AbstractTestWindow
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.Typesetter

class TypesetterTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Typesetter Test") {
    var text: AttributedString = AttributedString("")
    val options = Typesetter.Options()

    override fun stringInput(string: String) {
        text = AttributedString(string)
    }

    override fun drawControls(imgui: ImGui) {
        imgui.sameLine()
//        withNative(options::enableKerning) {
//            imgui.checkbox("Kerning", it)
//        }
        imgui.sameLine()
        withNative(options::enableCombiningCharacters) {
            imgui.checkbox("Combining Characters", it)
        }
    }

    override fun drawCanvas(imgui: ImGui) {
        val glyphs = GlyphGenerator(text, document.bitfont)
        val typesetter = Typesetter(glyphs)
        typesetter.options = options

        val height = document.bitfont.ascent + document.bitfont.descent
        val min = canvas.min + vec(height * scale, canvas.heighti / 2)

        for(main in typesetter) {
//            main.draw(imgui, min + vec(main.posX, main.posY) * scale, scale, Colors.layoutTest.text.u32)
//            main.attachments?.also { attachments ->
//                for(attachment in attachments) {
//                    attachment.draw(imgui,
//                        min + vec(main.posX + attachment.posX, main.posY + attachment.posY) * scale,
//                        scale, Colors.layoutTest.text.u32
//                    )
//                }
//            }
        }
    }
}