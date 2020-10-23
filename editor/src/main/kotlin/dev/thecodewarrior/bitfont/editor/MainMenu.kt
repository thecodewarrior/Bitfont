package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.editor.utils.nkutil_max_text_width
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu_bar
import dev.thecodewarrior.bitfont.editor.utils.nkutil_menu_item
import dev.thecodewarrior.bitfont.editor.utils.nkutil_text_width
import dev.thecodewarrior.bitfont.editor.utils.stackPush
import dev.thecodewarrior.bitfont.editor.utils.set
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import org.lwjgl.util.nfd.NativeFileDialog.NFD_CANCEL
import org.lwjgl.util.nfd.NativeFileDialog.NFD_OKAY
import java.nio.file.Files
import java.nio.file.Paths
import java.text.NumberFormat

class MainMenu: Window(0f, 0f) {
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
                    stackPush { stack ->
                        val pathOut = stack.mallocPointer(1)
                        val result = NativeFileDialog.NFD_OpenDialog("bitfont", null, pathOut)
                        when(result) {
                            NFD_OKAY -> {
                                val path = pathOut.getStringUTF8(0)
                                NativeFileDialog.nNFD_Free(pathOut.get(0))
                                println("Selected: $path")
                                val data = BitfontEditorData.open(Files.newInputStream(Paths.get(path)))
                                BitfontEditorApp.getInstance().windows.add(FontWindow(data))
                            }
                            NFD_CANCEL -> {
                                println("Canceled")
                            }
                            else -> {}
                        }
                    }
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
