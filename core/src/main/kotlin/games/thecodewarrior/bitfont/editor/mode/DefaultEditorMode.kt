package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key

class DefaultEditorMode(editor: Editor): SimpleEditorMode(editor) {
    var cursor = 0

    override fun recieveText(text: String) {
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
        addAction(Key.ENTER) {
            insert("\n")
        }
    }
}
