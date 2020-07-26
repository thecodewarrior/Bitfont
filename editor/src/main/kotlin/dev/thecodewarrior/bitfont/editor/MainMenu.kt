package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkStyle
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack

class MainMenu: Window() {
    var fullWidth = 0
    var fullHeight = 0

    override fun push(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val rect = NkRect.mallocStack(stack)
            rect.set(0, 0, fullWidth, 35)
            if (nk_begin(
                    ctx,
                    uuid,
                    rect,
                    NK_WINDOW_BACKGROUND
                )) {
                nk_window_set_bounds(ctx, uuid, rect)
                pushContents(ctx)
            }
            nk_end(ctx)
        }
    }

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val size = NkVec2.mallocStack(stack)

            nk_menubar_begin(ctx)
            nk_layout_row_static(ctx, 25f, 80, 1)
            if (nk_menu_begin_label(ctx, "File", NK_TEXT_LEFT, size.set(150f, 200f))) {
                nk_layout_row_dynamic(ctx, 25f, 1)
                if(nk_menu_item_label(ctx, "Open", NK_TEXT_LEFT)) {
                    println("Submenu")
                }
                nk_menu_end(ctx)
            }
            nk_menubar_end(ctx)
        }
    }

    override fun free() {
    }
}