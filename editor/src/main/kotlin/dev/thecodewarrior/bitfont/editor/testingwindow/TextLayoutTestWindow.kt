package dev.thecodewarrior.bitfont.editor.testingwindow

import dev.thecodewarrior.bitfont.editor.BitfontDocument
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.imgui.withNative
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Vec2i

class TextLayoutTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Text Layout Test") {
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
        val height = document.bitfont.ascent + document.bitfont.descent
        val min = canvas.min + vec(height * scale, height * 2 * scale)

        val container = TextContainer()
        container.size = Vec2i(canvas.widthi / scale - height * 2, canvas.heighti / scale - height * 4)
        val layoutManager = TextLayoutManager(listOf(document.bitfont))
        layoutManager.textContainers.add(container)
        layoutManager.typesetterOptions = options
        layoutManager.attributedString = text
        layoutManager.layoutText()

        for(line in container.lines) {
            val origin = min + vec(line.posX, line.posY) * scale
            for(main in line.glyphs) {
                main.draw(imgui, origin + vec(main.posX, main.posY) * scale, scale, Colors.layoutTest.text.rgb)
                main.attachments?.also { attachments ->
                    for(attachment in attachments) {
                        attachment.draw(imgui,
                            origin + vec(main.posX + attachment.posX, main.posY + attachment.posY) * scale,
                            scale, Colors.layoutTest.text.rgb
                        )
                    }
                }
            }
        }
    }
}