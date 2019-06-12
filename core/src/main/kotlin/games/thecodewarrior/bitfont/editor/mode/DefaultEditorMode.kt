package games.thecodewarrior.bitfont.editor.mode

import com.ibm.icu.text.BreakIterator
import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.utils.Clipboard
import games.thecodewarrior.bitfont.editor.utils.InternalClipboard
import games.thecodewarrior.bitfont.typesetting.TypesetString.GlyphRender
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.clamp
import games.thecodewarrior.bitfont.utils.extensions.BreakType
import games.thecodewarrior.bitfont.utils.extensions.endExclusive
import games.thecodewarrior.bitfont.utils.max
import games.thecodewarrior.bitfont.utils.min
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ExperimentalBitfont
open class DefaultEditorMode(editor: Editor): SimpleEditorMode(editor) {
    private var verticalMotionX: Int? = null

    var cursor: CursorPosition = CursorPosition(0, false)
        set(value) {
            if(field != value) {
                field = value
                verticalMotionX = null
                updateCursorPos()
            }
        }
    var selectionStart: CursorPosition? = null
    val selectionRange: CursorRange?
        get() = selectionStart?.let {
            when {
                cursor.index < it.index -> CursorRange(cursor, it)
                cursor.index > it.index -> CursorRange(it, cursor)
                else -> null
            }
        }

    var cursorPos: Vec2i = Vec2i(0, 0)
    var clipboard: Clipboard = InternalClipboard

    init {
        updateCursorPos()
    }

    fun resetCursor(pos: CursorPosition) {
        cursor = pos
        selectionStart = null
    }

    override fun updateText() {
        super.updateText()
        updateCursorPos()
    }

    fun updateCursorPos() {
        val atCursor = internals.typesetString.glyphMap[cursor.index]
        val beforeCursor = internals.typesetString.glyphMap[cursor.index-1]
        if(cursor.afterPrevious) {
            cursorPos = beforeCursor?.posAfter ?:
                atCursor?.pos ?:
                Vec2i(0, editor.font.ascent)
        } else {
            cursorPos = atCursor?.pos ?:
                beforeCursor?.posAfter ?:
                Vec2i(0, editor.font.ascent)
        }
    }

    override fun receiveText(text: String) {
        insert(text)
    }

    fun insert(text: String) {
        val selectionStart = selectionStart
        if(selectionStart != null) {
            if(cursor < selectionStart) {
                contents.delete(cursor.index, selectionStart.index)
                resetCursor(cursor)
            } else if(cursor > selectionStart) {
                contents.delete(selectionStart.index, cursor.index)
                resetCursor(selectionStart)
            }
        }
        contents.insert(cursor.index, text)
        // this is after the previous for a few reasons:
        // - If the user pastes and it causes a word wrap exactly at the end of the pasted value, the cursor should
        //   be at the end of the pasted value
        // - If the user pastes and it ends in a newline, the pos after the newline's end will be correctly on the
        //   next line
        resetCursor(CursorPosition(cursor.index + text.length, true))
        updateText()
    }

    //region actions

    protected fun delete(end: Int) {
        val rangeStart = selectionStart?.index ?: end.clamp(0, contents.length)
        if (cursor.index < rangeStart) {
            contents.delete(cursor.index, rangeStart)
        } else if (cursor.index > rangeStart) {
            contents.delete(rangeStart, cursor.index)
            resetCursor(CursorPosition(rangeStart, false))
        }
        this.selectionStart = null
        updateText()
    }

    protected fun enter() {
        insert("\n")
    }

    protected fun moveTo(pos: CursorPosition) {
        val selectionStart = selectionStart ?: cursor

        resetCursor(pos)

        if(Modifier.SHIFT in modifiers)
            this.selectionStart = selectionStart
    }

    protected fun moveBackward(breakType: BreakType) {
        val iter = breakType.get()
        iter.setText(contents.plaintext)
        val newPos = iter.preceding(selectionStart?.let { min(it, cursor) }?.index ?: cursor.index)
        if(newPos == BreakIterator.DONE)
            moveTo(CursorPosition(0, false))
        else
            moveTo(CursorPosition(newPos, false))
    }

    protected fun moveForward(breakType: BreakType) {
        val iter = breakType.get()
        iter.setText(contents.plaintext)
        val newPos = iter.following(selectionStart?.let { max(it, cursor) }?.index ?: cursor.index)
        if(newPos == BreakIterator.DONE)
            moveTo(CursorPosition(contents.length, true))
        else
            moveTo(CursorPosition(newPos, true))
    }

    protected fun moveUp() {
        var start = selectionStart?.let { min(it, cursor) }?.index ?: cursor.index
        val max = internals.typesetString.glyphMap.keys.max()
        if(max != null && start > max)
            start = max
        internals.typesetString.glyphMap[start]?.also {
            val x = verticalMotionX ?: cursorPos.x
            if(it.line == 0) {
                cursor = CursorPosition(0, false)
            } else {
                val closest = internals.typesetString.lines[it.line-1].closestCharacter(x)
                moveTo(CursorPosition(closest))
            }
            verticalMotionX = x
        }
    }

    protected fun moveDown() {
        val start = selectionStart?.let { max(it, cursor) } ?: cursor
        internals.typesetString.glyphMap[start.index]?.also {
            val x = verticalMotionX ?: cursorPos.x
            if(it.line == internals.typesetString.lines.size-1) {
                cursor = CursorPosition(contents.length, false)
            } else {
                val closest = internals.typesetString.lines[it.line+1].closestCharacter(x)
                moveTo(CursorPosition(closest))
            }
            verticalMotionX = x
        }
    }

    protected fun moveToLineStart() {
        internals.typesetString.glyphMap[cursor.index]?.also {
            // lines[it.line] exists, because this glyph exists
            // glyphs[0] exists, because at least this gliph is on it
            moveTo(CursorPosition(
                internals.typesetString.lines[it.line].glyphs.first().characterIndex, false
            ))
        }
    }

    protected fun moveToLineEnd() {
        internals.typesetString.glyphMap[cursor.index]?.also {
            // lines[it.line] exists, because this glyph exists
            // glyphs[0] exists, because at least this gliph is on it
            moveTo(CursorPosition(
                internals.typesetString.lines[it.line].glyphs.last().characterIndex+1, true
            ))
        }
    }

    protected fun jumpToMouse() {
        val newPos = CursorPosition(internals.typesetString.closestCharacter(mousePos))
        if(Modifier.SHIFT in modifiers) {
            val selection = selectionRange
            if (selection == null) {
                this.selectionStart = cursor
                cursor = newPos
            } else {
                cursor = newPos
                if (abs(selection.start.index - newPos.index) < abs(selection.endExclusive.index - newPos.index)) {
                    selectionStart = selection.endInclusive
                } else {
                    selectionStart = selection.start
                }
            }
            return
        }

        resetCursor(newPos)
    }

    protected fun normalMouseDrag(previousPos: Vec2i) {
        val selectionStart = selectionStart ?: cursor
        cursor = CursorPosition(internals.typesetString.closestCharacter(mousePos))
        this.selectionStart = selectionStart
    }

    protected fun paste() {
        clipboard.contents?.also {
            if(it.isNotEmpty()) insert(it)
        }
    }

    protected fun copy() {
        selectionRange?.also {
            clipboard.contents = contents.plaintext.substring(it.start.index .. it.endExclusive.index)
            selectionStart = null
        }
    }

    protected fun cut() {
        selectionRange?.also {
            copy()
            contents.delete(it.start.index, it.endExclusive.index)
            resetCursor(it.start)
            updateText()
        }
    }

    //endregion

    companion object {
        var operatingSystemMode: (editor: Editor) -> DefaultEditorMode = ::WindowsEditorMode

        fun systemMode(editor: Editor): DefaultEditorMode {
            return operatingSystemMode(editor)
        }
    }
}

data class CursorPosition(val index: Int, val afterPrevious: Boolean): Comparable<CursorPosition> {
    constructor(pair: Pair<Int, Boolean>) : this(pair.first, pair.second)

    override fun compareTo(other: CursorPosition): Int {
        if(this.index == other.index) {
            if(this.afterPrevious && !other.afterPrevious)
                return -1
            if(!this.afterPrevious && other.afterPrevious)
                return 1
        }
        return this.index.compareTo(other.index)
    }

    operator fun plus(other: Int): CursorPosition {
        return CursorPosition(index + other, afterPrevious)
    }

    operator fun minus(other: Int): CursorPosition {
        return CursorPosition(index - other, afterPrevious)
    }
}

data class CursorRange(val start: CursorPosition, val endExclusive: CursorPosition) {
    val endInclusive: CursorPosition = endExclusive.copy(index = endExclusive.index - 1)
    val indexRange = start.index until endExclusive.index
}
