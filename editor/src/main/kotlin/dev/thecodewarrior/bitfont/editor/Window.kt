package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.util.UUID

abstract class Window {
    private var uuid = UUID.randomUUID().toString()
    var title: String = ""

    fun push(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val rect = NkRect.mallocStack(stack)
            if (nk_begin_titled(
                    ctx,
                    uuid,
                    title,
                    rect.set(0, 0, 230f, 250f),
                    NK_WINDOW_BORDER or NK_WINDOW_MOVABLE or NK_WINDOW_SCALABLE or NK_WINDOW_MINIMIZABLE or NK_WINDOW_TITLE
                )) {
                pushContents(ctx)
            }
            nk_end(ctx)
        }
    }

    abstract fun pushContents(ctx: NkContext)
    abstract fun free()
}