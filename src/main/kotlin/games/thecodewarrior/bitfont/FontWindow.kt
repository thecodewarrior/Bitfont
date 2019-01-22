package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.BitFont
import glm_.func.common.clamp
import imgui.ImGui
import imgui.imgui.imgui_demoDebugInformations.Companion.showHelpMarker
import imgui.functionalProgramming.button

class FontWindow: IMWindow() {
    override val title: String
        get() = "Font"
    override val children: MutableList<IMWindow>
        get() = mutableListOf(glyphWindow)

    val bitFont = BitFont("Font", 16, 10, 4, 9, 6)

    var glyphWindow = GlyphEditor(bitFont)

    override fun main() = with(ImGui) {
        text("This is some useful text.")
        inputInt("Line Height", bitFont::lineHeight)
        sameLine(); showHelpMarker("The distance between the baselines of consecutive lines")
        inputInt("Ascender", bitFont::ascender)
        sameLine(); showHelpMarker("The height of ascenders (the top of d, l, etc.) above the baseline")
        inputInt("Descender", bitFont::descender)
        sameLine(); showHelpMarker("The height of descenders (the bottom of p, g, etc.) below the baseline")

        inputInt("Cap height", bitFont::capHeight)
        sameLine(); showHelpMarker("The height of capital letters (X, N, etc.) above the baseline, ignoring letters like A or O which may overshoot this line")
        inputInt("x height", bitFont::xHeight)
        sameLine(); showHelpMarker("The height of the short lowercase letters (x, n, etc.) above the baseline, ignoring letters like d or l which overshoot this line")

        button("Edit") {
            glyphWindow = GlyphEditor(bitFont)
            glyphWindow.visible = true
        }
    }
}