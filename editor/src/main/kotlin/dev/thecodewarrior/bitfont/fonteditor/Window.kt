package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.utils.Freeable
import dev.thecodewarrior.bitfont.fonteditor.utils.NkMagicConstants
import dev.thecodewarrior.bitfont.fonteditor.utils.set
import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkHandle
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.util.UUID

abstract class Window(width: Float, height: Float, scroll: Boolean = true): Freeable {
    var uuid = UUID.randomUUID().toString()
        private set
    var title: String = ""
    var flags: Int
    var defaultRect: NkRect = NkRect.create()
    var closeOnHide: Boolean = false
    var crashed: Boolean = false
    private var wasHidden = false

    init {
        flags = NK_WINDOW_BORDER or NK_WINDOW_MOVABLE or NK_WINDOW_MINIMIZABLE or NK_WINDOW_TITLE
        if(!scroll) flags = flags or NK_WINDOW_NO_SCROLLBAR

        val padding = if(scroll) NkMagicConstants.scrollingWindow else NkMagicConstants.window
        defaultRect.set(100f, 100f, width + padding.layoutPadding.horizontal, height + padding.layoutPadding.vertical)
    }

    val isWindowFocused: Boolean get() = windowDepth == 0

    val windowId: Int = System.identityHashCode(this) // used for window ordering
    val windowDepth: Int
        get() {
            val index = App.getInstance().windowIds.indexOf(windowId)
            if(index == -1)
                return Int.MAX_VALUE
            else
                return App.getInstance().windowIds.size - 1 - index
        }

    val contentRegion = NkRect.create()
    val layoutRegion = NkRect.create()

    open fun push(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            nk_set_user_data(ctx, NkHandle.mallocStack(stack).ptr(App.WINDOW_ID_SENTINEL or windowId.toULong().toLong()))
            if (nk_begin_titled(
                    ctx,
                    uuid,
                    title,
                    defaultRect,
                    flags
                )) {
                nk_set_user_data(ctx, NkHandle.mallocStack(stack).ptr(0))
                nk_window_get_content_region(ctx, contentRegion)
                val padding = ctx.style().window().padding()
                layoutRegion.set(
                    contentRegion.x() + padding.x(),
                    contentRegion.y() + padding.y(),
                    contentRegion.w() - 2 * padding.x(),
                    contentRegion.h() - padding.y()
                )
                if (crashed) {
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
            ctx.input().mouse().scroll_delta().set(Input.scrollX, Input.scrollY)
            nk_end(ctx)
            val isHidden = nk_window_is_hidden(ctx, uuid)
            if (closeOnHide && isHidden) {
                close(ctx)
            } else if(isHidden && !wasHidden) {
                onHide(ctx)
            }
            wasHidden = isHidden
        }
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