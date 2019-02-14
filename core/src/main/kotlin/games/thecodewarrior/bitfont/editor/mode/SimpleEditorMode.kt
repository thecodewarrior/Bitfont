package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.Modifiers
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.utils.Vec2i
import java.util.Collections

open class SimpleEditorMode(editor: Editor): EditorMode(editor) {
    val keyActions: MutableList<KeyEditorAction> = mutableListOf()
    val mouseActions: MutableList<MouseEditorAction> = mutableListOf()
    var mousePos: Vec2i = Vec2i(0, 0)

    private var currentKeyAction: KeyEditorAction? = null
    private var currentMouseAction: MouseEditorAction? = null

    fun addAction(key: Key, vararg modifiers: Modifier,
        keyDown: () -> Unit
    ) {
        addAction(key, modifiers, keyDown, {})
    }

    fun addAction(key: Key, modifiers: Array<out Modifier>,
        keyDown: () -> Unit,
        keyUp: () -> Unit
    ) {
        keyActions.add(object: KeyEditorAction(key, Modifiers(*modifiers)) {
            override fun keyDown() {
                keyDown()
            }

            override fun keyUp() {
                keyUp()
            }
        })
    }

    fun addAction(button: MouseButton, vararg modifiers: Modifier,
        mouseDown: () -> Unit
    ) {
        addAction(button, modifiers, mouseDown, {}, {})
    }

    fun addAction(button: MouseButton, modifiers: Array<out Modifier>,
        mouseDown: () -> Unit,
        mouseUp: () -> Unit,
        mouseDrag: (previousPos: Vec2i) -> Unit
    ) {
        mouseActions.add(object: MouseEditorAction(button, Modifiers(*modifiers)) {
            override fun mouseDown() {
                mouseDown()
            }

            override fun mouseUp() {
                mouseUp()
            }

            override fun mouseDrag(previousPos: Vec2i) {
                mouseDrag(previousPos)
            }
        })
    }

    override fun receiveText(text: String) {
        contents.append(text)
        updateText()
    }

    override fun keyDown(key: Key) {
        keyActions.firstOrNull { it.matches(key, modifiers) }?.also {
            it.keyDown()
            currentKeyAction = it
        }
    }

    override fun keyUp(key: Key) {
        currentKeyAction?.also {
            it.keyUp()
            currentKeyAction = null
        }
    }

    override fun mouseDown(button: MouseButton) {
        mouseActions.firstOrNull { it.matches(button, modifiers) }?.also {
            it.mouseDown()
            currentMouseAction = it
        }
    }

    override fun mouseUp(button: MouseButton) {
        currentMouseAction?.also {
            it.mouseUp()
            currentMouseAction = null
        }
    }

    override fun mouseMove(pos: Vec2i) {
        currentMouseAction?.also {
            val prev = mousePos
            it.mouseDrag(prev)
        }
        mousePos = pos
    }
}

abstract class KeyEditorAction(val key: Key, val mods: Modifiers) {
    abstract fun keyDown()
    abstract fun keyUp()
    fun matches(key: Key, mods: Modifiers) = key == this.key && this.mods == mods
}

abstract class MouseEditorAction(val button: MouseButton, val mods: Modifiers) {
    abstract fun mouseDown()
    abstract fun mouseUp()
    abstract fun mouseDrag(previousPos: Vec2i)
    fun matches(button: MouseButton, mods: Modifiers) = button == this.button && this.mods == mods
}
