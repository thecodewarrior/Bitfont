package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.BitGrid
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.ReferenceFonts
import games.thecodewarrior.bitfont.utils.contours
import games.thecodewarrior.bitfont.utils.extensions.lineTo
import games.thecodewarrior.bitfont.utils.extensions.primaryModifier
import games.thecodewarrior.bitfont.utils.glyphProfile
import games.thecodewarrior.bitfont.utils.ifMac
import games.thecodewarrior.bitfont.utils.keys
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Col
import imgui.Dir
import imgui.ImGui
import imgui.functionalProgramming.withItemWidth
import imgui.internal.Rect
import org.lwjgl.glfw.GLFW
import java.awt.Font
import kotlin.math.abs
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

    val dataMap = mutableMapOf<Int, Data>()

    val data: Data
        get() = dataMap.getOrPut(codepoint) { Data(codepoint) }

    var referenceFontFamily = 0
        set(value) {
            if(field != value) referenceFontFace = 0
            field = value
        }
    var referenceFontFace = 0
    var referenceFontSize: Float
        get() = referenceFont.size2D
        set(value) { ReferenceFonts.setFontSize(referenceFontFamily, value) }

    val referenceFont: Font
        get() = ReferenceFonts[referenceFontFamily, referenceFontFace]

    class Data(val codepoint: Int) {
        val glyph = Glyph(codepoint)
        val enabledCells = mutableSetOf<Vec2i>()

        var historyIndex = 0
        var undoDepth = 0
        var redoDepth = 0
        val history = MutableList(100) { State() }

        fun pushHistory() {
            historyIndex++
            undoDepth = min(99, undoDepth + 1)
            redoDepth = 0
            history[historyIndex % history.size] = State()
        }

        fun undo() {
            if (undoDepth != 0) {
                undoDepth--
                historyIndex--
                redoDepth++
            }
            history[historyIndex % history.size].apply()
        }

        fun redo() {
            if (redoDepth != 0) {
                undoDepth++
                historyIndex++
                redoDepth--
            }
            history[historyIndex % history.size].apply()
        }

        inner class State {
            val cells = enabledCells.toSet()

            fun apply() {
                enabledCells.clear()
                enabledCells.addAll(cells)
            }
        }

        fun updateGlyph() {
            if(enabledCells.isEmpty())  {
                glyph.image = BitGrid(1, 1)
                glyph.bearingX = 0
                glyph.bearingY = 0
                return
            }
            var minX = Int.MAX_VALUE
            var maxX = 0
            var minY = Int.MAX_VALUE
            var maxY = 0
            enabledCells.forEach {
                minX = min(minX, it.x)
                maxX = max(maxX, it.x)
                minY = min(minY, it.y)
                maxY = max(maxY, it.y)
            }
            val grid = BitGrid(maxX - minX + 1, maxY - minY + 1)
            enabledCells.forEach {
                grid[it - Vec2i(minX, maxX)] = true
            }
            glyph.image = grid
            glyph.bearingX = minX
            glyph.bearingY = -minY
        }

    }

    override fun main() = with(ImGui) {
        val contentRect = win.contentsRegionRect

        pushButtonRepeat(true)
        if (arrowButton("##left", Dir.Left)) codepoint--
        sameLine()
        text("U+%04X".format(codepoint))
        sameLine()
        if (arrowButton("##right", Dir.Right)) codepoint++
        popButtonRepeat()

        withItemWidth(150) {
            listBox("##fontFamily", ::referenceFontFamily,
                ReferenceFonts.familyNames, 4)
            sameLine()
            listBox("##fontFace", ::referenceFontFace,
                ReferenceFonts.faceNames[referenceFontFamily], 4)
            sameLine()
            val speed = 1f / max(1f, abs(getMouseDragDelta(0).y) / 10)
            dragFloat("Size", ::referenceFontSize, speed, 1f, 1000f)

            inputInt("Zoom", ::granularity)
            sameLine()
            inputInt("Origin X", ::originX)
            sameLine()
            inputInt("Origin Y", ::originY)
        }

        canvas = Rect(contentRect.min + cursorPos - Vec2(0, win.dc.prevLineHeight), contentRect.max)
        itemSize(canvas, style.framePadding.y)
        itemHoverable(canvas, "editor".hashCode())
        pushClipRect(canvas.min, canvas.max, true)
        if (!itemAdd(canvas, "editor".hashCode())) return
        drawGrid()
        popClipRect()
    }

    fun drawCell(cell: Vec2i, col: Col) {
        drawList.addRectFilled(
            canvas.min + pos(cell),
            canvas.min + pos(cell + Vec2i(1, 1)),
            col.u32
        )
    }

    fun drawGrid() = with(ImGui) {

        drawList.addRectFilled(canvas.min, canvas.max, Constants.editorBackground)

        if(isWindowHovered()) {
            keys {
                if (ifMac("shift+cmd+z", "ctrl+y").pressed()) {
                    data.redo()
                } else if (ifMac("cmd+z", "ctrl+z").pressed()) {
                    data.undo()
                }
                val newTool = when {
                    "b".pressed() -> brush.apply { eraser = false }
                    "e".pressed() -> brush.apply { eraser = true }
                    "m".pressed() -> marquee
                    "prim+v".pressed() -> {
                        nudge.stop()
                        if (marquee.clipboard.isEmpty()) {
                            tool
                        } else {
                            nudge.moving.addAll(marquee.clipboard)
                            nudge
                        }
                    }
                    else -> tool
                }
                if (newTool != tool) {
                    tool.stop()
                    tool = newTool
                }
            }
        }

        tool.update()

        for(i in 0 .. (canvas.height / granularity).toInt()) {
            val col = when(i) {
                origin.y -> Constants.editorAxes
                origin.y - App.xHeight -> Constants.editorGuides
                origin.y - App.capHeight -> Constants.editorGuides
                origin.y - App.ascender -> Constants.editorGuides
                origin.y + App.descender -> Constants.editorGuides
                else -> Constants.editorGrid
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
                if(i == origin.x) Constants.editorAxes else Constants.editorGrid,
                1f
            )
        }

        (data.enabledCells + nudge.moving).forEach {
            drawCell(it, Col.Text)
        }

        referenceFont.glyphProfile(codepoint).forEach { contour ->
            drawList.addPolyline(ArrayList(contour.map {
                canvas.min + pos(it)
            }), Constants.editorSelection, true, 1f)
        }

        tool.draw()
    }

    fun cell(relativeMouse: Vec2): Vec2i {
        val gridScaled = relativeMouse / granularity
        return Vec2i(floor(gridScaled.x).toInt(), floor(gridScaled.y).toInt()) - origin
    }

    fun pos(cell: Vec2i): Vec2 {
        return Vec2((cell + origin) * granularity)
    }

    fun pos(point: Vec2): Vec2 {
        return (point + origin) * granularity
    }

    interface EditorTool {
        fun update()
        fun draw()
        fun stop()
    }

    inner class NudgeTool: EditorTool {
        val moving = mutableSetOf<Vec2i>()
        var draggingGripPoint: Vec2i? = null

        override fun update() = with(ImGui) {
            val mousePos = cell(io.mousePos - canvas.min)
            if(isWindowHovered() && isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                stop()
                tool = marquee
                marquee.update()
                return
            }
            if(isWindowHovered() && isMouseHoveringRect(canvas)) {
                if(isMouseClicked(0)) {
                    if(mousePos in moving) {
                        draggingGripPoint = mousePos
                    } else {
                        stop()
                        tool = marquee
                        marquee.update()
                        return
                    }
                }
            }
            if(isMouseReleased(0)) {
                draggingGripPoint = null
            }

            draggingGripPoint?.also { grip ->
                if(mousePos != grip) {
                    offset(mousePos - grip)
                    draggingGripPoint = mousePos
                }
            }

            if(isWindowHovered()) {
                keys {
                    "left" pressed { offset(Vec2i(-1, 0)) }
                    "right" pressed { offset(Vec2i(1, 0)) }
                    "up" pressed { offset(Vec2i(0, -1)) }
                    "down" pressed { offset(Vec2i(0, 1)) }

                    if (moving.isNotEmpty()) {
                        "prim+c" pressed {
                            marquee.clipboard.clear()
                            marquee.clipboard.addAll(moving)
                        }
                        "prim+x" pressed {
                            marquee.clipboard.clear()
                            marquee.clipboard.addAll(moving)
                            moving.clear()
                            tool = marquee
                        }
                        "backspace" pressed {
                            moving.clear()
                            tool = marquee
                        }
                    }
                }
            }

        }

        override fun draw() = with(ImGui) {
            moving.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Constants.editorSelection, true, 2f)
            }
        }

        fun start(selected: Collection<Vec2i>) {
            moving.addAll(selected.intersect(data.enabledCells))
            if(data.enabledCells.removeAll(moving)) data.pushHistory()
        }

        override fun stop() {
            if(data.enabledCells.addAll(moving)) data.pushHistory()
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
        val clipboard = mutableSetOf<Vec2i>()
        var start: Vec2i? = null
        var downPos: Vec2? = null

        override fun update(): Unit = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            if(isWindowHovered() && isMouseHoveringRect(canvas)) {
                if (isMouseClicked(0)) {
                    if(mouseCell in selected && io.primaryModifier) {
                        tool = nudge
                        nudge.start(selected)
                        selected.clear()
                        nudge.draggingGripPoint = mouseCell
                        nudge.update()
                        return
                    } else {
                        downPos = Vec2(io.mousePos)
                        start = mouseCell
                    }
                }
            }
            if (isMouseReleased(0)) {
                stop()
            }
            if(isWindowHovered()) {
                if(isMouseDown(0)) {
                    val start = start
                    selecting.clear()
                    if(start != null) {
                        for (x in min(start.x, mouseCell.x)..max(start.x, mouseCell.x)) {
                            for (y in min(start.y, mouseCell.y)..max(start.y, mouseCell.y)) {
                                selecting.add(Vec2i(x, y))
                            }
                        }
                    }
                } else {
                    keys {
                        "prim+(left|right|up|down)" pressed {
                            tool = nudge
                            nudge.start(selected)
                            selected.clear()
                            nudge.update()
                            return
                        }
                        "backspace" pressed {
                            if (data.enabledCells.removeAll(selected)) data.pushHistory()
                            selected.clear()
                        }
                        "escape" pressed {
                            selected.clear()
                        }
                        (selected.isNotEmpty() && io.primaryModifier) and "c" pressed {
                            clipboard.clear()
                            clipboard.addAll(selected.intersect(data.enabledCells))
                            selected.clear()
                        }
                        (selected.isNotEmpty() && io.primaryModifier) and "x" pressed {
                            clipboard.clear()
                            clipboard.addAll(selected.intersect(data.enabledCells))
                            if (data.enabledCells.removeAll(clipboard)) data.pushHistory()
                            selected.clear()
                        }
                        "left" pressed { offset(Vec2i(-1, 0)) }
                        "right" pressed { offset(Vec2i(1, 0)) }
                        "up" pressed { offset(Vec2i(0, -1)) }
                        "down" pressed { offset(Vec2i(0, 1)) }
                    }
                }
            }
        }

        override fun draw() = with(ImGui) {
            selected.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Constants.editorSelection, true, 2f)
            }

            selecting.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Constants.editorSelection, true, 2f)
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
        var modified = false

        override fun update() = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            var mouseCellPrev = cell(io.mousePos - canvas.min - io.mouseDelta)

            if(isWindowHovered() && isMouseHoveringRect(canvas)) {
                if (isMouseClicked(0)) {
                    drawingState = !eraser
                    if (io.keyShift)
                        mouseCellPrev = mouseReleasedPos ?: mouseCellPrev
                    modified = false
                }
            }
            if (isMouseReleased(0)) {
                drawingState = null
                if(modified) data.pushHistory()
                modified = false
            }

            if(drawingState != null)
                mouseReleasedPos = mouseCell
            val cells = mouseCellPrev.lineTo(mouseCell)
            if (drawingState == true) {
                modified = data.enabledCells.addAll(cells) || modified
            } else if (drawingState == false) {
                modified = data.enabledCells.removeAll(cells) || modified
            }
        }

        override fun draw(): Unit = with(ImGui) {
            val mouseCell = cell(io.mousePos - canvas.min)
            drawCell(mouseCell, Col.PlotLinesHovered)
            mouseReleasedPos?.also {
                if(io.keyShift) {
                    val startPos = canvas.min + pos(it) + Vec2(granularity/2, granularity/2)
                    val endPos = canvas.min + pos(mouseCell) + Vec2(granularity/2, granularity/2)
                    drawList.addLine(startPos, endPos, Col.PlotLinesHovered.u32, 2f)
                }
            }
        }

        override fun stop() {
            drawingState = null
        }
    }
}
