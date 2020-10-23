package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.Freeable
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.util.UUID

abstract class Window(width: Float, height: Float): Freeable {
    var uuid = UUID.randomUUID().toString()
        private set
    var title: String = ""
    var flags: Int = NK_WINDOW_BORDER or NK_WINDOW_MOVABLE or NK_WINDOW_MINIMIZABLE or NK_WINDOW_TITLE
    var defaultRect: NkRect = NkRect.create().set(100f, 100f, width, height)
    var closeOnHide: Boolean = false

    open fun push(ctx: NkContext) {
        if (nk_begin_titled(
                ctx,
                uuid,
                title,
                defaultRect,
                flags
            )) {
            pushContents(ctx)
        }
        nk_end(ctx)
        if(closeOnHide && nk_window_is_hidden(ctx, uuid)) {
            close(ctx)
        }
    }

    abstract fun pushContents(ctx: NkContext)
    open fun onClose(ctx: NkContext) {}

    fun open(ctx: NkContext) {
        if(!BitfontEditorApp.getInstance().windows.contains(this)) {
            BitfontEditorApp.getInstance().windows.add(this)
        } else {
            nk_window_show(ctx, uuid, NK_SHOWN)
            nk_window_set_focus(ctx, uuid)
        }
    }

    fun close(ctx: NkContext) {
        BitfontEditorApp.getInstance().windows.remove(this)
        onClose(ctx)
    }

    fun hide(ctx: NkContext) {
        nk_window_show(ctx, uuid, NK_HIDDEN)
    }

    fun show(ctx: NkContext) {
        nk_window_show(ctx, uuid, NK_SHOWN)
    }
}