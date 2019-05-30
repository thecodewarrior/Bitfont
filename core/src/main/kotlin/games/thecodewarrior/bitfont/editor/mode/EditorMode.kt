package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifiers
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.Vec2i
import games.thecodewarrior.bitfont.utils.extensions.getValue
import games.thecodewarrior.bitfont.utils.extensions.setValue

@ExperimentalBitfont
abstract class EditorMode(val editor: Editor) {
    open var modifiers: Modifiers = Modifiers()

    val internals = editor.internals
    var contents by internals::contents
    open fun updateText() = internals.updateText()

    abstract fun update()

    abstract fun receiveText(text: String)
    abstract fun keyDown(key: Key): Boolean
    abstract fun keyUp(key: Key)

    abstract fun mouseDown(button: MouseButton)
    abstract fun mouseUp(button: MouseButton)
    abstract fun mouseMove(pos: Vec2i)
}
