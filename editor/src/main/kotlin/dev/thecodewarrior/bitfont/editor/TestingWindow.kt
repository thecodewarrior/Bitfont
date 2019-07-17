package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.TypesetString
import dev.thecodewarrior.bitfont.editor.IMWindow
import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.editor.imgui.ImGui
import dev.thecodewarrior.bitfont.editor.imgui.withNative
import dev.thecodewarrior.bitfont.utils.Attribute
import dev.thecodewarrior.bitfont.typesetting.AttributedString
import dev.thecodewarrior.bitfont.editor.utils.Colors
import dev.thecodewarrior.bitfont.editor.utils.extensions.JColor
import dev.thecodewarrior.bitfont.editor.utils.extensions.color
import dev.thecodewarrior.bitfont.editor.utils.extensions.draw
import dev.thecodewarrior.bitfont.editor.utils.extensions.im
import dev.thecodewarrior.bitfont.editor.utils.extensions.random
import dev.thecodewarrior.bitfont.editor.utils.keys
import dev.thecodewarrior.bitfont.editor.utils.math.Vec2
import dev.thecodewarrior.bitfont.editor.utils.math.rect
import dev.thecodewarrior.bitfont.editor.utils.math.vec
import dev.thecodewarrior.bitfont.typesetting.MutableAttributedString
import dev.thecodewarrior.bitfont.typesetting.font
import org.ice1000.jimgui.flag.JImFocusedFlags
import java.awt.Color
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
    var canvas = rect(0, 0, 0, 0)
    val textOrigin: Vec2
        get() = canvas.min + vec(5, 5)

    init {
    }

    open fun handleInput(imgui: ImGui) {
        imgui.keys {
            "prim+v" pressed {
                val clipboard = imgui.clipboardText
//                if("shift".pressed()) {
//                    val hue = Math.random().toFloat()
//                    val saturation = 0.25f + Math.random().toFloat() * 0.75f
//                    val brightness = 0.75f + Math.random().toFloat() * 0.25f
//                    val color = JColor.getHSBColor(hue, saturation, brightness)
//                    testString.insert(Random.nextInt(0, testString.length), AttributedString(clipboard, Attribute.color to color))
//                } else {
                    testString = MutableAttributedString(clipboard)
//                }
            }
        }
    }

    override fun main(imgui: ImGui): Unit {
        if(imgui.isWindowFocused(JImFocusedFlags.RootAndChildWindows) /* && imgui.g.activeId == 0 */) handleInput(imgui)
//        withItemWidth(win.contentsRegionRect.width - 300f) {
//            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
//                CharArray(name.size + 1000).also { name.copyInto(it) }
//            }
//            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
//                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
//        }
        imgui.pushAllowKeyboardFocus(false)
        imgui.pushItemWidth(150f)
            imgui.sameLine()
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
        imgui.rect(canvas.widthf, canvas.heightf, Color(0, 0, 0, 0).im, 0f, 0f)
        drawCanvas(imgui)
        imgui.popClipRect()
    }

    fun drawCanvas(imgui: ImGui) {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.layoutTest.background.rgb
        )

        val cursor = textOrigin - vec(0.5, 0.5)
        drawList.addTriangleFilled(cursor, cursor + vec(-scale, -scale), cursor + vec(-scale, scale), Colors.layoutTest.originIndicator.rgb)
        val textRegion = rect(textOrigin, canvas.max)

        typesetString = TypesetString(bitfont, testString, (textRegion.width / scale).toInt())
        typesetString.glyphs.forEach {
            drawGlyph(imgui, it)
        }
    }

    fun drawGlyph(imgui: ImGui, char: TypesetString.GlyphRender) {
        val color = char.attributes[Attribute.color] ?: Colors.layoutTest.text
        val font = char.attributes[Attribute.font] ?: bitfont
        char.glyph.draw(imgui, textOrigin + Vec2(char.pos) * scale, scale, color.rgb)
    }
}