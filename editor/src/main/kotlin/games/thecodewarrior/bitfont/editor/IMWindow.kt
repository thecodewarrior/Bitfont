package games.thecodewarrior.bitfont.editor

import imgui.DrawList
import imgui.ImGui
import imgui.WindowFlag
import imgui.internal.Window

abstract class IMWindow {
    var visible: Boolean = false
    abstract val title: String
    protected abstract fun main()

    open val children = mutableListOf<IMWindow>()
    val windowFlags = mutableSetOf<WindowFlag>()

    private var takeFocus = false

    fun focus() { takeFocus = true }

    fun push() {
        if(visible) {
            if(takeFocus) {
                ImGui.setNextWindowFocus()
                takeFocus = false
            }
            if(ImGui.begin_("$title###${System.identityHashCode(this@IMWindow)}", ::visible,
                    windowFlags.fold(0) { acc, it -> acc or it.i })
            ) {
                main()
            }
            ImGui.end()
        }
        children.forEach {
            it.push()
        }
    }

    companion object {
        val drawList: DrawList
            get() = ImGui.windowDrawList
        val win: Window
            get() = ImGui.currentWindow
    }
}