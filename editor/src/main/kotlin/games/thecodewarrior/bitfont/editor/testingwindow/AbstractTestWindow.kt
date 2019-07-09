package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.IMWindow
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.JColor
import games.thecodewarrior.bitfont.editor.utils.extensions.color
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.random
import games.thecodewarrior.bitfont.editor.utils.extensions.toIm
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.editor.utils.keys
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.font
import glm_.vec2.Vec2
import imgui.FocusedFlag
import imgui.ImGui
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

abstract class AbstractTestWindow(val document: BitfontDocument, testName: String): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String = "${bitfont.name}: $testName"

    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()

    abstract fun stringInput(string: String)

    abstract fun drawCanvas()

    open fun handleInput() = with(ImGui) {
        keys {
            "prim+v" pressed {
                val clipboard = GLFW.glfwGetClipboardString(0)
                if(clipboard != null) {
                    stringInput(clipboard)
                }
            }
        }
    }

    override fun main(): Unit = with(ImGui) {
        if(isWindowFocused(FocusedFlag.RootAndChildWindows) && g.activeId == 0) handleInput()
        pushAllowKeyboardFocus(false)
        withItemWidth(150f) {
            sameLine()
            inputInt("Scale", ::scale)
        }
        popAllowKeyboardFocus()

        val canvasPos = win.contentsRegionRect.min + Vec2(0, frameHeightWithSpacing)

        canvas = Rect(canvasPos, canvasPos + Vec2(win.contentsRegionRect.width, win.contentsRegionRect.max.y - canvasPos.y))
        itemSize(canvas)
        pushClipRect(canvas.min, canvas.max, true)
        itemHoverable(canvas, "canvas".hashCode())
        itemAdd(canvas, "canvas".hashCode())
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )
        drawCanvas()
        popClipRect()
    }

}