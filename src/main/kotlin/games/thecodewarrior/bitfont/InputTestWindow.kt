package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.TypesetCharacter
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.extensions.cString
import games.thecodewarrior.bitfont.utils.extensions.draw
import games.thecodewarrior.bitfont.utils.extensions.u32
import games.thecodewarrior.bitfont.utils.keys
import glm_.vec2.Vec2
import imgui.FocusedFlag
import imgui.ImGui
import imgui.InputTextFlag
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import kotlin.math.max

class InputTestWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont
    val field = BitfontTextField(bitfont)

    override val title: String
        get() = "${bitfont.name}: Testing"

    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()
    val textOrigin: Vec2
        get() = canvas.min + Vec2(5, 5)
    var canvasKeyFocused = false

    init {
    }

    override fun main(): Unit = with(ImGui) {
        canvasKeyFocused = isWindowFocused(FocusedFlag.RootAndChildWindows)
        withItemWidth(win.contentsRegionRect.width - 300f) {
            val arr = field.text.replace("\n", "\\n").toCharArray().let { name ->
                CharArray(name.size + 1000).also { name.copyInto(it) }
            }
            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
                field.text = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
        }
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

    fun drawCanvas() = with(ImGui) {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Colors.inputTest.background.u32
        )

        val cursor = textOrigin - Vec2(0.5)
        drawList.addTriangleFilled(cursor, cursor + Vec2(-scale, -scale), cursor + Vec2(-scale, scale), Colors.inputTest.originIndicator.u32)

        if(canvasKeyFocused) keys {
            if("left".pressed()) {
                field.cursor--
            }
            if("right".pressed()) {
                field.cursor++
            }
            if("backspace".pressed() && field.cursor > 0) {
                    field.delete(field.cursor-1, field.cursor)
            }
            if("delete".pressed() && field.cursor < field.text.length) {
                field.delete(field.cursor, field.cursor+1)
            }
            val input = io.inputCharacters.cString()
            if(input.isNotEmpty()) {
                field.insert(field.cursor, input)
            }
        }

        field.layout.characters.forEach { char ->
            drawGlyph(char)
        }
        if(System.currentTimeMillis() % 1000 > 500) {
            drawList.addRectFilled(
                textOrigin + (field.rendering.cursor - Vec2(1, bitfont.ascent))*scale,
                textOrigin + (field.rendering.cursor + Vec2(0, bitfont.descent))*scale,
                Colors.inputTest.cursor.u32
            )
        }
    }

    fun drawGlyph(char: TypesetCharacter) {
        val glyph = char.glyph ?: return
        glyph.draw(textOrigin + char.glyphPos * scale, scale, Colors.inputTest.text.u32)
    }
}