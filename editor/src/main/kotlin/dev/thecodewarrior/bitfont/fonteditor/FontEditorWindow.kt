package dev.thecodewarrior.bitfont.fonteditor

import com.ibm.icu.lang.UCharacter
import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.HistoryTracker
import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import dev.thecodewarrior.bitfont.fonteditor.utils.NkEditor
import dev.thecodewarrior.bitfont.fonteditor.utils.Rect2i
import dev.thecodewarrior.bitfont.fonteditor.utils.Vec2i
import dev.thecodewarrior.bitfont.fonteditor.utils.contours
import dev.thecodewarrior.bitfont.fonteditor.utils.nk_quick_keys
import dev.thecodewarrior.bitfont.fonteditor.widgets.ReferenceGlyphWidget
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkStyleButton
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FontEditorWindow(val data: BitfontEditorData, val editorData: GlyphEditorData): Window(700f, 500f, false) {
    var closed = false

    private var codepoint: Int = 0
        set(value) {
            if(value < 0 || value > 0x10ffff)
                return
            if(field != value) {
                if(tool === nudge) nudge.stop()
                field = value
                editorData.release(glyphData)
                brush.mouseReleasedPos = null
                glyphData = editorData.retain(codepoint)
                codepointField.text = "%04X".format(codepoint)
                title = "${data.font.name}: U+%04X - %s".format(codepoint, UCharacter.getName(codepoint))
                referenceWidget.codepoint = codepoint
            }
        }

    var glyphData = editorData.retain(0)
    var codepointHistory = HistoryTracker<Int>(100, 65)

    val brush = BrushTool()
    val marquee: MarqueeTool = MarqueeTool()
    val nudge: NudgeTool = NudgeTool()

    var tool: EditorTool = brush

    private var zoom = 30
        set(value) {
            if(field != value) {
                field = value
                referenceWidget.codepoint = codepoint
            }
        }
    /**
     * One pixel
     */
    private val px get() = 1.0/zoom
    private var viewX = 3
    private var viewY = 12

    private var canvasWidth: Float = 0f
    private var canvasHeight: Float = 0f
    private var mouseX: Float = 0f
    private var mouseY: Float = 0f

    private var bounds: Rect2i = Rect2i(0, 0, 0, 0)
    private var mousePos: Vec2i = Vec2i(0, 0)
    private var isMouseOverCanvas = false
    private var isCanvasFocused = false

    var codepointField: NkEditor = NkEditor(NK_EDIT_FIELD or NK_EDIT_SIG_ENTER)
    val referenceWidget = ReferenceGlyphWidget()

    private val backgroundColor = Color(0x0A0A0A)
    private val gridColor = Color(0x3b3b46)
    private val axisColor = Color(0xf58231)
    private val selectionColor = Color(0xf032e6)
    private val guidesColor = Color(0x4363d8)
    private val markingsColor = Color(0x3cb44b)
    private val toolColor = Color(0xff7964)
    private val glyphColor = Color(0xffffff)

    init {
        flags = flags or NK_WINDOW_SCALABLE or NK_WINDOW_CLOSABLE
        codepoint = 'A'.code
        closeOnHide = true
        referenceWidget.guides = ReferenceGlyphWidget.GuideSet(thickness = 2f, all = true, whileBlank = false)
    }

    fun pushControls(ctx: NkContext) {
        if(isWindowFocused) {
            nk_quick_keys {
                "tab" pressed {
                    if("shift".pressed())
                        codepoint--
                    else
                        codepoint++
                    codepointHistory.push(codepoint)
                }
                "shift+h" pressed {
                    codepointHistory.undo()
                }
                "shift+l" pressed {
                    codepointHistory.redo()
                }
            }
        }

        MemoryStack.stackPush().use { stack ->
            val groupRegion = NkRect.mallocStack(stack)
            nk_window_get_content_region(ctx, groupRegion)

            val style = NkStyleButton.mallocStack(stack)
            style.set(ctx.style().button())
            style.padding(NkVec2.mallocStack(stack).set(0f, 0f))

            nk_layout_row_begin(ctx, NK_STATIC, 20f, 3)
            nk_layout_row_push(ctx, 20f)
            if(nk_button_symbol_styled(ctx, style, NK_SYMBOL_TRIANGLE_LEFT)) {
                codepoint--
            }
            nk_layout_row_push(ctx, groupRegion.w() - 40 - 16)
            run {
                val widgetBounds = NkRect.mallocStack(stack)
                nk_widget_bounds(ctx, widgetBounds)
                if(nk_input_is_mouse_hovering_rect(ctx.input(), widgetBounds)) {
                    val scroll = ctx.input().mouse().scroll_delta().y()
                    if(scroll < 0f) {
                        codepoint++
                    } else if(scroll > 0f) {
                        codepoint--
                    }
                }
            }
            if(codepointField.push(ctx) and NK_EDIT_COMMITED != 0) {
                nk_edit_unfocus(ctx)
                codepoint = codepointField.text.toInt(16)
            }
            nk_layout_row_push(ctx, 20f)
            if(nk_button_symbol_styled(ctx, style, NK_SYMBOL_TRIANGLE_RIGHT)) {
                codepoint++
            }
            nk_layout_row_end(ctx)

            nk_layout_row_dynamic(ctx, 20f, 1)

            val metric = stack.ints(0)

            metric.put(0, glyphData.glyph.advance)
            nk_property_int(ctx, "Advance:", -data.font.ascent * 3, metric, data.font.ascent * 3, 1, 1f)
            glyphData.glyph.advance = metric[0]

            val maxViewX = max(10, canvasWidth.toInt())
            if(viewX > maxViewX) viewX = maxViewX
            metric.put(0, viewX)
            nk_property_int(ctx, "View X:", 0, metric, maxViewX, 1, 1f)
            viewX = metric[0]

            val maxViewY = max(10, canvasHeight.toInt())
            if(viewY > maxViewY) viewY = maxViewY
            metric.put(0, viewY)
            nk_property_int(ctx, "View Y:", 0, metric, maxViewY, 1, 1f)
            viewY = metric[0]

            metric.put(0, zoom)
            nk_property_int(ctx, "Zoom:", 1, metric, 50, 1, 1f)
            zoom = metric[0]

            nk_layout_row_dynamic(ctx, 180f, 1)
            referenceWidget.push(ctx)
        }
    }

    fun updateCanvas(ctx: NkContext) {
        if(isWindowFocused) {
            nk_quick_keys {
                if (Input.REDO_KEY.pressed()) {
                    glyphData.redo()
                } else if (Input.UNDO_KEY.pressed()) {
                    glyphData.undo()
                }
                val newTool = when {
                    "b".pressed() -> brush.apply { eraser = false }
                    "e".pressed() -> brush.apply { eraser = true }
                    "m|prim+r".pressed() -> marquee
                    "prim+v".pressed() -> {
                        nudge.stop()
                        if (editorData.clipboard.isEmpty()) {
                            tool
                        } else {
                            nudge.moving.addAll(editorData.clipboard)
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

        tool.update(ctx)
    }

    override fun onClose(ctx: NkContext) {
        closed = true
    }

    fun draw(drawList: DrawList) {
        drawList.fillRect(-viewX, -viewY, canvasWidth, canvasHeight, 0, backgroundColor)
        drawGrid(drawList)
        drawMarkings(drawList)
        drawGlyph(drawList)
    }

    fun drawGlyph(drawList: DrawList) {
        for(cell in glyphData.enabledCells) {
            drawList.fillRect(
                cell.x, cell.y,
                1, 1,
                0, glyphColor
            )
        }
    }

    fun drawGrid(drawList: DrawList) {
        val verticalGuides = listOf<Int>()
        val horizontalGuides = listOf(-data.font.ascent, data.font.descent)

        for(x in bounds.minX .. bounds.maxX) {
            if(x == 0) continue
            drawList.strokeLine(x, bounds.minY, x, bounds.maxY, 2, if(x in verticalGuides) guidesColor else gridColor)
        }
        for(y in bounds.minY .. bounds.maxY) {
            if(y == 0) continue
            drawList.strokeLine(bounds.minX, y, bounds.maxX, y, 2, if(y in horizontalGuides) guidesColor else gridColor)
        }

        drawList.strokeLine(0, bounds.minY, 0, bounds.maxY, 2, axisColor)
        drawList.strokeLine(bounds.minX, 0, bounds.maxX, 0, 2, axisColor)
    }

    fun drawMarkings(drawList: DrawList) {
        drawList.strokeLine(0, 0, glyphData.glyph.advance, 0, 2, markingsColor)
        drawList.strokeLine(glyphData.glyph.advance, -0.25, glyphData.glyph.advance, 0.25 + px, 2, markingsColor)
    }

    // ========= implementation stuff =========

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row(ctx, NK_STATIC, layoutRegion.h(), floatArrayOf(
                200f,
                layoutRegion.w() - 200f
            ))

            if(nk_group_begin(ctx, "controls", NK_WINDOW_BORDER)) {
                pushControls(ctx)
                nk_group_end(ctx)
            }

            val canvasBounds = NkRect.mallocStack(stack).set(
                layoutRegion.x() + 200 + ctx.style().window().group_padding().x(), layoutRegion.y(),
                layoutRegion.w() - 200, layoutRegion.h()
            )

            val nkPos = ctx.input().mouse().pos()
            this.mouseX = (nkPos.x() - canvasBounds.x()) / zoom - viewX
            this.mouseY = (nkPos.y() - canvasBounds.y()) / zoom - viewY
            this.canvasWidth = canvasBounds.w() / zoom
            this.canvasHeight = canvasBounds.h() / zoom
            this.isMouseOverCanvas = nkPos.x() in canvasBounds.x() .. canvasBounds.x() + canvasBounds.w() &&
                nkPos.y() in canvasBounds.y() .. canvasBounds.y() + canvasBounds.h()

            // immediately lose focus
            if((Input.isMousePressed(0) || Input.isMousePressed(1)) && !this.isMouseOverCanvas || !this.isWindowFocused) {
                this.isCanvasFocused = false
            }

            this.mousePos = Vec2i(floor(mouseX).toInt(), floor(mouseY).toInt())
            this.bounds = Rect2i(-viewX, -viewY, ceil(canvasWidth).toInt(), ceil(canvasHeight).toInt())

            updateCanvas(ctx)

            val drawList = DrawList()
            drawList.clip(-viewX, -viewY, canvasWidth, canvasHeight)
            draw(drawList)
            tool.draw(drawList)
            drawList.transformX = canvasBounds.x() + viewX * zoom
            drawList.transformY = canvasBounds.y() + viewY * zoom
            drawList.transformScale = zoom.toFloat()
            drawList.push(ctx)

            nk_layout_row_end(ctx)

            // regain focus at the end of the frame
            if((Input.isMousePressed(0) || Input.isMousePressed(1)) && this.isMouseOverCanvas && this.isWindowFocused) {
                this.isCanvasFocused = true
            }
        }
    }

    override fun free() {
        editorData.release(glyphData)
        referenceWidget.free()
    }

    interface EditorTool {
        fun update(ctx: NkContext)
        fun draw(drawList: DrawList)
        fun stop()
    }

    inner class NudgeTool: EditorTool {
        val moving = mutableSetOf<Vec2i>()
        var isDragging = false
        var draggingGripX: Float = 0f
        var draggingGripY: Float = 0f

        override fun update(ctx: NkContext) {
            if(isCanvasFocused && Input.isKeyPressed(GLFW_KEY_ESCAPE)) {
                stop()
                tool = marquee
                marquee.update(ctx)
                return
            }
            if (isMouseOverCanvas) {
                if(Input.isMousePressed(0)) {
                    if(mousePos in moving) {
                        isDragging = true
                        draggingGripX = mouseX
                        draggingGripY = mouseY
                    } else {
                        stop()
                        tool = marquee
                        marquee.update(ctx)
                        return
                    }
                }
            }
            if(Input.isMouseReleased(0)) {
                isDragging = false
            }

            val draggingOffsetX = (mouseX - draggingGripX).roundToInt()
            val draggingOffsetY = (mouseY - draggingGripY).roundToInt()

            if(isDragging && (draggingOffsetX != 0 || draggingOffsetY != 0)) {
                offset(Vec2i(draggingOffsetX, draggingOffsetY))
                draggingGripX += draggingOffsetX
                draggingGripY += draggingOffsetY
            }

            if(isCanvasFocused) {
                nk_quick_keys {
                    "left" pressed { offset(Vec2i(-1, 0)) }
                    "right" pressed { offset(Vec2i(1, 0)) }
                    "up" pressed { offset(Vec2i(0, -1)) }
                    "down" pressed { offset(Vec2i(0, 1)) }

                    if (moving.isNotEmpty()) {
                        "prim+c" pressed {
                            editorData.clipboard.clear()
                            editorData.clipboard.addAll(moving)
                        }
                        "prim+x" pressed {
                            editorData.clipboard.clear()
                            editorData.clipboard.addAll(moving)
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

        override fun draw(drawList: DrawList) {
            for(point in moving) {
                drawList.fillRect(point.x, point.y, 1, 1, 0, glyphColor)
            }
            for(contour in moving.contours()) {
                val polygon = drawList.strokePolygon(2, selectionColor)
                for(point in contour) {
                    polygon.point(point.xf, point.yf)
                }
                polygon.finish()
            }
        }

        fun start(selected: Collection<Vec2i>) {
            moving.addAll(selected.intersect(glyphData.enabledCells))
            if(!Input.isModifierDown(GLFW_MOD_ALT))
                if(glyphData.enabledCells.removeAll(moving)) glyphData.pushHistory()
        }

        override fun stop() {
            if(glyphData.enabledCells.addAll(moving)) glyphData.pushHistory()
            moving.clear()
            isDragging = false
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
        var downX: Float = 0f
        var downY: Float = 0f

        override fun update(ctx: NkContext) {
            if(isMouseOverCanvas) {
                if (Input.isMousePressed(0)) {
                    if(mousePos in selected && Input.isModifierDown(Input.PRIMARY_MODIFIER)) {
                        tool = nudge
                        nudge.start(selected)
                        selected.clear()
                        nudge.draggingGripX = mouseX
                        nudge.draggingGripY = mouseY
                        nudge.isDragging = true
                        nudge.update(ctx)
                        return
                    } else {
                        downX = mouseX
                        downY = mouseY
                        start = mousePos
                    }
                }
            }
            if (Input.isMouseReleased(0)) {
                stop()
            }
            if(isMouseOverCanvas && Input.isMouseDown(0)) {
                val start = start
                selecting.clear()
                if(start != null) {
                    for (x in min(start.x, mousePos.x)..max(start.x, mousePos.x)) {
                        for (y in min(start.y, mousePos.y)..max(start.y, mousePos.y)) {
                            selecting.add(Vec2i(x, y))
                        }
                    }
                }
            }
            if(isCanvasFocused) {
                nk_quick_keys {
                    "prim+(left|right|up|down)" pressed {
                        tool = nudge
                        nudge.start(selected)
                        selected.clear()
                        nudge.update(ctx)
                        return
                    }
                    "backspace" pressed {
                        if (glyphData.enabledCells.removeAll(selected)) glyphData.pushHistory()
                        selected.clear()
                    }
                    "escape" pressed {
                        selected.clear()
                    }
                    selected.intersect(glyphData.enabledCells).isNotEmpty() and "prim+c" pressed {
                        editorData.clipboard.clear()
                        editorData.clipboard.addAll(selected.intersect(glyphData.enabledCells))
                        selected.clear()
                    }
                    selected.intersect(glyphData.enabledCells).isNotEmpty() and "prim+x" pressed {
                        editorData.clipboard.clear()
                        editorData.clipboard.addAll(selected.intersect(glyphData.enabledCells))
                        if (glyphData.enabledCells.removeAll(editorData.clipboard)) glyphData.pushHistory()
                        selected.clear()
                    }
                    "left" pressed { offset(Vec2i(-1, 0)) }
                    "right" pressed { offset(Vec2i(1, 0)) }
                    "up" pressed { offset(Vec2i(0, -1)) }
                    "down" pressed { offset(Vec2i(0, 1)) }
                }
            }
        }

        override fun draw(drawList: DrawList) {
            for(contour in selected.contours()) {
                val polygon = drawList.strokePolygon(2, selectionColor)
                for(point in contour) {
                    polygon.point(point.xf, point.yf)
                }
                polygon.finish()
            }
            for(contour in selecting.contours()) {
                val polygon = drawList.strokePolygon(2, selectionColor)
                for(point in contour) {
                    polygon.point(point.xf, point.yf)
                }
                polygon.finish()
            }
        }

        override fun stop() {
            this@MarqueeTool.start = null
            when {
                Input.isModifierDown(GLFW_MOD_SHIFT) -> selected.addAll(selecting)
                Input.isModifierDown(GLFW_MOD_ALT) -> selected.removeAll(selecting)
                mouseX == downX && mouseY == downY -> selected.clear()
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
        var previousPosition = Vec2i(0, 0)

        override fun update(ctx: NkContext) {
            if(isMouseOverCanvas) {
                if (Input.isMousePressed(0)) {
                    drawingState = !eraser
                    if (Input.isModifierDown(GLFW_MOD_SHIFT)) {
                        previousPosition = mouseReleasedPos ?: previousPosition
                    }
                    modified = false
                }
                if (Input.isMousePressed(1)) {
                    drawingState = eraser
                    if (Input.isModifierDown(GLFW_MOD_SHIFT)) {
                        previousPosition = mouseReleasedPos ?: previousPosition
                    }
                    modified = false
                }
            }
            if (Input.isMouseReleased(0) || Input.isMouseReleased(1)) {
                drawingState = null
                if(modified) glyphData.pushHistory()
                modified = false
            }

            if(drawingState != null)
                mouseReleasedPos = mousePos
            val cells = previousPosition.lineTo(mousePos)
            if (drawingState == true) {
                modified = glyphData.enabledCells.addAll(cells) || modified
            } else if (drawingState == false) {
                modified = glyphData.enabledCells.removeAll(cells) || modified
            }
            previousPosition = mousePos
        }

        override fun draw(drawList: DrawList) {
            drawList.fillRect(mousePos.x, mousePos.y, 1, 1, 0, toolColor)
            mouseReleasedPos?.also { mouseReleasedPos ->
                if(Input.isModifierDown(GLFW_MOD_SHIFT)) {
                    drawList.strokeLine(
                        mouseReleasedPos.x + 0.5, mouseReleasedPos.y + 0.5,
                        mousePos.x + 0.5, mousePos.y + 0.5,
                        2, toolColor
                    )
                }
            }
        }

        override fun stop() {
            drawingState = null
        }
    }
}
