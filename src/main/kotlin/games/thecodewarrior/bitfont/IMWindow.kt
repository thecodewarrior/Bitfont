package games.thecodewarrior.bitfont

import imgui.DrawList
import imgui.ImGui
import imgui.WindowFlag
import imgui.internal.Window

abstract class IMWindow {
    var visible: Boolean = false
    abstract val title: String
    protected abstract fun main()

    val windowFlags = mutableSetOf<WindowFlag>()

    fun push() {
        if(visible) {
            if(ImGui.begin_("$title###${System.identityHashCode(this@IMWindow)}", ::visible,
                    windowFlags.fold(0) { acc, it -> acc or it.i })
            ) {
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