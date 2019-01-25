package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.typesetting.PlacedGlyph
import games.thecodewarrior.bitfont.typesetting.Typesetter
import games.thecodewarrior.bitfont.typesetting.TypesettingOptions
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.extensions.addAll
import glm_.vec2.Vec2
import imgui.ImGui
import imgui.InputTextFlag
import imgui.WindowFlag
import imgui.functionalProgramming.withItemWidth
import imgui.g
import imgui.internal.Rect
import kotlin.math.max

class TestingWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont
    val typesetter = Typesetter(bitfont)

    override val title: String
        get() = "${bitfont.name}: Testing"

    var testString: String = ""
    var scale = 2
        set(value) {
            field = max(value, 1)
        }
    var canvas = Rect()

    init {
    }

    override fun main(): Unit = with(ImGui) {
        withItemWidth(win.contentsRegionRect.width - 300f) {
            val arr = testString.replace("\n", "\\n").toCharArray().let { name ->
                CharArray(name.size + 1000).also { name.copyInto(it) }
            }
            if(inputText("##testText", arr, InputTextFlag.EnterReturnsTrue.i))
                testString = String(g.inputTextState.textW.sliceArray(0 until g.inputTextState.textW.indexOf('\u0000'))).replace("\\n", "\n")
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

    fun drawCanvas() {
        drawList.addRectFilled(
            canvas.min,
            canvas.max,
            Constants.editorBackground
        )

        val cursor = canvas.min + Vec2(bitfont.lineHeight) * scale
        drawList.addLine(Vec2(canvas.min.x, cursor.y), cursor, Constants.editorAxes)

        val runs = typesetter.typeset(testString)
        runs.forEach { run ->
            run.glyphs.forEach { glyph ->
                drawGlyph(glyph)
            }
        }
    }

    fun drawGlyph(glyph: PlacedGlyph) {
        val grid = glyph.glyph.image
        for(x in 0 until grid.width) {
            for(y in 0 until grid.height) {
                if(grid[x, y]) {
                    val pos = canvas.min + (glyph.pos + Vec2(x, y) + Vec2(bitfont.lineHeight)) * scale
                    drawList.addRectFilled(pos, pos + Vec2(scale), Constants.SimpleColors.white)
                }

            }
        }
    }
}