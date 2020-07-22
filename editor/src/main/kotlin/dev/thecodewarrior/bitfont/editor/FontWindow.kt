package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.BufferUtils
import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkColorf
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack

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

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_layout_row_static(ctx, 30f, 80, 1)
            if (nk_button_label(ctx, "button")) {
                println("button pressed")
            }
            nk_layout_row_dynamic(ctx, 30f, 2)
            if (nk_option_label(ctx, "easy", op == EASY)) {
                op = EASY
            }
            if (nk_option_label(ctx, "hard", op == HARD)) {
                op = HARD
            }
            nk_layout_row_dynamic(ctx, 25f, 1)
            nk_property_int(ctx, "Compression:", 0, compression, 100, 10, 1f)
            nk_layout_row_dynamic(ctx, 20f, 1)
            nk_label(ctx, "background:", NK_TEXT_LEFT)
            nk_layout_row_dynamic(ctx, 25f, 1)
            if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.mallocStack(stack)), NkVec2.mallocStack(stack).set(nk_widget_width(ctx), 400f))) {
                nk_layout_row_dynamic(ctx, 120f, 1)
                nk_color_picker(ctx, background, NK_RGBA)
                nk_layout_row_dynamic(ctx, 25f, 1)
                background
                    .r(nk_propertyf(ctx, "#R:", 0f, background.r(), 1.0f, 0.01f, 0.005f))
                    .g(nk_propertyf(ctx, "#G:", 0f, background.g(), 1.0f, 0.01f, 0.005f))
                    .b(nk_propertyf(ctx, "#B:", 0f, background.b(), 1.0f, 0.01f, 0.005f))
                    .a(nk_propertyf(ctx, "#A:", 0f, background.a(), 1.0f, 0.01f, 0.005f))
                nk_combo_end(ctx)
            }
            nk_layout_row_dynamic(ctx, 20f, 1)
            nk_label(ctx, "background:", NK_TEXT_LEFT)
            nk_layout_row_dynamic(ctx, 120f, 1)
            nk_edit_string(ctx, NK_EDIT_BOX, text, textLen, 1024, null)
        }
    }

    override fun free() {
    }
}