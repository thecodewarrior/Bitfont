package games.thecodewarrior.bitfont

import glm_.func.common.clamp
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.functionalProgramming.button

object App {

    var f = 0f
    val clearColor = Vec4(0.45f, 0.55f, 0.6f, 1f)
    var showAnotherWindow = false
    var showDemo = true
    var counter = 0

    val name = CharArray(100)

    var lineHeight = 16
        set(value) {
            field = value.clamp(0, 65535)
        }

    var ascender = 10
        set(value) {
            field = value.clamp(0, 65535)
        }

    var descender = 4
        set(value) {
            field = value.clamp(0, 65535)
        }

    var capHeight = 9
        set(value) {
            field = value.clamp(0, 65535)
        }

    var xHeight = 5
        set(value) {
            field = value.clamp(0, 65535)
        }

    var glyphWindow = GlyphEditor()

    fun mainLoop() = with(ImGui) {
        // 1. Show the big demo window (Most of the sample code is in ImGui::ShowDemoWindow()! You can browse its code to learn more about Dear ImGui!).
        if (showDemo)
            showDemoWindow(::showDemo)

        run {
            begin("Font Settings")

            text("This is some useful text.")
            checkbox("Demo Window", ::showDemo)

            inputInt("Line Height", ::lineHeight)
            sameLine(); showHelpMarker("The distance between the baselines of consecutive lines")
            inputInt("Ascender", ::ascender)
            sameLine(); showHelpMarker("The height of ascenders (the top of d, l, etc.) above the baseline")
            inputInt("Descender", ::descender)
            sameLine(); showHelpMarker("The height of descenders (the bottom of p, g, etc.) below the baseline")

            inputInt("Cap height", ::capHeight)
            sameLine(); showHelpMarker("The height of capital letters (X, N, etc.) above the baseline, ignoring letters like A or O which may overshoot this line")
            inputInt("x height", ::xHeight)
            sameLine(); showHelpMarker("The height of the short lowercase letters (x, n, etc.) above the baseline, ignoring letters like d or l which overshoot this line")

            button("Edit") {
                glyphWindow = GlyphEditor()
                glyphWindow.visible = true
            }

            end()
        }

        glyphWindow.push()
    }

    private fun showHelpMarker(desc: String) = with(ImGui) {
        textDisabled("(?)")
        if (isItemHovered()) {
            beginTooltip()
            pushTextWrapPos(fontSize * 35f)
            textUnformatted(desc)
            popTextWrapPos()
            endTooltip()
        }
    }
}