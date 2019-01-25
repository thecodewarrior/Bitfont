package games.thecodewarrior.bitfont

import com.ibm.icu.lang.UCharacter
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.BitGrid
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.ReferenceFonts
import games.thecodewarrior.bitfont.utils.alignedText
import games.thecodewarrior.bitfont.utils.contours
import games.thecodewarrior.bitfont.utils.extensions.ImGuiDrags
import games.thecodewarrior.bitfont.utils.extensions.lineTo
import games.thecodewarrior.bitfont.utils.extensions.primaryModifier
import games.thecodewarrior.bitfont.utils.glyphProfile
import games.thecodewarrior.bitfont.utils.ifMac
import games.thecodewarrior.bitfont.utils.keys
import games.thecodewarrior.bitfont.utils.opengl.Java2DTexture
import glm_.func.common.clamp
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Col
import imgui.Dir
import imgui.FocusedFlag
import imgui.ImGui
import imgui.functionalProgramming.withChild
import imgui.functionalProgramming.withItemWidth
import imgui.internal.Rect
import org.lwjgl.glfw.GLFW
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class GlyphEditorWindow(val document: BitfontDocument): IMWindow() {
    val bitfont: Bitfont = document.bitfont

    override val title: String
        get() = "${bitfont.name}: U+%04X - %s".format(codepoint, UCharacter.getName(codepoint))

    var granularity = 25
        set(value) {
            field = max(value, 1)
        }
    val brush = BrushTool()
    val marquee: MarqueeTool = MarqueeTool()
    val nudge: NudgeTool = NudgeTool()

    var tool: EditorTool = brush

    var canvas = Rect()
    val controlsWidth: Float = 175f

    var originX = 4
    var originY = 16
    val origin: Vec2i
        get() = Vec2i(originX, originY)

    val dataMap = mutableMapOf<Int, Data>()

    var data: Data = Data(0)
    var codepoint: Int = 0
        set(value) {
            if(field != value.clamp(0, 0x10FFFF)) {
                if(tool === nudge) nudge.stop()
                if(data == Data(codepoint)) {
                    dataMap.remove(codepoint)
                }
                codepointChanged = true
            }
            field = value.clamp(0, 0x10FFFF)
            data = dataMap.getOrPut(codepoint) { Data(codepoint) }
        }

    var codepointChanged = false
    var referenceStyle = 0
    var referenceSize = 1f
        set(value) { field = value.clamp(1f, 1000f) }
    val sidebarReferenceImage = Java2DTexture(controlsWidth.toInt(), controlsWidth.toInt())
    var displayReference = false
    var displayGrid = true
    var displayGuides = true
    var localGridRadius = 0
        set(value) { field = value.clamp(0, 100) }

    init {
        codepoint = 65
    }

    inner class Data(val codepoint: Int) {
        val glyph = bitfont.glyphs.getOrPut(codepoint) { Glyph(codepoint) }
        val enabledCells = mutableSetOf<Vec2i>()

        var historyIndex = 0
            private set
        var undoDepth = 0
            private set
        var redoDepth = 0
            private set
        val history = MutableList(100) { State() }

        var advance: Int
            get() = glyph.calcAdvance(bitfont.spacing)
            set(value) { glyph.advance = value }
        var autoAdvance: Boolean
            get() = glyph.advance == null
            set(value) { glyph.advance = if(value) null else advance }

        init {
            updateFromGlyph()
            history[0] = State()
        }

        fun pushHistory() {
            historyIndex++
            undoDepth = min(99, undoDepth + 1)
            redoDepth = 0
            history[historyIndex % history.size] = State()
            updateGlyph()
        }

        fun undo() {
            if (undoDepth != 0) {
                undoDepth--
                historyIndex--
                redoDepth++
            }
            history[historyIndex % history.size].apply()
            updateGlyph()
        }

        fun redo() {
            if (redoDepth != 0) {
                undoDepth++
                historyIndex++
                redoDepth--
            }
            history[historyIndex % history.size].apply()
            updateGlyph()
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
                grid[it - Vec2i(minX, minY)] = true
            }
            glyph.image = grid
            glyph.bearingX = minX
            glyph.bearingY = minY
        }

        fun updateFromGlyph() {
            enabledCells.clear()
            val grid = glyph.image
            for(x in 0 until grid.width) {
                for(y in 0 until grid.height) {
                    if(grid[Vec2i(x, y)]) {
                        enabledCells.add(Vec2i(x + glyph.bearingX, y + glyph.bearingY))
                    }
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Data) return false

            if (codepoint != other.codepoint) return false
            if (glyph != other.glyph) return false
            if (enabledCells != other.enabledCells) return false
            if (historyIndex != other.historyIndex) return false
            if (undoDepth != other.undoDepth) return false
            if (redoDepth != other.redoDepth) return false
            if (history != other.history) return false

            return true
        }

        override fun hashCode(): Int {
            var result = codepoint
            result = 31 * result + glyph.hashCode()
            result = 31 * result + enabledCells.hashCode()
            result = 31 * result + historyIndex
            result = 31 * result + undoDepth
            result = 31 * result + redoDepth
            result = 31 * result + history.hashCode()
            return result
        }
    }

    override fun main() = with(ImGui) {
        val wasChanged = codepointChanged
        var menuHeight = -cursorPos
        val contentRect = win.contentsRegionRect
        menuHeight = menuHeight + cursorPos
        withChild("Controls", Vec2(controlsWidth, contentRect.height), false) {
            drawControls()
        }

        sameLine()

        canvas = Rect(contentRect.min + Vec2(controlsWidth + 5, menuHeight.y), contentRect.max)
        drawCanvas()
        if(wasChanged)
            codepointChanged = false
    }

    fun drawControls() = with(ImGui) { withItemWidth(controlsWidth) {
        pushButtonRepeat(true)
        if (arrowButton("##left", Dir.Left)) codepoint--
        sameLine()
        withItemWidth(controlsWidth - frameHeight*2 - style.itemSpacing.x*2) {
            val speed = max(1.0, abs(getMouseDragDelta(0).y) / 10.0)
            ImGuiDrags.dragScalar(
                label = "##codepointDrag",
                value = ::codepoint,
                speed = speed,
                power = 1.0,
                minValue = 0,
                maxValue = 0x10FFFF,
                valueToDisplay = { "U+%04X".format(it) },
                correct = { it.roundToInt() },
                valueToString = { "%04X".format(it) },
                stringToValue = { current, new -> (new.toIntOrNull(16) ?: current) to "" }
            )
        }
        sameLine()
        if (arrowButton("##right", Dir.Right)) codepoint++
        popButtonRepeat()

        var labelWidth = calcTextSize("Advance").x + 1
        withItemWidth(controlsWidth - labelWidth - style.itemSpacing.x) {
            alignTextToFramePadding(); alignedText("Advance", Vec2(1, 0.5), labelWidth); sameLine()
            inputInt("##advance", data::advance)
            alignTextToFramePadding(); alignedText("Auto", Vec2(1, 0.5), labelWidth); sameLine()
            checkbox("##autoAdvance", data::autoAdvance)
        }

        separator()

        labelWidth = calcTextSize("Local Grid").x + 1
        withItemWidth(controlsWidth - labelWidth - style.itemSpacing.x) {
            alignTextToFramePadding(); alignedText("Origin X", Vec2(1, 0.5), labelWidth); sameLine()
            inputInt("Origin X", ::originX)
            alignTextToFramePadding(); alignedText("Origin Y", Vec2(1, 0.5), labelWidth); sameLine()
            inputInt("Origin Y", ::originY)
            alignTextToFramePadding(); alignedText("Scale", Vec2(1, 0.5), labelWidth); sameLine()
            inputInt("Scale", ::granularity)
            alignTextToFramePadding(); alignedText("Grid", Vec2(1, 0.5), labelWidth); sameLine()
            checkbox("##showGrid", ::displayGrid)
            alignTextToFramePadding(); alignedText("Local Grid", Vec2(1, 0.5), labelWidth); sameLine()
            inputInt("##localGridRadius", ::localGridRadius)
            alignTextToFramePadding(); alignedText("Guides", Vec2(1, 0.5), labelWidth); sameLine()
            checkbox("##showGuides", ::displayGuides)
        }

        separator()

        val prevCursor = Vec2(cursorPos)
        alignedText("Reference Font", Vec2(0.5), width = controlsWidth)
        cursorPos = prevCursor
        checkbox("##displayReference", ::displayReference)
        listBox("##style", ::referenceStyle,
            ReferenceFonts.styles, 3)
        val speed = 1f / max(1f, abs(getMouseDragDelta(0).y) / 10)
        alignTextToFramePadding()
        text("Size")
        sameLine()
        withItemWidth(controlsWidth - cursorPosX) {
            dragFloat("Size", ::referenceSize, speed, 1f, 1000f)
        }
        text(ReferenceFonts.style(referenceStyle).fontName(codepoint))

        val bb = Rect(win.dc.cursorPos, win.dc.cursorPos + Vec2(controlsWidth))
        drawReference(bb)
    } }

    fun drawCell(cell: Vec2i, col: Col) {
        drawList.addRectFilled(
            canvas.min + pos(cell),
            canvas.min + pos(cell + Vec2i(1, 1)),
            col.u32
        )
    }

    fun drawReference(bb: Rect) = with(ImGui) {
        itemSize(bb)
        pushClipRect(bb.min, bb.max, true)
        itemAdd(bb, "##referenceImage".hashCode())

        drawList.addRectFilled(
            bb.min,
            bb.max,
            Constants.editorBackground
        )

        val font = ReferenceFonts.style(referenceStyle)[codepoint]
        val bigFont = font.deriveFont(1000f)
        val frc = FontRenderContext(AffineTransform(), true, true)
        val profile = font.glyphProfile(codepoint, 1f)
        if(profile.isEmpty()) return

        val capHeight = bigFont.createGlyphVector(frc, "X").visualBounds.height / 1000
        val lowerHeight = bigFont.createGlyphVector(frc, "x").visualBounds.height / 1000

        val glyphMin = Vec2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        val glyphMax = Vec2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

        profile.forEach { it.forEach { point ->
            glyphMin minAssign point
            glyphMax maxAssign point
        } }

        val visibleBounds = Rect(
            glyphMin min Vec2(0, -capHeight),
            glyphMax max Vec2(0, 0)
        )

        val scales = (bb.size * 0.8) / visibleBounds.size
        val scale = min(scales.x, scales.y)
        val origin = Vec2i(bb.min + bb.size * 0.1 - visibleBounds.min * scale + Vec2(0, bb.height * 0.8 - visibleBounds.height * scale)/2)

        fun guide(height: Float, col: Int) {
            drawList.addLine(
                Vec2(bb.min.x, origin.y + (height * scale).toInt()),
                Vec2(bb.max.x, origin.y + (height * scale).toInt()),
                col,
                1f
            )
        }

        guide(-capHeight.toFloat(), Constants.editorGuides)
        guide(-lowerHeight.toFloat(), Constants.editorGuides)

        guide(0f, Constants.editorAxes)
        drawList.addLine(
            Vec2(origin.x, bb.min.y),
            Vec2(origin.x, bb.max.y),
            Constants.editorAxes,
            1f
        )

        if(codepointChanged) updateReference(font, scale, origin - bb.min)
        drawList.addImage(sidebarReferenceImage.texID, bb.min, bb.max)

        popClipRect()
    }

    fun updateReference(font: java.awt.Font, scale: Float, origin: Vec2i) {
        val g = sidebarReferenceImage.edit(true, true)
        g.font = font.deriveFont(scale)
        g.drawString(String(Character.toChars(codepoint)), origin.x, origin.y)
    }

    var canvasMouseHovered = false
    var canvasKeyFocused = false

    fun drawCanvas() = with(ImGui) {
        itemSize(canvas, style.framePadding.y)
        canvasMouseHovered = itemHoverable(canvas, "editor".hashCode())
        pushClipRect(canvas.min, canvas.max, true)
        if (!itemAdd(canvas, "editor".hashCode())) return
        canvasKeyFocused = isWindowFocused(FocusedFlag.RootAndChildWindows)

        drawList.addRectFilled(canvas.min, canvas.max, Constants.editorBackground)

        if(canvasMouseHovered && isMouseClicked(0)) focus()
        if(canvasKeyFocused) {
            keys {
                if (ifMac("shift+cmd+z", "ctrl+y").pressed()) {
                    data.redo()
                } else if (ifMac("cmd+z", "ctrl+z").pressed()) {
                    data.undo()
                }
                "tab" pressed {
                    if(io.keyShift)
                        codepoint--
                    else
                        codepoint++
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

        fun horizontalLine(pos: Number, col: Int) {
            drawList.addLine(
                Vec2(canvas.min.x, canvas.min.y + (pos.toFloat() + origin.y) * granularity),
                Vec2(canvas.max.x, canvas.min.y + (pos.toFloat() + origin.y) * granularity),
                col,
                1f
            )
        }

        fun verticalLine(pos: Number, col: Int) {
            drawList.addLine(
                Vec2(canvas.min.x + (pos.toFloat() + origin.x) * granularity, canvas.min.y),
                Vec2(canvas.min.x + (pos.toFloat() + origin.x) * granularity, canvas.max.y),
                col,
                1f
            )
        }

        if(displayGrid) {
            for (i in 0..(canvas.height / granularity).toInt()) {
                horizontalLine(i - origin.y, Constants.editorGrid)
            }

            for (i in 0..(canvas.width / granularity).toInt()) {
                verticalLine(i - origin.x, Constants.editorGrid)
            }
        } else if(localGridRadius > 0) {
            val cursorPos = pos(Vec2(0.5, 0.5) + cell(io.mousePos - canvas.min))
            val cursorCell = cell(cursorPos)
            for(i in -localGridRadius .. localGridRadius + 1) {

                val offset = pos(cursorCell + Vec2i(i, i)) - cursorPos
                val radius = localGridRadius*granularity
                if(offset.x < radius) {
                    val length = sqrt(radius * radius - offset.x * offset.x)
                    drawList.addLine(
                        canvas.min + cursorPos + Vec2(offset.x, -length),
                        canvas.min + cursorPos + Vec2(offset.x, length),
                        Constants.editorGrid,
                        1f
                    )
                }
                if(offset.y < radius) {
                    val length = sqrt(radius*radius - offset.y*offset.y)
                    drawList.addLine(
                        canvas.min + cursorPos + Vec2(-length, offset.y),
                        canvas.min + cursorPos + Vec2(length, offset.y),
                        Constants.editorGrid,
                        1f
                    )
                }
            }
        }


        if(displayGuides) {
            horizontalLine(-bitfont.xHeight, Constants.editorGuides)
            horizontalLine(-bitfont.capHeight, Constants.editorGuides)
            horizontalLine(-bitfont.ascender, Constants.editorGuides)
            horizontalLine(bitfont.descender, Constants.editorGuides)

            verticalLine(0, Constants.editorAxes)
            horizontalLine(0, Constants.editorAxes)

            val advanceEnd = Vec2(canvas.min.x + (origin.x + data.glyph.calcAdvance(bitfont.spacing)) * granularity, canvas.min.y + origin.y * granularity)
            drawList.addLine(
                Vec2(canvas.min.x + origin.x * granularity, canvas.min.y + origin.y * granularity),
                advanceEnd,
                Constants.editorAdvance,
                1f
            )

            drawList.addLine(
                advanceEnd - Vec2(0, ceil(granularity/2.0)),
                advanceEnd + Vec2(0, ceil(granularity/2.0)),
                Constants.editorAdvance,
                1f
            )
        }

        (data.enabledCells + nudge.moving).forEach {
            drawCell(it, Col.Text)
        }

        if(displayReference) {
            ReferenceFonts.style(referenceStyle)[codepoint].glyphProfile(codepoint, referenceSize).forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Constants.editorSelection, true, 1f)
            }
        }

        tool.draw()

        popClipRect()
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
            if(canvasKeyFocused && isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                stop()
                tool = marquee
                marquee.update()
                return
            }
            if(canvasMouseHovered) {
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

            if(canvasKeyFocused) {
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
            if(!ImGui.io.keyAlt)
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
            if(canvasMouseHovered) {
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
            if(canvasMouseHovered && isMouseDown(0)) {
                val start = start
                selecting.clear()
                if(start != null) {
                    for (x in min(start.x, mouseCell.x)..max(start.x, mouseCell.x)) {
                        for (y in min(start.y, mouseCell.y)..max(start.y, mouseCell.y)) {
                            selecting.add(Vec2i(x, y))
                        }
                    }
                }
            }
            if(canvasKeyFocused) {
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
                    (selected.intersect(data.enabledCells).isNotEmpty() && io.primaryModifier) and "c" pressed {
                        clipboard.clear()
                        clipboard.addAll(selected.intersect(data.enabledCells))
                        selected.clear()
                    }
                    (selected.intersect(data.enabledCells).isNotEmpty() && io.primaryModifier) and "x" pressed {
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

            if(canvasMouseHovered) {
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
