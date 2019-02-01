package games.thecodewarrior.bitfont.editor.utils.extensions

import imgui.ImGui

fun ImGui.showHelpMarker(desc: String) {
    textDisabled("(?)")
    if (isItemHovered()) {
        beginTooltip()
        pushTextWrapPos(fontSize * 35f)
        textUnformatted(desc)
        popTextWrapPos()
        endTooltip()
    }
}
