package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.typesetting.MutableAttributedString
import dev.thecodewarrior.bitfont.typesetting.TextContainer
import dev.thecodewarrior.bitfont.typesetting.TextLayoutDelegate
import dev.thecodewarrior.bitfont.typesetting.TextLayoutManager
import kotlin.math.abs
import kotlin.math.min

public class TextEditor(
    font: Bitfont,
    containers: List<TextContainer>,
    private val delegate: TextEditorDelegate?
): TextLayoutDelegate {
    public val layoutManager: TextLayoutManager = TextLayoutManager(font)
    public val text: MutableAttributedString = MutableAttributedString("")

    private val cursors = mutableListOf<Cursor>()

    init {
        layoutManager.delegate = this
        layoutManager.textContainers.addAll(containers)
        layoutManager.attributedString = text
    }

    override fun textDidLayout() {
        for(cursor in cursors) {
            cursor.updateFromIndex()
        }
    }

    public fun createCursor(): Cursor {
        return Cursor(this)
    }

    /**
     * Finds the closest line in [container] to the given [y] coordinate.
     */
    public fun queryLine(container: TextContainer, y: Int): TextContainer.TypesetLine? {
        return container.lines.minByOrNull { min(abs(it.posY - y), abs(it.posY + it.height - y)) }
    }

    /**
     * Finds the closest column in [line] to the given [x] coordinate. [container] is used to assemble the final
     * [CursorPosition] instance.
     */
    public fun queryColumn(container: TextContainer, line: TextContainer.TypesetLine, x: Int): CursorPosition? {
        val column = (0..line.clusters.size).minByOrNull { abs(line.positionAt(it)!! - x) } ?: return null
        return CursorPosition(
            line.indexAt(column)!!, container,
            line.lineIndex, column
        )
    }

    public class Cursor(private val editor: TextEditor) {
        private var _position: CursorPosition? = null
        private var _indexDirty = false

        /**
         * The position of the cursor within the text. If [index] or the underlying attributed string change, this will
         * recompute its container/line/column when next accessed. When set, this will overwrite [index].
         */
        public var position: CursorPosition?
            get() {
                if(editor.layoutManager.isStringDirty()) {
                    // The layout is out of sync with the string contents. re-layout the text to fix this.
                    // This also triggers a recalculation of the position of every cursor, so we don't need to call
                    // updateFromIndex()
                    editor.layoutManager.layoutText()
                } else if(_indexDirty) {
                    updateFromIndex()
                }
                return _position
            }
            set(value) {
                if(value != null) {
                    index = value.index
                    _indexDirty = false
                }
                _position = value
            }

        /**
         * The index within the input string. Modifying this value will cause a recalculation of [position] when next
         * accessed.
         */
        public var index: Int = 0
            set(value) {
                if(field != value)
                    _indexDirty = true
                field = value
            }

        // this marker is only queried when the text is laid out
        private val mark = object : MutableAttributedString.Marker {
            override var position: Int
                get() = index
                set(value) { index = value }
        }
        init {
            editor.text.registerMarker(mark)
            editor.cursors.add(this)
        }

        /**
         * Updates [position] based on the current [index].
         */
        @JvmSynthetic
        internal fun updateFromIndex() {
            this._indexDirty = false
            if(isIndexValid()) return
            this._position = findOnSameLine() ?: findLineShift() ?: findAnyPosition()
        }

        /**
         * The list of containers in the text editor with the current position's container at the front
         */
        private val searchContainers: Sequence<TextContainer>
            get() = listOfNotNull(_position?.container).asSequence() + editor.layoutManager.textContainers.asSequence()
                .filter { it != _position?.container }

        /**
         * Checks if the current [index] matches the [position]
         */
        private fun isIndexValid(): Boolean {
            val position = this._position ?: return false
            val line = position.container.lines.getOrNull(position.line) ?: return false
            return line.indexAt(position.column) == index
        }

        /**
         * If the cursor's [index] can be found on its current line, this updates [position] to that new column. This is
         * to preserve cursor positioning when text in a line shifts around.
         *
         * Part of the best-effort cursor preservation system.
         */
        private fun findOnSameLine(): CursorPosition? {
            val position = this._position ?: return null
            val line = position.container.lines.getOrNull(position.line) ?: return null
            val newColumn = line.columnOf(index) ?: return null
            return CursorPosition(index, position.container, position.line, newColumn)
        }

        /**
         * If the cursor's [index] can be found on another line at its current column, this updates [position] to that
         * new line. This is to preserve cursor positioning when line contents move around.
         *
         * Part of the best-effort cursor preservation system.
         */
        private fun findLineShift(): CursorPosition? {
            val position = this._position ?: return null
            for(container in searchContainers) {
                for(line in container.lines) {
                    if(line.indexAt(position.column) == index) {
                        return CursorPosition(index, container, line.lineIndex, position.column)
                    }
                }
            }
            return null
        }

        /**
         * Finds any position for the current index. Returns false if the attempt failed
         */
        private fun findAnyPosition(): CursorPosition? {
            for(container in searchContainers) {
                for(line in container.lines) {
                    line.columnOf(index)?.also { column ->
                        return CursorPosition(index, container, line.lineIndex, column)
                    }
                }
            }
            return null
        }
    }

    public data class CursorPosition(
        /**
         * The index within the input string
         */
        val index: Int,
        /**
         * The container the cursor lies in
         */
        val container: TextContainer,
        /**
         * The line the cursor is on
         */
        val line: Int,
        /**
         * The column the cursor is on. This is measured as the start position of the Nth grapheme cluster, or the end
         * position of the last one, if the column is past the end
         */
        val column: Int
    ) {
        /**
         * The X position within the container
         */
        public val x: Int
            get() = container.lines.getOrNull(line)?.positionAt(column) ?: 0

        /**
         * The baseline Y position within the container
         */
        public val y: Int
            get() = container.lines.getOrNull(line)?.baselineY ?: 0

        /**
         * The length of the cursor above the baseline
         */
        public val ascent: Int
            get() = container.lines.getOrNull(line)?.ascent ?: 0

        /**
         * The length of the cursor below the baseline
         */
        public val descent: Int
            get() = container.lines.getOrNull(line)?.descent ?: 0
    }
}

public interface TextEditorDelegate {

}