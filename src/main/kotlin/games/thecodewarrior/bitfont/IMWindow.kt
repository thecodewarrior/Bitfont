package games.thecodewarrior.bitfont

import imgui.DrawList
import imgui.ImGui
import imgui.internal.Window

abstract class IMWindow {
    var visible: Boolean = false
    abstract val title: String
    protected abstract fun main()

    fun push() {
        if(visible) {
            if(ImGui.begin_("$title###${System.identityHashCode(this@IMWindow)}", ::visible)) {
                main()
            }
            ImGui.end()
        }
    }

    companion object {
        val drawList: DrawList
            get() = ImGui.windowDrawList
        val win: Window
            get() = ImGui.currentWindow
    }
}