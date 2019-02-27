package games.thecodewarrior.bitfont.editor

import com.ibm.icu.lang.UCharacter
import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.data.BitGrid
import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.HistoryTracker
import games.thecodewarrior.bitfont.editor.utils.ReferenceFonts
import games.thecodewarrior.bitfont.editor.utils.alignedText
import games.thecodewarrior.bitfont.editor.utils.contours
import games.thecodewarrior.bitfont.editor.utils.extensions.ImGuiDrags
import games.thecodewarrior.bitfont.editor.utils.extensions.JColor
import games.thecodewarrior.bitfont.editor.utils.extensions.lineTo
import games.thecodewarrior.bitfont.editor.utils.extensions.primaryModifier
import games.thecodewarrior.bitfont.editor.utils.extensions.set
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.editor.utils.glyphProfile
import games.thecodewarrior.bitfont.editor.utils.ifMac
import games.thecodewarrior.bitfont.editor.utils.keys
import games.thecodewarrior.bitfont.editor.utils.opengl.Java2DTexture
import glm_.func.common.clamp
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Col
import imgui.Dir
import imgui.FocusedFlag
import imgui.ImGui
import imgui.functionalProgramming.withChild
import imgui.functionalProgramming.withItemWidth
import imgui.functionalProgramming.button
import imgui.imgui.withFloat
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
        get() = if(index < 0)
            "${bitfont.name}: Special"
        else
            "${bitfont.name}: U+%04X - %s".format(index, UCharacter.getName(index))

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

    var index: Int = 0
        set(value) {
            if(field != value.clamp(Int.MIN_VALUE, 0x10FFFF)) {
                if(tool === nudge) nudge.stop()
                if(data == Data(index)) {
                    dataMap.remove(index)
                }
                indexChanged = true
            }
            field = value.clamp(Int.MIN_VALUE, 0x10FFFF)
            data = dataMap.getOrPut(index) { Data(index) }
        }

    var indexChanged = false
    val sidebarReferenceImage = Java2DTexture(controlsWidth.toInt(), controlsWidth.toInt())
    var displayReference = false
    var displayGrid = true
    var displayGuides = true
    var localGridRadius = 0
        set(value) { field = value.clamp(0, 100) }

    init {
        index = 65
    }

    var horizontalGuides = mutableListOf<Float>()
    var verticalGuides = mutableListOf<Float>()

    inner class Data(val index: Int) {
        val glyph = bitfont.glyphs.getOrPut(index) { Glyph() }
        val enabledCells = mutableSetOf<Vec2i>()

        val history: HistoryTracker<State>

        var advance: Int
            get() = glyph.calcAdvance(bitfont.spacing)
            set(value) { glyph.advance = value }
        var autoAdvance: Boolean
            get() = glyph.advance == null
            set(value) { glyph.advance = if(value) null else advance }

        init {
            updateFromGlyph()
            history = HistoryTracker(100, State())
        }

        fun pushHistory() {
            history.push(State())
            updateGlyph()
        }

        fun undo() {
            history.undo().apply()
            updateGlyph()
        }

        fun redo() {
            history.redo().apply()
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
            var maxX = Int.MIN_VALUE
            var minY = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE
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
                    if(grid[x, y]) {
                        enabledCells.add(Vec2i(x + glyph.bearingX, y + glyph.bearingY))
                    }
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Data) return false

            if (index != other.index) return false
            if (glyph != other.glyph) return false
            if (enabledCells != other.enabledCells) return false
            if (history != other.history) return false

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + glyph.hashCode()
            result = 31 * result + enabledCells.hashCode()
            result = 31 * result + history.hashCode()
            return result
        }
    }

    override fun main() = with(ImGui) {
        val wasChanged = indexChanged
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
            indexChanged = false
    }

    fun drawControls() = with(ImGui) { withItemWidth(controlsWidth) {
        pushAllowKeyboardFocus(false)

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

        text("Horizontal")
        horizontalGuides.forEachIndexed { i, value ->
            val arr = floatArrayOf(value)
            withFloat(arr, 0) { prop ->
                dragFloat("##horizontal.$i", prop, 0.5f, -100f, 100f)
            }
            horizontalGuides[i] = arr[0]
        }
        button("+##horizontal") {
            horizontalGuides.add(0f)
        }

        text("Vertical")
        verticalGuides.forEachIndexed { i, value ->
            val arr = floatArrayOf(value)
            withFloat(arr, 0) { prop ->
                dragFloat("##vertical.$i", prop, 0.5f, -100f, 100f)
            }
            verticalGuides[i] = arr[0]
        }
        button("+##vertical") {
            verticalGuides.add(0f)
        }

        separator()

        if(index >= 0) {
            pushButtonRepeat(true)
            if (arrowButton("##left", Dir.Left)) index--
            sameLine()
            withItemWidth(controlsWidth - frameHeight*2 - style.itemSpacing.x*2) {
                val speed = max(1.0, abs(getMouseDragDelta(0).y) / 10.0)
                ImGuiDrags.dragScalar(
                    label = "##codepointDrag",
                    value = ::index,
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
            if (arrowButton("##right", Dir.Right)) index++
            popButtonRepeat()

            val prevCursor = Vec2(cursorPos)
            alignedText("Reference Font", Vec2(0.5), width = controlsWidth)
            cursorPos = prevCursor
            checkbox("##displayReference", ::displayReference)
            text(ReferenceFonts.style(document.referenceStyle).fontName(index))

            val bb = Rect(win.dc.cursorPos, win.dc.cursorPos + Vec2(controlsWidth))
            drawReference(bb)
        }
        popAllowKeyboardFocus()
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
            Colors.editor.background.u32
        )

        if(indexChanged) updateReference()
        drawList.addImage(sidebarReferenceImage.texID, bb.min, bb.max)

        popClipRect()
    }

    fun updateReference() {
        val g = sidebarReferenceImage.edit(true, true)
        val size = Vec2(sidebarReferenceImage.width, sidebarReferenceImage.height)

        val chr = String(Character.toChars(index))
        val frc = FontRenderContext(AffineTransform(), true, true)

        val font = ReferenceFonts.style(document.referenceStyle)[index]
        val bigFont = font.deriveFont(1000f)

        val (ascent, descent) = bigFont.getLineMetrics(chr, frc).let { it.ascent/1000 to it.descent/1000 }

        val capHeight = bigFont.createGlyphVector(frc, "X").visualBounds.height / 1000
        val lowerHeight = bigFont.createGlyphVector(frc, "x").visualBounds.height / 1000
        val descender = bigFont.createGlyphVector(frc, "gjpqy").visualBounds.maxY / 1000

        val bounds = bigFont.createGlyphVector(frc, chr).visualBounds.let {
            Rect(it.minX.toFloat()/1000, it.minY.toFloat()/1000, it.maxX.toFloat()/1000, it.maxY.toFloat()/1000)
        }

        bounds.min = bounds.min min Vec2(0, 0) min Vec2(0, -ascent)
        bounds.max = bounds.max max Vec2(0, 0) max Vec2(0, descent)

        val scales = (size * 0.8) / bounds.size
        val scale = min(scales.x, scales.y)
        val origin = Vec2i(size * 0.1 - bounds.min * scale + Vec2(0, size.y * 0.8 - bounds.height * scale)/2)

        fun hGuide(position: Float, col: JColor) {
            g.color = col
            val y = (position*scale + origin.y).toInt()
            g.drawLine(0, y, size.x.toInt(), y)
        }

        fun vGuide(position: Float, col: JColor) {
            g.color = col
            val x = (position*scale + origin.x).toInt()
            g.drawLine(x, 0, x, size.y.toInt())
        }

        hGuide(-capHeight.toFloat(), Colors.editor.referencePanel.guides)
        hGuide(-lowerHeight.toFloat(), Colors.editor.referencePanel.guides)
        hGuide(descender.toFloat(), Colors.editor.referencePanel.guides)
        hGuide(0f, Colors.editor.referencePanel.axes)

        vGuide(0f, Colors.editor.referencePanel.axes)

        g.color = Colors.editor.referencePanel.glyph
        g.font = font.deriveFont(scale)
        g.drawString(String(Character.toChars(index)), origin.x, origin.y)
    }

    var canvasMouseHovered = false
    var canvasKeyFocused = false

    fun drawCanvas() = with(ImGui) {
        itemSize(canvas, style.framePadding.y)
        canvasMouseHovered = itemHoverable(canvas, "editor".hashCode())
        pushClipRect(canvas.min, canvas.max, true)
        if (!itemAdd(canvas, "editor".hashCode())) return
        canvasKeyFocused = isWindowFocused(FocusedFlag.RootAndChildWindows)

        drawList.addRectFilled(canvas.min, canvas.max, Colors.editor.background.u32)

        if(canvasMouseHovered && isMouseClicked(0)) focus()
        if(canvasKeyFocused) {
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

        fun horizontalLine(pos: Number, col: Int) {
            drawList.addLine(
                Vec2(canvas.min.x, canvas.min.y + ((pos.toFloat() + origin.y) * granularity).toInt()),
                Vec2(canvas.max.x, canvas.min.y + ((pos.toFloat() + origin.y) * granularity).toInt()),
                col,
                1f
            )
        }

        fun verticalLine(pos: Number, col: Int) {
            drawList.addLine(
                Vec2(canvas.min.x + ((pos.toFloat() + origin.x) * granularity).toInt(), canvas.min.y),
                Vec2(canvas.min.x + ((pos.toFloat() + origin.x) * granularity).toInt(), canvas.max.y),
                col,
                1f
            )
        }

        if(displayGrid) {
            for (i in 0..(canvas.height / granularity).toInt()) {
                horizontalLine(i - origin.y, Colors.editor.grid.u32)
            }

            for (i in 0..(canvas.width / granularity).toInt()) {
                verticalLine(i - origin.x, Colors.editor.grid.u32)
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
                        Colors.editor.grid.u32,
                        1f
                    )
                }
                if(offset.y < radius) {
                    val length = sqrt(radius*radius - offset.y*offset.y)
                    drawList.addLine(
                        canvas.min + cursorPos + Vec2(-length, offset.y),
                        canvas.min + cursorPos + Vec2(length, offset.y),
                        Colors.editor.grid.u32,
                        1f
                    )
                }
            }
        }


        if(displayGuides) {
            horizontalLine(-bitfont.xHeight, Colors.editor.guides.u32)
            horizontalLine(-bitfont.capHeight, Colors.editor.guides.u32)
            horizontalLine(-bitfont.ascent, Colors.editor.guides.u32)
            horizontalLine(bitfont.descent, Colors.editor.guides.u32)

            val advanceEnd = Vec2(canvas.min.x + (origin.x + data.glyph.calcAdvance(bitfont.spacing)) * granularity, canvas.min.y + origin.y * granularity)
            drawList.addLine(
                Vec2(canvas.min.x + origin.x * granularity, canvas.min.y + origin.y * granularity),
                advanceEnd,
                Colors.editor.advance.u32,
                1f
            )

            drawList.addLine(
                advanceEnd - Vec2(0, ceil(granularity/2.0)),
                advanceEnd + Vec2(0, ceil(granularity/2.0)),
                Colors.editor.advance.u32,
                1f
            )
        }


        horizontalGuides.forEach {
            horizontalLine(it, Colors.maroon.u32)
        }
        verticalGuides.forEach {
            verticalLine(it, Colors.maroon.u32)
        }

        verticalLine(0, Colors.editor.axes.u32)
        horizontalLine(0, Colors.editor.axes.u32)


        (data.enabledCells + nudge.moving).forEach {
            drawCell(it, Col.Text)
        }

        if(displayReference) {
            ReferenceFonts.style(document.referenceStyle)[index].glyphProfile(index, document.referenceSize).forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Colors.editor.selection.u32, true, 1f)
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
                }), Colors.editor.selection.u32, true, 2f)
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
                }), Colors.editor.selection.u32, true, 2f)
            }

            selecting.contours().forEach { contour ->
                drawList.addPolyline(ArrayList(contour.map {
                    canvas.min + pos(it)
                }), Colors.editor.selection.u32, true, 2f)
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
                if (isMouseClicked(1)) {
                    drawingState = eraser
                    if (io.keyShift)
                        mouseCellPrev = mouseReleasedPos ?: mouseCellPrev
                    modified = false
                }
            }
            if (isMouseReleased(0) || isMouseReleased(1)) {
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
