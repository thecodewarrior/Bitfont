package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.IMWindow
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
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

open class TestingWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Testing"

    var testString: MutableAttributedString = MutableAttributedString("")
    var typesetString = TypesetString(bitfont, testString, -1)
    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()
    val textOrigin: Vec2
        get() = canvas.min + Vec2(5, 5)

    init {
    }

    open fun handleInput() = with(ImGui) {
        keys {
            "prim+v" pressed {
                val clipboard = GLFW.glfwGetClipboardString(0)
                if(clipboard != null) {
                    if("shift".pressed()) {
                        val hue = Math.random().toFloat()
                        val saturation = 0.25f + Math.random().toFloat() * 0.75f
                        val brightness = 0.75f + Math.random().toFloat() * 0.25f
                        val color = JColor.getHSBColor(hue, saturation, brightness)
                        testString.insert(Random.nextInt(0, testString.length), AttributedString(clipboard, Attribute.color to color))
                    } else {
                        testString = MutableAttributedString(clipboard)
                    }
                }
            }
        }
    }

    override fun main(): Unit = with(ImGui) {
        if(isWindowFocused(FocusedFlag.RootAndChildWindows) && g.activeId == 0) handleInput()
//        withItemWidth(win.contentsRegionRect.width - 300f) {
//            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
//                CharArray(name.size + 1000).also { name.copyInto(it) }
//            }
//            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
//                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
//        }
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
        drawCanvas()
        popClipRect()
    }

    fun drawCanvas() {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.u32
        )

        val cursor = textOrigin - Vec2(0.5)
        drawList.addTriangleFilled(cursor, cursor + Vec2(-scale, -scale), cursor + Vec2(-scale, scale), Colors.layoutTest.originIndicator.u32)
        val textRegion = Rect(textOrigin, canvas.max)

        typesetString = TypesetString(bitfont, testString, (textRegion.width / scale).toInt())
        typesetString.glyphs.forEach {
            drawGlyph(it)
        }
    }

    fun drawGlyph(char: TypesetString.GlyphRender) {
        val color = char.attributes[Attribute.color] ?: Colors.layoutTest.text
        val font = char.attributes[Attribute.font] ?: bitfont
        char.glyph.draw(textOrigin + char.pos.toIm() * scale, scale, color.u32)
    }
}