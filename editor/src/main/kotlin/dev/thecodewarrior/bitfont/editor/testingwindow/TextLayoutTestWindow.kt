package dev.thecodewarrior.bitfont.editor.testingwindow

import dev.thecodewarrior.bitfont.editor.BitfontDocument
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.imgui.withNative
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.typesetting.GlyphGenerator
import dev.thecodewarrior.bitfont.typesetting.LineFragment
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import dev.thecodewarrior.bitfont.typesetting.Typesetter
import dev.thecodewarrior.bitfont.utils.Rect2i
import dev.thecodewarrior.bitfont.utils.Vec2i

class TextLayoutTestWindow(document: BitfontDocument): AbstractTestWindow(document, "Text Layout Test") {
    var text: AttributedString = AttributedString("")
    val options = Typesetter.Options()
    var exclusion = false

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
        imgui.sameLine()
        withNative(::exclusion) {
            imgui.checkbox("Exclusion", it)
        }
    }

    override fun drawCanvas(imgui: ImGui) {
        val height = document.bitfont.ascent + document.bitfont.descent
        val min = canvas.min + vec(height * scale, height * 2 * scale)

        val container = ExcludedContainer()
        if(exclusion) {
            val relativeMouse = (imgui.io.mousePos - min) / scale
            val size = 30
            container.exclusionRect = Rect2i(
                relativeMouse.x.toInt() - size, relativeMouse.y.toInt() - size,
                size*2, size*2
            )

            imgui.windowDrawList.addRectFilled(
                imgui.io.mousePos - vec(size*scale, size*scale), imgui.io.mousePos + vec(size*scale, size*scale),
                Colors.layoutTest.originIndicator.rgb
            )
        }
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

    class ExcludedContainer: TextContainer() {
        var exclusionRect: Rect2i? = null
        override fun fixLineFragment(line: LineFragment): LineFragment? {
            val exclusionRect = this.exclusionRect ?: return super.fixLineFragment(line)
            val intersectsY = !(line.maxY < exclusionRect.y || exclusionRect.maxY < line.posY)

            var nextFragment: LineFragment? = null
            if(intersectsY) {
                if(line.posX in exclusionRect.x until exclusionRect.maxX) {
                    line.width = line.maxX - exclusionRect.maxX
                    line.posX = exclusionRect.maxX
                } else if(line.posX < exclusionRect.x && line.maxX > exclusionRect.x) {
                    if(line.maxX > exclusionRect.maxX) {
                        nextFragment = LineFragment(exclusionRect.maxX, line.posY, line.maxX - exclusionRect.maxX, line.height)
                    }
                    line.width = exclusionRect.x - line.posX
                }
            }
            return nextFragment
        }
    }
}