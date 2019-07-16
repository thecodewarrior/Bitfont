package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.imgui.AutoDeallocator
import dev.thecodewarrior.bitfont.editor.imgui.ImDrawList
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import org.ice1000.jimgui.JImDrawList
import org.ice1000.jimgui.NativeBool
import org.ice1000.jimgui.flag.JImFocusedFlags
import org.ice1000.jimgui.flag.JImWindowFlags

abstract class IMWindow: AutoDeallocator() {
    var visible: Boolean by native()
    abstract val title: String
    protected abstract fun main(imgui: ImGui)
    open fun drawMenu(imgui: ImGui) {}

    open val children = mutableListOf<dev.thecodewarrior.bitfont.editor.IMWindow>()
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
                dev.thecodewarrior.bitfont.editor.IMWindow.Companion.currentWindow = this
                drawMenu(imgui)
                main(imgui)
                imgui.end()
            }

        }
        children.forEach {
            it.push(imgui)
            dev.thecodewarrior.bitfont.editor.IMWindow.Companion.currentWindow = this
        }
    }

    companion object {
        lateinit var currentWindow: dev.thecodewarrior.bitfont.editor.IMWindow
        val drawList: ImDrawList
            get() = ImGui.current.windowDrawList
    }
}