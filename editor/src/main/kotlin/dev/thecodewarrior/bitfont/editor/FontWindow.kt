package dev.thecodewarrior.bitfont.editor

import org.lwjgl.BufferUtils
import org.lwjgl.nuklear.NkColorf
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.Nuklear.NK_EDIT_FIELD
import org.lwjgl.nuklear.Nuklear.NK_TEXT_LEFT
import org.lwjgl.nuklear.Nuklear.nk_button_label
import org.lwjgl.nuklear.Nuklear.nk_edit_string
import org.lwjgl.nuklear.Nuklear.nk_label
import org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic
import org.lwjgl.nuklear.Nuklear.nk_property_int
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.text.ParseException

class FontWindow: Window() {
    private val EASY = 0
    private val HARD = 1

    var background = NkColorf.create()
        .r(0.10f)
        .g(0.18f)
        .b(0.24f)
        .a(1.0f)

    var text = BufferUtils.createByteBuffer(1024)
    var textLen = BufferUtils.createIntBuffer(1)

    private var op = EASY

    private val compression = BufferUtils.createIntBuffer(1).put(0, 20)

    var fontName = ""

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_dynamic(ctx, 20f, 1)
            nk_label(ctx, "Font Name:", NK_TEXT_LEFT)
            nk_layout_row_dynamic(ctx, 25f, 1)
            run {
                val buffer = stack.calloc(256)
                val length = MemoryUtil.memUTF8(fontName, false, buffer)
                val len = stack.ints(length)
                nk_edit_string(ctx, NK_EDIT_FIELD, buffer, len, 255, null)
                try {
                    fontName = MemoryUtil.memUTF8(buffer, len[0])
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                title = fontName
            }

            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Ascent:", 0, compression, 100, 1, 1f)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Cap Height:", 0, compression, 100, 1, 1f)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "X Height:", 0, compression, 100, 1, 1f)
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Descent:", 0, compression, 100, 1, 1f)

            nk_layout_row_dynamic(ctx, 30f, 2)
            if (nk_button_label(ctx, "Edit")) {
                println("edit glyphs")
            }
            if (nk_button_label(ctx, "Browse")) {
                println("browse glyphs")
            }
            if (nk_button_label(ctx, "Typesetter")) {
                println("typesetter")
            }
            if (nk_button_label(ctx, "Text Layout")) {
                println("text layout")
            }
        }
    }

    override fun free() {
    }
}