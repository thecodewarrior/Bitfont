package games.thecodewarrior.bitfont.editor.utils.extensions

import games.thecodewarrior.bitfont.editor.imgui.ImGui
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
