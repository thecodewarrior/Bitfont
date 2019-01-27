package games.thecodewarrior.bitfont.utils

import kotlin.math.min

class HistoryTracker<T>(val maxDepth: Int, initialValue: T) {

    var undoDepth = 0
        private set
    var redoDepth = 0
        private set

    private var historyIndex = 0
    private val history: MutableList<T?> = MutableList(100) { null }

    init {
        history[0] = initialValue
    }

    fun canUndo(): Boolean = undoDepth != 0
    fun canRedo(): Boolean = redoDepth != 0
    fun willOverwrite(): Boolean = undoDepth == maxDepth-1
    fun peek(): T = history[historyIndex % history.size]!!

    fun push(state: T) {
        historyIndex++
        undoDepth = min(maxDepth-1, undoDepth + 1)
        redoDepth = 0
        history[historyIndex % history.size] = state
    }

    fun undo(): T {
        if (canUndo()) {
            undoDepth--
            historyIndex--
            redoDepth++
        }
        return history[historyIndex % history.size]!!
    }

    fun redo(): T {
        if (canRedo()) {
            undoDepth++
            historyIndex++
            redoDepth--
        }
        return history[historyIndex % history.size]!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HistoryTracker<*>) return false

        if (maxDepth != other.maxDepth) return false
        if (undoDepth != other.undoDepth) return false
        if (redoDepth != other.redoDepth) return false
        if (historyIndex != other.historyIndex) return false
        if (history != other.history) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maxDepth
        result = 31 * result + undoDepth
        result = 31 * result + redoDepth
        result = 31 * result + historyIndex
        result = 31 * result + history.hashCode()
        return result
    }
}