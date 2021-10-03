package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.utils.Freeable
import org.lwjgl.nuklear.NkColor
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
    var crashed: Boolean = false
    private var wasHidden = false

    open fun push(ctx: NkContext) {
        if (nk_begin_titled(
                ctx,
                uuid,
                title,
                defaultRect,
                flags
            )) {
            if (crashed) {
                MemoryStack.stackPush().use { stack ->
                    val contentRegion = NkRect.mallocStack(stack)
                    nk_window_get_content_region(ctx, contentRegion)
                    val canvas = nk_window_get_canvas(ctx)!!
                    val color = NkColor.mallocStack(stack)
                    color.set(255.toByte(), 0, 255.toByte(), 255.toByte())
                    nk_stroke_line(canvas,
                        contentRegion.x(), contentRegion.y(),
                        contentRegion.x() + contentRegion.w(), contentRegion.y() + contentRegion.h(),
                        3f, color
                    )
                    nk_stroke_line(canvas,
                        contentRegion.x() + contentRegion.w(), contentRegion.y(),
                        contentRegion.x(), contentRegion.y() + contentRegion.h(),
                        3f, color
                    )
                }

            } else {
                try {
                    pushContents(ctx)
                } catch(e: Exception) {
                    System.err.println("Error while drawing")
                    e.printStackTrace()
                    crashed = true
                }
            }
        }
        nk_end(ctx)
        val isHidden = nk_window_is_hidden(ctx, uuid)
        if (closeOnHide && isHidden) {
            close(ctx)
        } else if(isHidden && !wasHidden) {
            onHide(ctx)
        }
        wasHidden = isHidden
    }

    abstract fun pushContents(ctx: NkContext)
    open fun onClose(ctx: NkContext) {}
    open fun onHide(ctx: NkContext) {}

    fun open(ctx: NkContext) {
        if (!App.getInstance().windows.contains(this)) {
            App.getInstance().windows.add(this)
        } else {
            nk_window_show(ctx, uuid, NK_SHOWN)
            nk_window_set_focus(ctx, uuid)
        }
    }

    fun close(ctx: NkContext) {
        App.getInstance().windows.remove(this)
        onClose(ctx)
    }

    fun hide(ctx: NkContext) {
        nk_window_show(ctx, uuid, NK_HIDDEN)
    }

    fun show(ctx: NkContext) {
        nk_window_show(ctx, uuid, NK_SHOWN)
    }
}