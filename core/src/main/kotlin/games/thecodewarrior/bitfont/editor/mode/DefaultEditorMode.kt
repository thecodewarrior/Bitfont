package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.typesetting.TypesetString.GlyphRender
import games.thecodewarrior.bitfont.utils.Vec2i
import kotlin.math.abs
import kotlin.math.min

class DefaultEditorMode(editor: Editor): SimpleEditorMode(editor) {
    private var verticalMotionX: Int? = null

    var cursor = 0
        set(value) {
            if(field != value) {
                field = value
                verticalMotionX = null
                updateCursorPos()
            }
        }
    var cursorGlyph: GlyphRender? = null
    var cursorPos: Vec2i = Vec2i(0, 0)

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
        contents.insert(cursor, text)
        cursor += text.length
        updateText()
    }

    init {
        addAction(Key.BACKSPACE) {
            if(cursor > 0) {
                contents.delete(cursor-1, cursor)
                cursor--
                updateText()
            }
        }
        addAction(Key.DELETE) {
            if(cursor < contents.length) {
                contents.delete(cursor, cursor+1)
                updateText()
            }
        }

        addAction(Key.LEFT) {
            if(cursor > 0)
                cursor--
        }
        addAction(Key.RIGHT) {
            if(cursor < contents.length)
                cursor++
        }
        addAction(Key.UP) {
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
                    cursor = internals.typesetString.lines[nextLine].closestCharacter(x)
                    verticalMotionX = x
                }
            }
        }
        addAction(Key.DOWN) {
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
                    cursor = internals.typesetString.lines[nextLine].closestCharacter(x)
                    verticalMotionX = x
                }
            }
        }

        addAction(Key.ENTER) {
            insert("\n")
        }

        addAction(MouseButton.LEFT) {
            cursor = internals.typesetString.closestCharacter(mousePos)
        }
    }


}
