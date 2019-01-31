package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.Attribute
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.extensions.BitVec2i
import games.thecodewarrior.bitfont.utils.extensions.JColor
import games.thecodewarrior.bitfont.utils.extensions.draw
import games.thecodewarrior.bitfont.utils.extensions.random
import games.thecodewarrior.bitfont.utils.extensions.toIm
import games.thecodewarrior.bitfont.utils.extensions.u32
import games.thecodewarrior.bitfont.utils.keys
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.InputTextFlag
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import org.lwjgl.glfw.GLFW
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.max
import kotlin.math.min

class TestingWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: Testing"

    var testString: AttributedString = AttributedString("")
    var typesetString = TypesetString(bitfont, testString, -1)
    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()

    init {
    }

    override fun main(): Unit = with(ImGui) {
        keys {
            "prim+v" pressed {
                val clipboard = GLFW.glfwGetClipboardString(0)
                if(clipboard != null) {
                    testString = AttributedString(clipboard)
                    (0 until 20).forEach {
                        val start = random.nextInt(clipboard.length)
                        val end = start + random.nextInt(min(15, clipboard.length - start))

                        val hue = Math.random().toFloat()
                        val saturation = 0.25f + Math.random().toFloat() * 0.75f
                        val brightness = 0.75f + Math.random().toFloat() * 0.25f
                        val color = JColor.getHSBColor(hue, saturation, brightness)
                        testString.setAttributesForRange(start .. end, mapOf(
                            Attribute.color to color
                        ))
                    }
                }
            }
        }
//        withItemWidth(win.contentsRegionRect.width - 300f) {
//            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
//                CharArray(name.size + 1000).also { name.copyInto(it) }
//            }
//            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
//                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
//        }
        withItemWidth(150f) {
            sameLine()
            inputInt("Scale", ::scale)
        }

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

        val cursor = canvas.min + Vec2(bitfont.lineHeight) * scale - Vec2(0.5)
        drawList.addTriangleFilled(cursor, cursor + Vec2(-scale, -scale), cursor + Vec2(-scale, scale), Colors.layoutTest.originIndicator.u32)

        typesetString = TypesetString(bitfont, testString, (canvas.width / scale).toInt() - bitfont.lineHeight)
        typesetString.glyphs.forEach {
            drawGlyph(it)
        }
    }

    fun drawGlyph(char: TypesetString.GlyphRender) {
        val color = char.attributes[Attribute.color] as? JColor ?: Colors.layoutTest.text
        char.glyph.draw(canvas.min + Vec2(bitfont.lineHeight * scale) + char.pos.toIm() * scale, scale, color.u32)
    }
}