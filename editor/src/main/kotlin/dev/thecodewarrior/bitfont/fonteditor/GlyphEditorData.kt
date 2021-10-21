package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.data.BitGrid
import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.fonteditor.data.BitfontEditorData
import dev.thecodewarrior.bitfont.fonteditor.utils.HistoryTracker
import dev.thecodewarrior.bitfont.fonteditor.utils.Vec2i
import kotlin.math.max
import kotlin.math.min

class GlyphEditorData(val data: BitfontEditorData) {
    val clipboard = mutableSetOf<Vec2i>()
    private val glyphDataMap = mutableMapOf<Int, GlyphData>()

    fun retain(codepoint: Int): GlyphData {
        val glyph = glyphDataMap.getOrPut(codepoint) { GlyphData(codepoint) }
        glyph.retains++
        return glyph
    }

    fun release(glyph: GlyphData) {
        glyph.retains--
        if(glyph.retains == 0 && glyph == GlyphData(glyph.codepoint)) {
            glyphDataMap.remove(glyph.codepoint)
        }
    }

    inner class GlyphData(val codepoint: Int) {
        var retains = 0
        val glyph: Glyph = data.font.glyphs.getOrPut(codepoint) { Glyph(data.font) }
        val enabledCells = mutableSetOf<Vec2i>()

        val history: HistoryTracker<State>

        var advance: Int
            get() = glyph.advance
            set(value) { glyph.advance = value }

        init {
            updateFromGlyph()
            history = HistoryTracker(100, State())
        }

        fun pushHistory() {
            history.push(State())
            updateGlyph()
        }

        fun undo() {
            history.undo().apply()
            updateGlyph()
        }

        fun redo() {
            history.redo().apply()
            updateGlyph()
        }

        inner class State {
            val cells = enabledCells.toSet()

            fun apply() {
                enabledCells.clear()
                enabledCells.addAll(cells)
            }
        }

        fun updateGlyph() {
            if(enabledCells.isEmpty())  {
                glyph.image = BitGrid(1, 1)
                glyph.bearingX = 0
                glyph.bearingY = 0
                return
            }
            var minX = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var minY = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE
            enabledCells.forEach {
                minX = min(minX, it.x)
                maxX = max(maxX, it.x)
                minY = min(minY, it.y)
                maxY = max(maxY, it.y)
            }
            val grid = BitGrid(maxX - minX + 1, maxY - minY + 1)
            enabledCells.forEach {
                grid[it.x - minX, it.y - minY] = true
            }
            glyph.image = grid
            glyph.bearingX = minX
            glyph.bearingY = minY
        }

        fun updateFromGlyph() {
            enabledCells.clear()
            val grid = glyph.image
            for(x in 0 until grid.width) {
                for(y in 0 until grid.height) {
                    if(grid[x, y]) {
                        enabledCells.add(Vec2i(x + glyph.bearingX, y + glyph.bearingY))
                    }
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GlyphData) return false

            if (codepoint != other.codepoint) return false
            if (glyph != other.glyph) return false
            if (enabledCells != other.enabledCells) return false
            if (history != other.history) return false

            return true
        }

        override fun hashCode(): Int {
            var result = codepoint
            result = 31 * result + glyph.hashCode()
            result = 31 * result + enabledCells.hashCode()
            result = 31 * result + history.hashCode()
            return result
        }
    }
}