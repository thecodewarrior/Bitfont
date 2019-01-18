package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.utils.extensions.lineTo
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Col
import imgui.Color
import imgui.ImGui
import imgui.internal.Rect

class GlyphEditor: IMWindow() {
    var codepoint: Int = 65
    override val title: String
        get() = "U+%04X".format(codepoint)

    val enabledCells = mutableSetOf<Vec2i>()
    var drawingState: Boolean? = null

    override fun main() = with(ImGui) {
        val bb = win.contentsRegionRect//Rect(win.dc.cursorPos, win.dc.cursorPos + win.cont)
        itemSize(bb, style.framePadding.y)
        itemHoverable(bb, "editor".hashCode())
        if (!itemAdd(bb, "editor".hashCode())) return
        drawGrid(bb)
//        val borderSize = style.frameBorderSize
//        if (border && borderSize > 0f) {
//            window.drawList.addRect(pMin + 1, pMax + 1, Col.BorderShadow.u32, rounding, Dcf.All.i, borderSize)
//            window.drawList.addRect(pMin, pMax, Col.Border.u32, rounding, 0.inv(), borderSize)
//        }
    }

    fun drawGrid(bb: Rect) = with(ImGui) {
        val granularity = 25

        for(i in 0 .. bb.height.toInt() step granularity) {
            drawList.addLine(Vec2(bb.min.x, bb.min.y + i), Vec2(bb.max.x, bb.min.y + i), Col.PlotLines.u32, 1f)
        }

        for(i in 0 .. bb.width.toInt() step granularity) {
            drawList.addLine(Vec2(bb.min.x + i, bb.min.y), Vec2(bb.min.x + i, bb.max.y), Col.PlotLines.u32, 1f)
        }

        var relativeMouse = io.mousePos - bb.min
        var gridScaled = relativeMouse / granularity
        val mouseCell = Vec2i(gridScaled.x.toInt(), gridScaled.y.toInt())

        relativeMouse = io.mousePos - io.mouseDelta - bb.min
        gridScaled = relativeMouse / granularity
        val mouseCellPrev = Vec2i(gridScaled.x.toInt(), gridScaled.y.toInt())

        if(isMouseClicked(0)) drawingState = !enabledCells.contains(mouseCell)
        if(isMouseReleased(0)) drawingState = null

        fun drawCell(cell: Vec2i, col: Col) {
            drawList.addRectFilled(
                bb.min + cell * granularity,
                bb.min + cell * granularity + Vec2(granularity, granularity),
                col.u32
            )
        }

        mouseCellPrev.lineTo(mouseCell).forEach {
            if (drawingState == true) {
                enabledCells.add(it)
            } else if (drawingState == false) {
                enabledCells.remove(it)
            }
        }

        enabledCells.forEach {
            if(it == mouseCell) return@forEach
            drawCell(it, Col.PlotLines)
        }

        drawCell(mouseCell, Col.PlotLinesHovered)
    }
}