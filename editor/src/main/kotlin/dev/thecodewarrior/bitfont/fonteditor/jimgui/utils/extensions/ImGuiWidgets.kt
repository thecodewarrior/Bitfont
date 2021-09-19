package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.extensions

import dev.thecodewarrior.bitfont.fonteditor.jimgui.imgui.ImGui
import org.ice1000.jimgui.flag.JImHoveredFlags

fun ImGui.showHelpMarker(desc: String) {
    textDisabled("(?)")
    if (isItemHovered(JImHoveredFlags.Default)) {
        beginTooltip()
        pushTextWrapPos(fontSize * 35f)
        textUnformatted(desc)
        popTextWrapPos()
        endTooltip()
    }
}
