package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import dev.thecodewarrior.bitfont.fonteditor.widgets.ReferenceGlyphWidget
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color
import kotlin.math.ceil

class GlyphBrowserWindow(val data: BitfontEditorData) : Window(430f, 500f) {
    var gridSize = 50f

    var referencePool = mutableListOf<ReferenceGlyphWidget>()
    var referenceCache = Int2ObjectOpenHashMap<ReferenceGlyphWidget>()
    var references = Int2ObjectOpenHashMap<ReferenceGlyphWidget>()
    val glyphs = IntArrayList()

    init {
        flags = flags or NK_WINDOW_CLOSABLE
    }

    private fun getReferenceWidget(codepoint: Int): ReferenceGlyphWidget {
        val reference = referencePool.removeLastOrNull() ?: ReferenceGlyphWidget()
        reference.guides = ReferenceGlyphWidget.GuideSet(whileBlank = false, baseline = true)
        reference.codepoint = codepoint
        return reference
    }

    override fun pushContents(ctx: NkContext) {
        glyphs.clear()
        glyphs.addAll(data.font.glyphs.keys)
        referenceCache.also { tmp ->
            referenceCache = references
            references = tmp
        }


        MemoryStack.stackPush().use { stack ->
            val columns = (layoutRegion.w() / gridSize).toInt()
            val rows = ceil((layoutRegion.h() / gridSize)).toInt()
            val totalRows = (glyphs.size + columns - 1) / columns
            val scrollY = stack.mallocInt(1)
            nk_window_get_scroll(ctx, null, scrollY)
            val start = (scrollY[0] / gridSize).toInt() * columns
            val widgetRect = NkRect.mallocStack()
            nk_layout_space_begin(ctx, NK_STATIC, totalRows * gridSize, Int.MAX_VALUE)
            for(i in start until start + columns * (rows + 1)) {
                if(i >= glyphs.size)
                    break
                val x = (i % columns) * gridSize
                val y = (i / columns) * gridSize
                nk_layout_space_push(ctx, widgetRect.set(x, y, gridSize, gridSize))
                val codepoint = glyphs.getInt(i)
                val reference = referenceCache.remove(codepoint) ?: getReferenceWidget(codepoint)
                references[codepoint] = reference
                reference.push(ctx)
            }

            referencePool.addAll(referenceCache.values)
            referenceCache.clear()
            while(referencePool.size > 25) {
                referencePool.removeLast().free()
            }
        }
    }

    override fun free() {
        references.values.forEach { it.free() }
        referencePool.forEach { it.free() }
    }
}