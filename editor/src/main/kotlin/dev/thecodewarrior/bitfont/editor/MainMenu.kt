package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.nkutil_max_text_width
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu_bar
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu_bar_item
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu_item
import dev.thecodewarrior.bitfont.editor.utils.nkutil_text_width
import dev.thecodewarrior.bitfont.editor.utils.stackPush
import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.NkVec2
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryUtil
import java.text.NumberFormat

class MainMenu: Window() {
    var fullWidth = 0
    var fullHeight = 0

    override fun push(ctx: NkContext) {
        stackPush { stack ->
            val rect = NkRect.mallocStack(stack)
            rect.set(0, 0, fullWidth, 25 + ctx.style().window().menu_padding().y()*2)
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
        nkutil_menu_bar(ctx, 2) {
            nkutil_menu(ctx, "File", nkutil_text_width(ctx, "File"), nkutil_max_text_width(ctx, "Open", "Save"), 2) {
                nkutil_menu_item(ctx, "Open") {
                    println("Open")
                }
                nkutil_menu_item(ctx, "Save") {
                    println("Save")
                }
            }
            nkutil_menu(ctx, "Tools", nkutil_text_width(ctx, "Tools"), nkutil_max_text_width(ctx, "Memory Report"), 1) {
                nkutil_menu_item(ctx, "Memory Report") {
                    printMemoryReport()
                }
            }
        }
    }

    private fun printMemoryReport() {
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
