package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.JColor
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.DrawList
import imgui.ImGui
import imgui.WindowFlag
import imgui.internal.DrawCornerFlag
import imgui.internal.Rect
import imgui.internal.Window

abstract class IMWindow {
    var visible: Boolean = true
    abstract val title: String
    protected abstract fun main()

    open val children = mutableListOf<IMWindow>()
    val windowFlags = mutableSetOf<WindowFlag>()
    private var lastError: Exception? = null

    private var takeFocus = false

    fun focus() { takeFocus = true }

    fun push() {
        if (visible) {
            if (takeFocus) {
                ImGui.setNextWindowFocus()
                takeFocus = false
            }
            if (ImGui.begin_("$title###${System.identityHashCode(this@IMWindow)}", ::visible,
                    windowFlags.fold(0) { acc, it -> acc or it.i })
            ) {
                try {
                    main()
                    lastError = null
                } catch(e: Exception) {
                    if(lastError?.let { exceptionEquals(it, e) } != true) {
                        e.printStackTrace()
                        lastError = e
                    }
                    val rect = Rect(win.pos + Vec2(0, win.titleBarHeight), win.pos + win.size)
                    ImGui.popClipRect()
                    drawList.addRectFilled(rect.min, rect.max, JColor(127, 127, 127, 127).u32, win.windowRounding, DrawCornerFlag.Bot.i)
                    val sin45 = Math.sin(Math.toRadians(45.0))
                    val roundOffset = sin45*win.windowRounding/2
                    drawList.addLine(
                        rect.min,
                        rect.max - Vec2(roundOffset+1, roundOffset),
                        Colors.white.u32)
                    drawList.addLine(
                        Vec2(rect.min.x + roundOffset, rect.max.y - roundOffset),
                        Vec2(rect.max.x - 1, rect.min.y),
                        Colors.white.u32
                    )
                    ImGui.pushClipRect(Vec2(), Vec2(), false)
                }
            }
            ImGui.end()
        }
        children.forEach {
            it.push()
        }
    }

    private fun exceptionEquals(e1: Throwable, e2: Throwable): Boolean {
        return e1.message == e2.message &&
            e1.stackTrace contentEquals e2.stackTrace &&
            (e1.cause to e2.cause).let { (cause1, cause2) ->
                cause1 === cause2 || (cause1 != null && cause2 != null && exceptionEquals(cause1, cause2))
            }
    }

    companion object {
        val drawList: DrawList
            get() = ImGui.windowDrawList
        val win: Window
            get() = ImGui.currentWindow
    }
}