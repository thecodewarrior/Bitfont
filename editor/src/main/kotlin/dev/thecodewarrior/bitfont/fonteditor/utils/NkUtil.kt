@file:Suppress("FunctionName")

package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memAddress
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max

object NkMagicConstants {
    val window = WindowMetrics(
        Padding(top = 36f, left = 6f, bottom = 2f, right = 6f),
        Padding(top = 40f, left = 6f, bottom = 6f, right = 6f),
    )
    val scrollingWindow = WindowMetrics(
        window.contentPadding.add(bottom = 10f, right = 10f),
        window.layoutPadding.add(bottom = 10f, right = 10f),
    )

    data class WindowMetrics(val contentPadding: Padding, val layoutPadding: Padding)

    data class Padding(val top: Float, val left: Float, val bottom: Float, val right: Float) {
        val horizontal = left + right
        val vertical = top + bottom

        fun add(top: Float = 0f, left: Float = 0f, bottom: Float = 0f, right: Float = 0f): Padding {
            return Padding(this.top + top, this.left + left, this.bottom + bottom, this.right + right)
        }
    }
}

/**
 * Creates a menubar
 */
@OptIn(ExperimentalContracts::class)
inline fun nkutil_menu_bar(ctx: NkContext, item_count: Int, crossinline draw: () -> Unit) {
    contract {
        callsInPlace(draw, InvocationKind.EXACTLY_ONCE)
    }

    nk_menubar_begin(ctx)
    nk_layout_row_begin(ctx, NK_STATIC, 25f, item_count)
    draw()
    nk_layout_row_end(ctx)
    nk_menubar_end(ctx)
}



/**
 * Creates an expandable menu. Designed for use inside [nkutil_menu_bar]
 */
@OptIn(ExperimentalContracts::class)
inline fun nkutil_menu(ctx: NkContext, text: String, item_width: Float, content_width: Float, item_count: Int, crossinline draw: () -> Unit) {
    contract {
        callsInPlace(draw, InvocationKind.AT_MOST_ONCE)
    }
    val buttonPadding = ctx.style().menu_button().padding()
    nk_layout_row_push(ctx, item_width + buttonPadding.x() * 2)
    val menuPadding = ctx.style().window().menu_padding()
    val windowPadding = ctx.style().window().padding()

    // we can't push the stack because of the callback. the callback may try to stick stuff in the previous frame,
    // either crashing or overwriting our data
    val size = NkVec2.malloc()
    size.set(
        content_width + menuPadding.x() * 2 + windowPadding.x() * 2,
        (25f + buttonPadding.y() * 2) * item_count + menuPadding.y() * 2
    )
    try {
        if (nk_menu_begin_label(ctx, text, NK_TEXT_LEFT, size)) {
            draw()
            nk_menu_end(ctx)
        }
    } finally {
        size.free()
    }
}

/**
 * Creates a clickable item. Designed for use inside [nkutil_menu]
 */
@OptIn(ExperimentalContracts::class)
inline fun nkutil_menu_item(ctx: NkContext, text: String, action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    nk_layout_row_dynamic(ctx, 25f, 1)
    if (nk_menu_item_label(ctx, text, NK_TEXT_LEFT)) {
        action()
    }
}


/**
 * Gets the width of the passed text using the current font
 */
fun nkutil_text_width(ctx: NkContext, text: String): Float {
    stackPush { stack ->
        val font = ctx.style().font() ?: return 0f
        val textBuffer = stack.UTF8(text)
        return font.width()?.invoke(
            font.userdata().address(), font.height(),
            memAddress(textBuffer), textBuffer.remaining()
        ) ?: 0f
    }
}

/**
 * Gets the width of the widest passed string using the current font
 */
fun nkutil_max_text_width(ctx: NkContext, vararg strings: String): Float {
    var maxWidth = 0f
    val font = ctx.style().font() ?: return 0f
    val widthCallback = font.width() ?: return 0f
    strings.forEach { text ->
        stackPush { stack ->
            val textBuffer = stack.UTF8(text)
            maxWidth = max(maxWidth, widthCallback.invoke(
                font.userdata().address(), font.height(),
                memAddress(textBuffer), textBuffer.remaining()
            ))
        }
    }
    return maxWidth
}

fun nkutil_widget_is_hovered(ctx: NkContext): Boolean {
    stackPush { stack ->
        val hoverBounds = NkRect.mallocStack(stack)
        nk_widget_bounds(ctx, hoverBounds)
        return nk_input_is_mouse_hovering_rect(ctx.input(), hoverBounds)
    }
}

// note to self: don't use nk_layout_row with groups. Always use nk_layout_row_begin/nk_layout_row_push.
// For some reason using nk_layout_row breaks things horribly and stuff starts flickering all over the place
fun nk_quick_group(ctx: NkContext, title: String, vararg flags: Int): NkGroupBuilder {
    return NkGroupBuilder(ctx, title, flags.fold(0) { acc, flag -> acc or flag })
}

class NkGroupBuilder(val ctx: NkContext, val title: String, val flags: Int) {
    var name: String? = null
    var paddingX: Float? = null
    var paddingY: Float? = null

    fun name(name: String) = build { this.name = name }
    fun padding(pad: Number) = this.padding(pad, pad)
    fun padding(padX: Number, padY: Number) = build {
        paddingX = padX.toFloat()
        paddingY = padY.toFloat()
    }

    inline fun push(block: (NkGroupInfo) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        // we can't push the stack because of the callback. the callback may try to stick stuff in the previous frame,
        // either crashing or overwriting our data
        val contentRegion = NkRect.malloc()
        val layoutRegion = NkRect.malloc()
        val oldPadding = NkVec2.malloc()
        val groupPadding = ctx.style().window().group_padding()
        try {
            oldPadding.set(groupPadding)
            groupPadding.set(
                paddingX ?: groupPadding.x(),
                paddingY ?: groupPadding.y()
            )

            val name = name
            if(
                (name == null && nk_group_begin(ctx, title, flags)) ||
                (name != null && nk_group_begin_titled(ctx, name, title, flags))
            ) {
                nk_window_get_content_region(ctx, contentRegion)
                layoutRegion.set(
                    contentRegion.x() + groupPadding.x(),
                    contentRegion.y() + groupPadding.y(),
                    contentRegion.w() - 2 * groupPadding.x(),
                    contentRegion.h() - groupPadding.y()
                )
                val drawList = DrawList()
                drawList.transformX = contentRegion.x()
                drawList.transformY = contentRegion.y()
                block(NkGroupInfo(contentRegion, layoutRegion, drawList))
                drawList.push(ctx)
                nk_group_end(ctx)
            } else {
                println("group canceled")
            }
        } finally {
            groupPadding.set(oldPadding)
            contentRegion.free()
            layoutRegion.free()
            oldPadding.free()
        }
    }

    private fun build(block: () -> Unit): NkGroupBuilder = this.also { block() }
}

data class NkGroupInfo(val contentRegion: NkRect, val layoutRegion: NkRect, val drawList: DrawList)
