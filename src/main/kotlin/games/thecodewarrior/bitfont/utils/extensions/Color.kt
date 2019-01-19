package games.thecodewarrior.bitfont.utils.extensions

import imgui.Color
import imgui.ImGui

val Color.u32: Int
    get() = ImGui.getColorU32(this.value)
