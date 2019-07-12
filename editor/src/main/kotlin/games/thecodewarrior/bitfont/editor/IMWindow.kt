package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.imgui.AutoDeallocator
import games.thecodewarrior.bitfont.editor.imgui.ImDrawList
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import org.ice1000.jimgui.JImDrawList
import org.ice1000.jimgui.NativeBool
import org.ice1000.jimgui.flag.JImFocusedFlags
import org.ice1000.jimgui.flag.JImWindowFlags

abstract class IMWindow: AutoDeallocator() {
    var visible: Boolean by native()
    abstract val title: String
    protected abstract fun main(imgui: ImGui)
    open fun drawMenu(imgui: ImGui) {}

    open val children = mutableListOf<IMWindow>()
    var windowFlags = JImWindowFlags.Nothing

    private var takeFocus = false

    fun focus() { takeFocus = true }

    fun push(imgui: ImGui) {
        if(visible) {
            if(takeFocus) {
                ImGui.setNextWindowFocus()
                takeFocus = false
            }
            if(imgui.begin("$title###${System.identityHashCode(this@IMWindow)}", native(::visible), windowFlags)) {
                currentWindow = this
                drawMenu(imgui)
                main(imgui)
                imgui.end()
            }

        }
        children.forEach {
            it.push(imgui)
            currentWindow = this
        }
    }

    companion object {
        lateinit var currentWindow: IMWindow
        val drawList: ImDrawList
            get() = ImGui.current.windowDrawList
    }
}