package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.ModifierPattern
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.utils.extensions.BreakType

class WindowsEditorMode(editor: Editor): DefaultEditorMode(editor) {

    init {
        val optionalShift = ModifierPattern.optional(Modifier.SHIFT)

        keyAction(Key.BACKSPACE, ::backspace)
        keyAction(Key.DELETE, ::delete)
        keyAction(Key.ENTER, ::enter)

        keyAction(Key.LEFT, optionalShift, { moveBackward(BreakType.CHARACTER) })
        keyAction(Key.RIGHT, optionalShift, { moveForward(BreakType.CHARACTER) })

        keyAction(Key.UP, optionalShift, ::moveUp)
        keyAction(Key.DOWN, optionalShift, ::moveDown)

        mouseAction(MouseButton.LEFT, optionalShift, 1, ::jumpToMouse, {}, ::normalMouseDrag)
//        mouseAction(MouseButton.LEFT, optionalShift, 2, ::leftMouseDoubleDown, {}, ::leftMouseDrag)

        keyAction(Key.V, Modifier.SUPER, ::paste)
        keyAction(Key.C, Modifier.SUPER, ::copy)
        keyAction(Key.X, Modifier.SUPER, ::cut)
    }

}
