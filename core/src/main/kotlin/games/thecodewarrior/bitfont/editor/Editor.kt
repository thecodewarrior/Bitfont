package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.editor.mode.DefaultEditorMode
import games.thecodewarrior.bitfont.editor.mode.EditorMode
import games.thecodewarrior.bitfont.typesetting.AttributedString
import games.thecodewarrior.bitfont.typesetting.MutableAttributedString
import games.thecodewarrior.bitfont.typesetting.TypesetString
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.getValue
import games.thecodewarrior.bitfont.utils.extensions.setValue

@ExperimentalBitfont
class Editor(font: Bitfont, width: Int) {
    var font = font
        set(value) {
            if(field != value) {
                field = value
                catchOOB { mode.updateText() }
            }
        }
    var width = width
        set(value) {
            if(field != value) {
                field = value
                catchOOB { mode.updateText() }
            }
        }
    /**
     * Validate and potentially modify the value whenever it's changed. This is a stopgap until a better editor is
     * written.
     */
    var validate: (MutableAttributedString) -> MutableAttributedString = { it }

    internal val internals: EditorInternals = EditorInternals()
    inner class EditorInternals {
        var contents: MutableAttributedString = MutableAttributedString("")
        var typesetString: TypesetString = TypesetString(font, AttributedString(""))

        fun updateText() {
            contents = validate(contents)
            typesetString = TypesetString(font, contents, width)
        }
    }

    var attributedString: AttributedString
        get() = internals.contents.staticCopy()
        set(value) {
            internals.contents = value.mutableCopy()
            catchOOB { mode.updateText() }
        }
    var typesetString by internals::typesetString
    var mode: EditorMode = DefaultEditorMode.systemMode(this)

    fun update() {
        catchOOB { mode.update() }
    }

    fun inputModifiers(modifiers: Modifiers) {
        catchOOB { mode.modifiers = modifiers }
    }

    fun inputText(text: String) {
        catchOOB { mode.receiveText(text) }
    }

    fun inputKeyDown(key: Key): Boolean {
        return catchOOB { mode.keyDown(key) } ?: false
    }

    fun inputKeyUp(key: Key) {
        catchOOB { mode.keyUp(key) }
    }

    fun inputMouseMove(pos: Vec2i) {
        catchOOB { mode.mouseMove(pos) }
    }

    fun inputMouseDown(button: MouseButton) {
        catchOOB { mode.mouseDown(button) }
    }

    fun inputMouseUp(button: MouseButton) {
        catchOOB { mode.mouseUp(button) }
    }

    private inline fun <T> catchOOB(fn: () -> T): T? {
        try {
            return fn()
        } catch(e: StringIndexOutOfBoundsException) {
            e.printStackTrace()
            return null
        }
    }
}
