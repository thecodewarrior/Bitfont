package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color

class FontEditorWindow(val data: BitfontEditorData): Window(500f, 300f) {
    private var glyph: Glyph = Glyph(data.font)

    private var codepoint: Int = 0
        set(value) {
            field = value
            glyph = data.font.glyphs[value] ?: Glyph(data.font)
        }

    private var zoom = 30
    private val dd get() = 1.0/zoom
    private var viewX = 3
    private var viewY = 12

    private val backgroundColor = Color(0x0A0A0A)
    private val gridColor = Color(0x3b3b46)
    private val axisColor = Color(0xf58231)
    private val selectionColor = Color(0xf032e6)
    private val guidesColor = Color(0x4363d8)
    private val markingsColor = Color(0x3cb44b)
    private val cursorColor = Color(0xff7964)
    private val glyphColor = Color(0xffffff)

    init {
        flags = flags or NK_WINDOW_NO_SCROLLBAR or NK_WINDOW_SCALABLE or NK_WINDOW_CLOSABLE
        codepoint = 'A'.code
    }

    fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 20f, 1)
            nk_label(ctx, "Font Name:", NK_TEXT_LEFT)

            val metric = stack.ints(0)

            metric.put(0, codepoint)
            nk_property_int(ctx, "Codepoint:", 0, metric, 100, 1, 1f)
            codepoint = metric[0]
        }
    }

    fun draw(width: Float, height: Float, mouseX: Float, mouseY: Float, drawList: DrawList) {
        drawList.fillRect(0, 0, width, height, 0, backgroundColor)
        drawGrid(width, height, mouseX, mouseY, drawList)
        drawMarkings(width, height, mouseX, mouseY, drawList)
        drawGlyph(width, height, mouseX, mouseY, drawList)
    }

    fun drawGlyph(width: Float, height: Float, mouseX: Float, mouseY: Float, drawList: DrawList) {
        val grid = glyph.image
        for(x in 0 until grid.width) {
            for(y in 0 until grid.height) {
                if(grid[x, y]) {
                    drawList.fillRect(
                        viewX + glyph.bearingX + x, viewY + glyph.bearingY + y,
                        1, 1,
                        0, glyphColor
                    )
                }
            }
        }
    }

    fun drawGrid(width: Float, height: Float, mouseX: Float, mouseY: Float, drawList: DrawList) {
        val verticalGuides = listOf<Int>()
        val horizontalGuides = listOf(-data.font.ascent, data.font.descent)

        for(x in 1 .. width.toInt()) {
            if(x == viewX) continue
            drawList.strokeLine(x, 0, x, height, 2, if(x - viewX in verticalGuides) guidesColor else gridColor)
        }
        for(y in 1 .. height.toInt()) {
            if(y == viewY) continue
            drawList.strokeLine(0, y, width, y, 2, if(y - viewY in horizontalGuides) guidesColor else gridColor)
        }

        drawList.strokeLine(viewX, 0, viewX, height, 2, axisColor)
        drawList.strokeLine(0, viewY, width, viewY, 2, axisColor)
    }

    fun drawMarkings(width: Float, height: Float, mouseX: Float, mouseY: Float, drawList: DrawList) {
        drawList.strokeLine(viewX, viewY, viewX + glyph.advance, viewY, 2, markingsColor)
        drawList.strokeLine(viewX + glyph.advance, viewY - 0.25, viewX + glyph.advance, viewY + 0.25 + dd, 2, markingsColor)
    }

    // ========= implementation stuff =========

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val contentRegion = NkRect.mallocStack(stack)
            nk_window_get_content_region(ctx, contentRegion)
            val padding = ctx.style().window().padding()
            contentRegion.set(
                contentRegion.x() + padding.x(),
                contentRegion.y() + padding.y(),
                contentRegion.w() - 2 * padding.x(),
                contentRegion.h() - padding.y()
            )

            nk_layout_row(ctx, NK_STATIC, contentRegion.h(), floatArrayOf(
                200f,
                contentRegion.w() - 200f
            ))

            if(nk_group_begin(ctx, "controls", NK_WINDOW_BORDER)) {
                pushControls(ctx)
                nk_group_end(ctx)
            }

            val groupPadding = ctx.style().window().group_padding()
            val oldGroupPadding = NkVec2.mallocStack(stack).set(groupPadding)
            groupPadding.set(0f, 0f)

            if(nk_group_begin(ctx, "editor", NK_WINDOW_BORDER or NK_WINDOW_NO_SCROLLBAR )) {
                val groupRegion = NkRect.mallocStack(stack)
                nk_window_get_content_region(ctx, groupRegion)

                val mousePos = ctx.input().mouse().pos()
                val mouseX = (mousePos.x() - groupRegion.x()) / zoom
                val mouseY = (mousePos.y() - groupRegion.y()) / zoom

                val drawList = DrawList()
                draw(groupRegion.w() / zoom, groupRegion.h() / zoom, mouseX, mouseY, drawList)
                drawList.transformX = groupRegion.x()
                drawList.transformY = groupRegion.y()
                drawList.transformScale = zoom.toFloat()
                drawList.push(ctx)

                nk_group_end(ctx)
            }

            ctx.style().window().group_padding().set(oldGroupPadding)

            nk_layout_row_end(ctx)
        }
    }

    override fun free() {
    }
}
