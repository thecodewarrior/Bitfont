package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.contours
import games.thecodewarrior.bitfont.utils.extensions.lineTo
import games.thecodewarrior.bitfont.utils.extensions.u32
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Col
import imgui.Color
import imgui.ImGui
import imgui.internal.Rect
import org.lwjgl.glfw.GLFW
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class GlyphEditor: IMWindow() {
    var codepoint: Int = 65
    override val title: String
        get() = "U+%04X".format(codepoint)

    var granularity = 25
    set(value) {
        field = max(value, 1)
    }
    val brush = BrushTool()
    val marquee: MarqueeTool = MarqueeTool()
    val nudge: NudgeTool = NudgeTool()

    var tool: EditorTool = brush

    var canvas = Rect()
    var originX = 10
    var originY = 10
    val origin: Vec2i
        get() = Vec2i(originX, originY)

    val enabledCells = mutableSetOf<Vec2i>()

    override fun main() = with(ImGui) {
        val contentRect = win.contentsRegionRect

        inputInt("Zoom", ::granularity)
        inputInt("Origin X", ::originX)
        inputInt("Origin Y", ::originY)

        canvas = Rect(contentRect.min + cursorPos - Vec2(0, win.dc.prevLineHeight), contentRect.max)
        itemSize(canvas, style.framePadding.y)
        itemHoverable(canvas, "editor".hashCode())
        pushClipRect(canvas.min, canvas.max, true)
        if (!itemAdd(canvas, "editor".hashCode())) return
        drawGrid()
        popClipRect()
//        val borderSize = style.frameBorderSize
//        if (border && borderSize > 0f) {
//            window.drawList.addRect(pMin + 1, pMax + 1, Col.BorderShadow.u32, rounding, Dcf.All.i, borderSize)
//            window.drawList.addRect(pMin, pMax, Col.Border.u32, rounding, 0.inv(), borderSize)
//        }
    }

    fun drawCell(cell: Vec2i, col: Col) {
        drawList.addRectFilled(
            canvas.min + cell * granularity,
            canvas.min + cell * granularity + Vec2(granularity, granularity),
            col.u32
        )
    }

    fun drawGrid() = with(ImGui) {

        drawList.addRectFilled(canvas.min, canvas.max, Colors.editorBackground)

        val newTool = when {
            isKeyPressed(GLFW.GLFW_KEY_B) -> brush.apply { eraser = false }
            isKeyPressed(GLFW.GLFW_KEY_E) -> brush.apply { eraser = true }
            isKeyPressed(GLFW.GLFW_KEY_M) -> marquee
            else -> tool
        }
        if(newTool != tool) {
            tool.stop()
            tool = newTool
        }

        tool.update()

        for(i in 0 .. (canvas.height / granularity).toInt()) {
            val col = when(i) {
                origin.y -> Colors.editorAxes
                origin.y - App.xHeight -> Colors.editorGuides
                origin.y - App.capHeight -> Colors.editorGuides
                origin.y - App.ascender -> Colors.editorGuides
                origin.y + App.descender -> Colors.editorGuides
                else -> Colors.editorGrid
            }
            drawList.addLine(
                Vec2(canvas.min.x, canvas.min.y + i*granularity),
                Vec2(canvas.max.x, canvas.min.y + i*granularity),
                col,
                1f
            )
        }

        for(i in 0 .. (canvas.width / granularity).toInt()) {
            drawList.addLine(
                Vec2(canvas.min.x + i*granularity, canvas.min.y),
                Vec2(canvas.min.x + i*granularity, canvas.max.y),
                if(i == origin.x) Colors.editorAxes else Colors.editorGrid,
                1f
            )
        }

        (enabledCells + nudge.moving).forEach {
            drawCell(it, Col.Text)
        }

        tool.draw()
    }

    fun cell(relativeMouse: Vec2): Vec2i {
        val gridScaled = relativeMouse / granularity
        return Vec2i(floor(gridScaled.x).toInt(), floor(gridScaled.y).toInt())
    }

    interface EditorTool {
        fun update()
        fun draw()
        fun stop()
    }

    inner class NudgeTool: EditorTool {
        val moving = mutableSetOf<Vec2i>()

        override fun update() = with(ImGui) {
            if(isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                stop()
                tool = marquee
                marquee.update()
                return
            }
            if(isMouseHoveringRect(canvas)) {
                if(isMouseClicked(0)) {
                    stop()
                    tool = marquee
                    marquee.update()
                    return
                }
            }

            when {
                isKeyPressed(GLFW.GLFW_KEY_LEFT) -> offset(Vec2i(-1, 0))
                isKeyPressed(GLFW.GLFW_KEY_RIGHT) -> offset(Vec2i(1, 0))
                isKeyPressed(GLFW.GLFW_KEY_UP) -> offset(Vec2i(0, -1))
                isKeyPressed(GLFW.GLFW_KEY_DOWN) -> offset(Vec2i(0, 1))
            }
        }

        override fun draw() = with(ImGui) {
            moving.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + Vec2(it) * granularity
                }), Colors.editorSelection, true, 2f)
            }
        }

        fun start(selected: Collection<Vec2i>) {
            moving.addAll(selected.intersect(enabledCells))
            enabledCells.removeAll(moving)
        }

        override fun stop() {
            enabledCells.addAll(moving)
            moving.clear()
        }

        fun offset(off: Vec2i) {
            val offsetPoints = moving.map { it + off }
            moving.clear()
            moving.addAll(offsetPoints)
        }
    }

    inner class MarqueeTool: EditorTool {
        val selected = mutableSetOf<Vec2i>()
        val selecting = mutableSetOf<Vec2i>()
        var start: Vec2i? = null
        var downPos: Vec2? = null

        override fun update(): Unit = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            if(isMouseHoveringRect(canvas)) {
                if (isMouseClicked(0)) {
                    downPos = Vec2(io.mousePos)
                    start = mouseCell
                }
            }
            if((isKeyPressed(GLFW.GLFW_KEY_LEFT) ||
                isKeyPressed(GLFW.GLFW_KEY_RIGHT) ||
                isKeyPressed(GLFW.GLFW_KEY_UP) ||
                isKeyPressed(GLFW.GLFW_KEY_DOWN)) && io.keySuper) {
                tool = nudge
                nudge.start(selected)
                selected.clear()
                nudge.update()
                return
            }
            if(isKeyPressed(GLFW.GLFW_KEY_BACKSPACE)) {
                enabledCells.removeAll(selected)
                selected.clear()
            }
            if(isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                selected.clear()
            }
            when {
                isKeyPressed(GLFW.GLFW_KEY_LEFT) -> offset(Vec2i(-1, 0))
                isKeyPressed(GLFW.GLFW_KEY_RIGHT) -> offset(Vec2i(1, 0))
                isKeyPressed(GLFW.GLFW_KEY_UP) -> offset(Vec2i(0, -1))
                isKeyPressed(GLFW.GLFW_KEY_DOWN) -> offset(Vec2i(0, 1))
            }

            val start = start
            selecting.clear()
            if(start != null) {
                for (x in min(start.x, mouseCell.x)..max(start.x, mouseCell.x)) {
                    for (y in min(start.y, mouseCell.y)..max(start.y, mouseCell.y)) {
                        selecting.add(Vec2i(x, y))
                    }
                }
            }
            if (isMouseReleased(0)) {
                stop()
            }
        }

        override fun draw() = with(ImGui) {
            selected.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + Vec2(it) * granularity
                }), Colors.editorSelection, true, 2f)
            }

            selecting.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + Vec2(it) * granularity
                }), Colors.editorSelection, true, 2f)
            }
        }

        override fun stop() = with(ImGui) {
            this@MarqueeTool.start = null
            when {
                io.keyShift -> selected.addAll(selecting)
                io.keyAlt -> selected.removeAll(selecting)
                io.mousePos == downPos -> selected.clear()
                else -> {
                    selected.clear()
                    selected.addAll(selecting)
                }
            }
            selecting.clear()
        }

        fun offset(off: Vec2i) {
            val offsetPoints = selected.map { it + off }
            selected.clear()
            selected.addAll(offsetPoints)
        }
    }

    inner class BrushTool: EditorTool {
        var eraser: Boolean = false
        var drawingState: Boolean? = null
        var mouseReleasedPos: Vec2i? = null

        override fun update() = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            var mouseCellPrev = cell(io.mousePos - canvas.min - io.mouseDelta)

            if(isMouseHoveringRect(canvas)) {
                if (isMouseClicked(0)) {
                    drawingState = !eraser
                    if (io.keyShift)
                        mouseCellPrev = mouseReleasedPos ?: mouseCellPrev
                }
            }
            if (isMouseReleased(0)) {
                drawingState = null
            }

            if(drawingState != null)
                mouseReleasedPos = mouseCell
            mouseCellPrev.lineTo(mouseCell).forEach {
                if (drawingState == true) {
                    enabledCells.add(it)
                } else if (drawingState == false) {
                    enabledCells.remove(it)
                }
            }
        }

        override fun draw(): Unit = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            drawCell(mouseCell, Col.PlotLinesHovered)
            mouseReleasedPos?.also {
                if(io.keyShift) {
                    val startPos = canvas.min + it * granularity + Vec2(granularity/2, granularity/2)
                    val endPos = canvas.min + mouseCell * granularity + Vec2(granularity/2, granularity/2)
                    drawList.addLine(startPos, endPos, Col.PlotLinesHovered.u32, 2f)
                }
            }
        }

        override fun stop() {
            drawingState = null
        }
    }
}
