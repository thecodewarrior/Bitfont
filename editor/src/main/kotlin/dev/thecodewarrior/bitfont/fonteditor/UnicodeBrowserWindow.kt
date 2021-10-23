package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.NkMagicConstants
import dev.thecodewarrior.bitfont.fonteditor.widgets.ReferenceGlyphWidget
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import kotlin.math.ceil

class UnicodeBrowserWindow(val data: BitfontEditorData) : Window(512f, 512f, false) {
    val gridSize = 32f
    val references = List(256) { createReferenceWidget() }

    var page = -1
        set(value) {
            if(field != value) {
                field = value
                references.forEachIndexed { i, reference -> reference.codepoint = value * 256 + i }
            }
        }

    init {
        flags = flags or NK_WINDOW_CLOSABLE
        page = 0
    }

    private fun createReferenceWidget(): ReferenceGlyphWidget {
        val reference = ReferenceGlyphWidget()
        reference.guides = ReferenceGlyphWidget.GuideSet(whileBlank = false, baseline = true)
        return reference
    }

    override fun pushContents(ctx: NkContext) {
        MemoryStack.stackPush().use { stack ->
            val widgetRect = NkRect.mallocStack()
            nk_layout_space_begin(ctx, NK_STATIC, 16 * gridSize, Int.MAX_VALUE)
            for(i in 0 until 256) {
                val x = (i % 16) * gridSize
                val y = (i / 16) * gridSize
                nk_layout_space_push(ctx, widgetRect.set(x, y, gridSize, gridSize))
                references[i].push(ctx)
            }
        }
    }

    override fun free() {
        references.forEach { it.free() }
    }
}