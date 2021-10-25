package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.NkEditor
import dev.thecodewarrior.bitfont.fonteditor.utils.NkGroupInfo
import dev.thecodewarrior.bitfont.fonteditor.utils.nk_quick_group
import dev.thecodewarrior.bitfont.fonteditor.utils.nkutil_widget_is_hovered
import dev.thecodewarrior.bitfont.fonteditor.widgets.ReferenceGlyphWidget
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkStyleButton
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color

class UnicodeBrowserWindow(val data: BitfontEditorData) : Window(512f + 17 + 200 + 8, 512f + 17, false) {
    val gridSize = 32f
    val references = List(256) { createReferenceWidget() }
    var selected = -1

    var page = -1
        set(value) {
            if(value < 0 || value > 0x10ff)
                return
            if(field != value) {
                field = value
                references.forEachIndexed { i, reference -> reference.codepoint = page * 256 + i }
                pageField.text = "%02X".format(page)
                title = "%s: U+%02X00–%02XFF".format(data.font.name, page, page)
            }
        }
    var pageField: NkEditor = NkEditor(NK_EDIT_FIELD or NK_EDIT_SIG_ENTER)

    var selection: Int = -1

    init {
        flags = flags or NK_WINDOW_CLOSABLE
        page = 0
    }

    private fun createReferenceWidget(): ReferenceGlyphWidget {
        val reference = ReferenceGlyphWidget()
        reference.guides = ReferenceGlyphWidget.GuideSet(whileBlank = false, baseline = true)
        return reference
    }

    override fun pushContents(ctx: NkContext) {
        nk_layout_row_begin(ctx, NK_STATIC, 512f + 17, 2)
        nk_layout_row_push(ctx, 200f)
        nk_quick_group(ctx, "$uuid-controls", NK_WINDOW_BORDER).push {
            pushControls(ctx)
        }
        nk_layout_row_push(ctx, 512f + 17)
        nk_quick_group(ctx, "$uuid-grid", NK_WINDOW_NO_SCROLLBAR).padding(0).push {
            pushGrid(ctx, it)
        }
    }

    fun pushControls(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val style = NkStyleButton.mallocStack(stack)
            style.set(ctx.style().button())
            style.padding(NkVec2.mallocStack(stack).set(0f, 0f))

            nk_layout_row_template_begin(ctx, 20f)
            nk_layout_row_template_push_static(ctx, 20f)
            nk_layout_row_template_push_variable(ctx, 50f)
            nk_layout_row_template_push_static(ctx, 20f)
            nk_layout_row_template_end(ctx)
            if(nk_button_symbol_styled(ctx, style, NK_SYMBOL_TRIANGLE_LEFT)) {
                page--
            }
            run {
                val widgetBounds = NkRect.mallocStack(stack)
                nk_widget_bounds(ctx, widgetBounds)
                if(nk_input_is_mouse_hovering_rect(ctx.input(), widgetBounds)) {
                    val scroll = ctx.input().mouse().scroll_delta().y()
                    if(scroll < 0f) {
                        page++
                    } else if(scroll > 0f) {
                        page--
                    }
                }
            }
            if(pageField.push(ctx) and NK_EDIT_COMMITED != 0) {
                nk_edit_unfocus(ctx)
                page = pageField.text.toInt(16)
            }
            if(nk_button_symbol_styled(ctx, style, NK_SYMBOL_TRIANGLE_RIGHT)) {
                page++
            }
        }
    }

    fun pushGrid(ctx: NkContext, groupInfo: NkGroupInfo) {
        MemoryStack.stackPush().use { stack ->
            val widgetRect = NkRect.mallocStack()
            nk_layout_space_begin(ctx, NK_STATIC, 16 * gridSize, Int.MAX_VALUE)
            var newSelected = selected
            for(i in 0 until 256) {
                val x = 1 + (i % 16) * (gridSize + 1)
                val y = 1 + (i / 16) * (gridSize + 1)
                nk_layout_space_push(ctx, widgetRect.set(x, y, gridSize, gridSize))
                references[i].push(ctx)

                val codepoint = page * 256 + i
                val isHovered = nkutil_widget_is_hovered(ctx)
                if(isHovered && Input.isMousePressed(0)) {
                    newSelected = codepoint
                }
                val isSelected = selected == codepoint
                if(isHovered || isSelected) {
                    groupInfo.drawList.strokeRect(
                        x, y, gridSize, gridSize, 0, 1,
                        if(isSelected) Color.ORANGE else Color.GREEN
                    )
                }
            }
            selected = newSelected // do this afterward because the left panel won't update until the next frame

//            for(i in 1 until 16) {
//                drawList.pixelStroke(i * gridSize, 0, i * gridSize, 512, Color.GREEN)
//                drawList.pixelStroke(0, i * gridSize, 512, i * gridSize, Color.GREEN)
//            }
        }
    }

    override fun free() {
        references.forEach { it.free() }
    }
}