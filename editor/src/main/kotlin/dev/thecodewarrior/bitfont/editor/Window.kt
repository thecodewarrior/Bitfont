package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.Freeable
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.util.UUID

abstract class Window(width: Float, height: Float): Freeable {
    val uuid = UUID.randomUUID().toString()
    var title: String = ""
    var flags: Int = NK_WINDOW_BORDER or NK_WINDOW_MOVABLE or NK_WINDOW_MINIMIZABLE or NK_WINDOW_TITLE
    var defaultRect: NkRect = NkRect.create().set(100f, 100f, width, height)

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
    }

    abstract fun pushContents(ctx: NkContext)
}