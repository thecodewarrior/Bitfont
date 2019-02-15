package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.mode.DefaultEditorMode
import games.thecodewarrior.bitfont.editor.mode.EditorMode
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.getValue
import games.thecodewarrior.bitfont.utils.extensions.setValue

class Editor(font: Bitfont, width: Int) {
    var font = font
        set(value) {
            if(field != value) {
                field = value
                mode.updateText()
            }
        }
    var width = width
        set(value) {
            if(field != value) {
                field = value
                mode.updateText()
            }
        }

    internal val internals: EditorInternals = EditorInternals()
    inner class EditorInternals {
        var contents: MutableAttributedString = MutableAttributedString("")
        var typesetString: TypesetString = TypesetString(font, AttributedString(""))

        fun updateText() {
            typesetString = TypesetString(font, contents, width)
        }
    }

    val attributedString: AttributedString get() = internals.contents.staticCopy()
    var typesetString by internals::typesetString
    var mode: EditorMode = DefaultEditorMode(this)

    fun inputModifiers(modifiers: Modifiers) {
        mode.modifiers = modifiers
    }

    fun inputText(text: String) {
        mode.receiveText(text)
    }

    fun inputKeyDown(key: Key) {
        mode.keyDown(key)
    }

    fun inputKeyUp(key: Key) {
        mode.keyUp(key)
    }

    fun inputMouseMove(pos: Vec2i) {
        mode.mouseMove(pos)
    }

    fun inputMouseDown(button: MouseButton) {
        mode.mouseDown(button)
    }

    fun inputMouseUp(button: MouseButton) {
        mode.mouseUp(button)
    }
}
