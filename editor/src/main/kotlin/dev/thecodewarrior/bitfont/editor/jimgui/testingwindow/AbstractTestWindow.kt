package dev.thecodewarrior.bitfont.editor.jimgui.testingwindow

import dev.thecodewarrior.bitfont.editor.jimgui.IMWindow
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.editor.jimgui.BitfontDocument
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.jimgui.imgui.withNative
import dev.thecodewarrior.bitfont.editor.jimgui.utils.Colors
import dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions.u32
import dev.thecodewarrior.bitfont.editor.jimgui.utils.keys
import dev.thecodewarrior.bitfont.editor.jimgui.utils.math.rect
import dev.thecodewarrior.bitfont.editor.jimgui.utils.math.vec
import org.ice1000.jimgui.flag.JImFocusedFlags
import kotlin.math.max

abstract class AbstractTestWindow(val document: BitfontDocument, testName: String): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String = "${bitfont.name}: $testName"

    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = rect(0, 0, 0, 0)

    abstract fun stringInput(string: String)

    abstract fun drawCanvas(imgui: ImGui)
    open fun drawControls(imgui: ImGui) {}

    open fun handleInput(imgui: ImGui) {
        imgui.keys {
            "prim+v" pressed {
                stringInput(imgui.clipboardText)
            }
        }
    }

    override fun main(imgui: ImGui) {
        if(imgui.isWindowFocused(JImFocusedFlags.RootAndChildWindows) /*&& g.activeId == 0*/) handleInput(imgui)
        imgui.pushAllowKeyboardFocus(false)
        imgui.sameLine()

        imgui.pushItemWidth(150f)
        withNative(::scale) {
            imgui.inputInt("Scale", it)
        }
        imgui.popItemWidth()
        drawControls(imgui)
        imgui.popAllowKeyboardFocus()

        val headerHeight = imgui.cursorPosY - imgui.frameHeightWithSpacing
        val canvasPos = imgui.windowContentRegionRect.min + vec(imgui.windowPosX, imgui.windowPosY + headerHeight)

        canvas = rect(canvasPos, canvasPos + vec(imgui.windowContentRegionRect.width, imgui.windowContentRegionRect.height - headerHeight))
//        imgui.itemSize(canvas)
        imgui.pushClipRect(canvas, true)
//        imgui.itemHoverable(canvas, "canvas".hashCode())
//        imgui.itemAdd(canvas, "canvas".hashCode())
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )
        drawCanvas(imgui)
        imgui.popClipRect()
    }

}