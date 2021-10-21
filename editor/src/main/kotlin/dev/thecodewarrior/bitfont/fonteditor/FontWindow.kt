package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD
import org.lwjgl.nuklear.Nuklear.NK_TEXT_LEFT
import org.lwjgl.nuklear.Nuklear.NK_WINDOW_CLOSABLE
import org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR
import org.lwjgl.nuklear.Nuklear.nk_button_label
import org.lwjgl.nuklear.Nuklear.nk_edit_string
import org.lwjgl.nuklear.Nuklear.nk_label
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_property_int
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.text.ParseException

class FontWindow(val data: BitfontEditorData): Window(250f, 325f) {
    private val typesetterWindow = TypesetterWindow(data)
    private val textLayoutWindow = TextLayoutWindow(data)
    private val editorData = GlyphEditorData(data)
    private val editorWindows = mutableListOf<FontEditorWindow>()

    init {
        flags = flags or NK_WINDOW_NO_SCROLLBAR or NK_WINDOW_CLOSABLE
        closeOnHide = true
    }

    override fun pushContents(ctx: NkContext) {
        editorWindows.removeIf {
            if(it.closed) {
                it.free()
                true
            } else {
                false
            }
        }
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 20f, 1)
            nk_label(ctx, "Font Name:", NK_TEXT_LEFT)
            nk_layout_row_dynamic(ctx, 25f, 1)
            run {
                val buffer = stack.calloc(256)
                val length = MemoryUtil.memUTF8(data.font.name, false, buffer)
                val len = stack.ints(length)
                nk_edit_string(ctx, NK_EDIT_FIELD, buffer, len, 255, null)
                try {
                    data.font.name = MemoryUtil.memUTF8(buffer, len[0])
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                title = data.font.name
            }

            val metric = stack.ints(0)

            metric.put(0, data.font.ascent)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Ascent:", 0, metric, 100, 1, 1f)
            data.font.ascent = metric[0]

            metric.put(0, data.font.capHeight)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Cap Height:", 0, metric, 100, 1, 1f)
            data.font.capHeight = metric[0]

            metric.put(0, data.font.xHeight)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "X Height:", 0, metric, 100, 1, 1f)
            data.font.xHeight = metric[0]

            metric.put(0, data.font.descent)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Descent:", 0, metric, 100, 1, 1f)
            data.font.descent = metric[0]

            nk_layout_row_dynamic(ctx, 30f, 2)
            if (nk_button_label(ctx, "Edit")) {
                val editorWindow = FontEditorWindow(data, editorData)
                editorWindows.add(editorWindow)
                editorWindow.open(ctx)
            }
            if (nk_button_label(ctx, "Browse")) {
                println("browse glyphs")
            }
            if (nk_button_label(ctx, "Typesetter")) {
                typesetterWindow.open(ctx)
            }
            if (nk_button_label(ctx, "Text Layout")) {
                textLayoutWindow.open(ctx)
            }
        }
    }

    override fun onClose(ctx: NkContext) {
        typesetterWindow.close(ctx)
        textLayoutWindow.close(ctx)
        editorWindows.forEach { it.close(ctx) }
    }

    override fun free() {
        typesetterWindow.free()
        textLayoutWindow.free()
        editorWindows.forEach { it.free() }
    }
}