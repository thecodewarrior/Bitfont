package games.thecodewarrior.bitfont.editor.mode

import com.ibm.icu.text.BreakIterator
import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.ModifierPattern
import games.thecodewarrior.bitfont.editor.Modifiers
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.editor.utils.Clipboard
import games.thecodewarrior.bitfont.editor.utils.InternalClipboard
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.typesetting.TypesetString.GlyphRender
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.BreakType
import games.thecodewarrior.bitfont.utils.extensions.endExclusive
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class DefaultEditorMode(editor: Editor): SimpleEditorMode(editor) {
    private var verticalMotionX: Int? = null

    var cursor = 0
        set(value) {
            if(field != value) {
                field = value
                verticalMotionX = null
                selectionStart = null
                updateCursorPos()
            }
        }
    var selectionStart: Int? = null
    val selectionRange: IntRange?
        get() = selectionStart?.let {
            if(cursor < it)
                cursor until it
            else if(cursor > it)
                it until cursor
            else
                null
        }

    var cursorGlyph: GlyphRender? = null
    var cursorPos: Vec2i = Vec2i(0, 0)
    var clipboard: Clipboard = InternalClipboard

    init {
        updateCursorPos()
    }

    override fun updateText() {
        super.updateText()
        updateCursorPos()
    }

    fun updateCursorPos() {
        val atCursor = internals.typesetString.glyphMap[cursor]
        val beforeCursor = internals.typesetString.glyphMap[cursor-1]
        cursorGlyph = atCursor ?: beforeCursor
        cursorPos = atCursor?.pos ?:
            beforeCursor?.posAfter ?:
            Vec2i(0, editor.font.ascent)
    }

    override fun receiveText(text: String) {
        insert(text)
    }

    fun insert(text: String) {
        val selectionStart = selectionStart
        if(selectionStart != null) {
            if(cursor < selectionStart) {
                contents.delete(cursor, selectionStart)
            } else if(cursor > selectionStart) {
                contents.delete(selectionStart, cursor)
                cursor = selectionStart
            }
            this.selectionStart = null
        }
        contents.insert(cursor, text)
        cursor += text.length
        updateText()
    }

    //region actions

    protected fun backspace() {
        val selectionStart = selectionStart
        if (selectionStart != null) {
            if (cursor < selectionStart) {
                contents.delete(cursor, selectionStart)
            } else if (cursor > selectionStart) {
                contents.delete(selectionStart, cursor)
                cursor = selectionStart
            }
            this.selectionStart = null
            updateText()
        } else if (cursor > 0) {
            contents.delete(cursor - 1, cursor)
            cursor--
            updateText()
        }
    }

    protected fun delete() {
        if (cursor < contents.length) {
            contents.delete(cursor, cursor + 1)
            updateText()
        }
    }

    protected fun enter() {
        insert("\n")

    }

    protected fun moveTo(pos: Int) {
        val selectionStart = selectionStart ?: cursor

        cursor = pos

        if(Modifier.SHIFT in modifiers)
            this.selectionStart = selectionStart
    }

    protected fun moveBackward(breakType: BreakType) {
        val iter = breakType.get()
        iter.setText(contents.plaintext)
        val newPos = iter.preceding(cursor)
        if(newPos == BreakIterator.DONE)
            moveTo(0)
        else
            moveTo(newPos)
    }

    protected fun moveForward(breakType: BreakType) {
        val iter = breakType.get()
        iter.setText(contents.plaintext)
        val newPos = iter.following(cursor)
        if(newPos == BreakIterator.DONE)
            moveTo(contents.length)
        else
            moveTo(newPos)
    }

    protected fun moveUp() {
        cursorGlyph?.also {
            if(it.line == 0) {
                val x = verticalMotionX ?: cursorPos.x
                cursor = 0
                verticalMotionX = x
            } else {
                val nextLine =
                    if(cursor == contents.length && verticalMotionX != null && verticalMotionX != cursorPos.x)
                        it.line
                    else
                        it.line-1
                val x = verticalMotionX ?: cursorPos.x
                moveTo(internals.typesetString.lines[nextLine].closestCharacter(x))
                verticalMotionX = x
            }
        }
    }

    protected fun moveDown() {
        cursorGlyph?.also {
            if(it.line == internals.typesetString.lines.size-1) {
                val x = verticalMotionX ?: cursorPos.x
                cursor = contents.length
                verticalMotionX = x
            } else {
                val nextLine =
                    if(cursor == 0 && verticalMotionX != null && verticalMotionX != cursorPos.x)
                        it.line
                    else
                        it.line+1
                val x = verticalMotionX ?: cursorPos.x
                moveTo(internals.typesetString.lines[nextLine].closestCharacter(x))
                verticalMotionX = x
            }
        }
    }

    protected fun jumpToMouse() {
        val newPos = internals.typesetString.closestCharacter(mousePos)
        if(Modifier.SHIFT in modifiers) {
            val selection = selectionRange
            if (selection == null) {
                val currentCursor = cursor
                cursor = newPos
                this.selectionStart = currentCursor
            } else {
                cursor = newPos
                if (abs(selection.start - newPos) < abs(selection.endExclusive - newPos)) {
                    selectionStart = selection.endInclusive
                } else {
                    selectionStart = selection.start
                }
            }
            return
        }

        cursor = newPos
    }

    protected fun normalMouseDrag(previousPos: Vec2i) {
        val selectionStart = selectionStart ?: cursor
        cursor = internals.typesetString.closestCharacter(mousePos)
        this.selectionStart = selectionStart
    }

    protected fun paste() {
        clipboard.contents?.also {
            if(it.isNotEmpty()) insert(it)
        }
    }

    protected fun copy() {
        selectionRange?.also {
            clipboard.contents = contents.plaintext.substring(it)
            selectionStart = null
        }
    }

    protected fun cut() {
        selectionRange?.also {
            copy()
            contents.delete(it.start, it.endExclusive)
            cursor = it.start
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
