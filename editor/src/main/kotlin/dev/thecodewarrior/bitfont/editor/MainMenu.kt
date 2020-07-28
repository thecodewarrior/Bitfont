package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.stackPush
import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.text.NumberFormat

class MainMenu: Window() {
    var fullWidth = 0
    var fullHeight = 0

    override fun push(ctx: NkContext) {
        stackPush { stack ->
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

    var opened = false
    override fun pushContents(ctx: NkContext) {
        stackPush { stack ->
            val size = NkVec2.mallocStack(stack)

            nk_menubar_begin(ctx)
            nk_layout_row_static(ctx, 25f, 80, 2)
            if (nk_menu_begin_label(ctx, "File", NK_TEXT_LEFT, size.set(150f, 200f))) {
                nk_layout_row_dynamic(ctx, 25f, 1)
                val state = stack.ints(if (opened) NK_MAXIMIZED else NK_MINIMIZED)
                if (nk_menu_begin_label(ctx, "FILE", NK_TEXT_LEFT, size.set(150f, 200f))) {
                    opened = true
                    nk_layout_row_dynamic(ctx, 25f, 1)
                    if (nk_menu_item_label(ctx, "Open", NK_TEXT_LEFT)) {
                        println("Open")
                    }
                    nk_layout_row_dynamic(ctx, 25f, 1)
                    if (nk_menu_item_label(ctx, "Close", NK_TEXT_LEFT)) {
                        println("Close")
                    }
                    nk_menu_end(ctx)
                } else if (opened) { // placeholder for "is this menu open" check, since we only have one right now
                    opened = false
                }
                nk_layout_row_dynamic(ctx, 25f, 1)
                if (nk_menu_item_label(ctx, "Thing", NK_TEXT_LEFT)) {
                    println("Thing")
                }
                nk_layout_row_dynamic(ctx, 25f, 1)
                if (nk_menu_item_label(ctx, "Other", NK_TEXT_LEFT)) {
                    println("Other")
                }
                nk_menu_end(ctx)
            }
            if (nk_menu_item_label(ctx, "Memory Report", NK_TEXT_LEFT)) {
                report()
            }
            nk_menubar_end(ctx)
        }
    }

    private fun report() {
        val threadReport = MemoryReport<String>()
        val stackDepth = 5
        val stackReport = MemoryReport<List<StackTraceElement>>()

        println("")
        println("# Generating memory report")

        MemoryUtil.memReport { address, memory, threadId, threadName, stacktrace ->
            threadReport.add(threadName ?: "ID: $threadId", memory)
            stackReport.add(stacktrace.take(stackDepth), memory)
        }

        println("Threads:")
        threadReport.entries.sortedByDescending { it.size }.forEach {
            println("  `${it.key}`: ${it.summary()}")
        }

        println("Stacks:")
        stackReport.entries.sortedByDescending { it.size }.forEach {
            println("  ${it.summary()}")
            it.key.forEach { element ->
                println("    $element")
            }
        }
    }

    override fun free() {
    }
}

class MemoryReport<T>() {
    data class Entry<T>(val key: T, var count: Int, var size: Long) {
        fun summary(): String {
            val nf = NumberFormat.getIntegerInstance()
            return "${nf.format(count)} buffer${if(count == 1) "" else "s"}, ${nf.format(size)} byte${if(size == 1L) "" else "s"}"
        }
    }

    private val _entries: MutableMap<T, Entry<T>> = mutableMapOf()
    val entries: Collection<Entry<T>> get() = _entries.values

    fun add(key: T, size: Long) {
        val entry = _entries.getOrPut(key) { Entry(key, 0, 0) }
        entry.count++
        entry.size += size
    }
}
