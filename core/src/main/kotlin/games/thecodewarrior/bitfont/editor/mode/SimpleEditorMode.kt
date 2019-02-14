package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.Modifiers
import java.util.Collections

open class SimpleEditorMode(editor: Editor): EditorMode(editor) {
    private val actionList = mutableListOf<EditorAction>()
    val actions: List<EditorAction> = Collections.unmodifiableList(actionList)

    fun addAction(key: Key, vararg modifiers: Modifier, action: EditorMode.() -> Unit) {
        actionList.add(EditorAction(key, Modifiers(*modifiers), action))
        actionList.sort()
    }

    override fun receiveText(text: String) {
        contents.append(text)
        updateText()
    }

    override fun keyDown(key: Key) {
        val action = actionList.firstOrNull { it.matches(key, modifiers) }?.action
        if(action != null) {
            this.action()
        }
    }

    override fun keyUp(key: Key) {
    }
}

class EditorAction(val key: Key, val mods: Modifiers, val action: EditorMode.() -> Unit): Comparable<EditorAction> {
    fun matches(key: Key, mods: Modifiers) = key == this.key && this.mods == mods

    override fun compareTo(other: EditorAction): Int {
        return other.mods.count - this.mods.count
    }
}
