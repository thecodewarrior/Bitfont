package games.thecodewarrior.bitfont.editor.testingwindow

import games.thecodewarrior.bitfont.editor.IMWindow
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.BitfontDocument
import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.imgui.withNative
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.extensions.JColor
import games.thecodewarrior.bitfont.editor.utils.extensions.color
import games.thecodewarrior.bitfont.editor.utils.extensions.draw
import games.thecodewarrior.bitfont.editor.utils.extensions.random
import games.thecodewarrior.bitfont.editor.utils.keys
import games.thecodewarrior.bitfont.editor.utils.math.rect
import games.thecodewarrior.bitfont.editor.utils.math.vec
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.font
import org.ice1000.jimgui.flag.JImFocusedFlags
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
    var canvas = rect(0, 0, 0, 0)

    abstract fun stringInput(string: String)

    abstract fun drawCanvas(imgui: ImGui)

    open fun handleInput(imgui: ImGui) {
        imgui.keys {
            "prim+v" pressed {
                stringInput(imgui.clipboardText)
            }
        }
    }

    override fun main(imgui: ImGui): Unit {
        if(imgui.isWindowFocused(JImFocusedFlags.RootAndChildWindows) /*&& g.activeId == 0*/) handleInput(imgui)
        imgui.pushAllowKeyboardFocus(false)
        imgui.sameLine()

        imgui.pushItemWidth(150f)
        withNative(::scale) {
            imgui.inputInt("Scale", it)
        }
        imgui.popItemWidth()
        imgui.popAllowKeyboardFocus()

        val canvasPos = imgui.windowContentRegionRect.min + vec(imgui.windowPosX, imgui.windowPosY + imgui.frameHeightWithSpacing)

        canvas = rect(canvasPos, canvasPos + vec(imgui.windowContentRegionRect.width, imgui.windowContentRegionRect.height - imgui.frameHeightWithSpacing))
//        imgui.itemSize(canvas)
        imgui.pushClipRect(canvas, true)
//        imgui.itemHoverable(canvas, "canvas".hashCode())
//        imgui.itemAdd(canvas, "canvas".hashCode())
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.rgb
        )
        drawCanvas(imgui)
        imgui.popClipRect()
    }

}