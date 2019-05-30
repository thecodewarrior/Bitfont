package games.thecodewarrior.bitfont.editor.mode

import games.thecodewarrior.bitfont.editor.Editor
import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.ModifierPattern
import games.thecodewarrior.bitfont.editor.Modifiers
import games.thecodewarrior.bitfont.editor.MouseButton
import games.thecodewarrior.bitfont.editor.utils.SystemTimeProvider
import games.thecodewarrior.bitfont.editor.utils.TimeProvider
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.Vec2i

@ExperimentalBitfont
open class SimpleEditorMode(editor: Editor): EditorMode(editor) {
    val keyActions: MutableList<KeyEditorAction> = mutableListOf()
    val mouseActions: MutableList<MouseEditorAction> = mutableListOf()
    var mousePos: Vec2i = Vec2i(0, 0)
    var timeProvider: TimeProvider = SystemTimeProvider

    private var currentKeyAction: KeyEditorAction? = null
    private val activeKeyActions = mutableListOf<KeyEditorAction>()
    private var currentMouseAction: MouseEditorAction? = null

    private var lastClickTime: Long = 0
    private var lastClickPos: Vec2i = Vec2i(0, 0)
    private var clickCount: Int = 0
    private var nextKeyRepeatTime: Long? = null

    open var multiClickInterval: Int = 1000/3
    open var multiClickMaxDistance: Double = editor.font.capHeight.toDouble()
    open var keyRepeatDelay: Int = 500
    open var keyRepeatInterval: Int = 100

    @JvmOverloads
    fun keyAction(
        key: Key,
        keyPress: () -> Unit,
        keyDown: () -> Unit = {},
        keyUp: () -> Unit = {}
    ) {
        keyAction(key, ModifierPattern.NONE, keyPress, keyDown, keyUp)
    }

    @JvmOverloads
    fun keyAction(
        key: Key,
        modifier: Modifier?,
        keyPress: () -> Unit,
        keyDown: () -> Unit = {},
        keyUp: () -> Unit = {}
    ) {
        keyAction(key, if(modifier == null) ModifierPattern.NONE else ModifierPattern.require(modifier), keyPress, keyDown, keyUp)
    }

    @JvmOverloads
    fun keyAction(
        key: Key,
        modifiers: ModifierPattern,
        keyPress: () -> Unit,
        keyDown: () -> Unit = {},
        keyUp: () -> Unit = {}
    ) {
        keyActions.add(object: KeyEditorAction(key, modifiers) {
            override fun keyPress() {
                keyPress()
            }

            override fun keyDown() {
                keyDown()
            }

            override fun keyUp() {
                keyUp()
            }
        })
        keyActions.sortByDescending { it.mods.required.size }
    }


    @JvmOverloads
    fun mouseAction(
        button: MouseButton,
        clickCount: Int,
        mouseDown: () -> Unit,
        mouseUp: () -> Unit = {},
        mouseDrag: (previousPos: Vec2i) -> Unit = {}
    ) {
        mouseAction(button, ModifierPattern.NONE, clickCount, mouseDown, mouseUp, mouseDrag)
    }

    @JvmOverloads
    fun mouseAction(
        button: MouseButton,
        modifier: Modifier?,
        clickCount: Int,
        mouseDown: () -> Unit,
        mouseUp: () -> Unit = {},
        mouseDrag: (previousPos: Vec2i) -> Unit = {}
    ) {
        mouseAction(button, if(modifier == null) ModifierPattern.NONE else ModifierPattern.require(modifier), clickCount, mouseDown, mouseUp, mouseDrag)
    }


    @JvmOverloads
    fun mouseAction(
        button: MouseButton,
        modifiers: ModifierPattern,
        clickCount: Int,
        mouseDown: () -> Unit,
        mouseUp: () -> Unit = {},
        mouseDrag: (previousPos: Vec2i) -> Unit = {}
    ) {
        mouseActions.add(object: MouseEditorAction(button, clickCount, modifiers) {
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
        mouseActions.sortByDescending {
            it.clickCount * (Modifier.values().size+1) + it.mods.required.size
        }
    }

    override fun receiveText(text: String) {
        contents.append(text)
        updateText()
    }

    override fun update() {
        currentKeyAction?.also { action ->
            var nextTime = nextKeyRepeatTime ?: return@also

            var i = 0
            while(i < 10 && nextTime < timeProvider.time) {
                action.keyPress()
                nextTime += keyRepeatInterval
                i++
            }
            if(i == 9)
                nextTime = timeProvider.time + keyRepeatInterval

            nextKeyRepeatTime = nextTime
        }
    }

    override fun keyDown(key: Key): Boolean {
        keyActions.lastOrNull { it.matches(key, modifiers) }?.also {
            it.keyDown()
            it.keyPress()
            currentKeyAction = it
            activeKeyActions.add(it)
            nextKeyRepeatTime = timeProvider.time + keyRepeatDelay
            return true
        }
        return false
    }

    override fun keyUp(key: Key) {
        activeKeyActions.removeIf { action ->
            (action.key == key).also {
                if(it) action.keyUp()
            }
        }

        currentKeyAction?.also {
            if(it.key == key) {
                currentKeyAction = null
                nextKeyRepeatTime = null
            }
        }
    }

    override fun mouseDown(button: MouseButton) {
        val time = timeProvider.time
        if(lastClickTime + multiClickInterval < time || (lastClickPos - mousePos).length() > multiClickMaxDistance)
            clickCount = 0
        clickCount++
        lastClickTime = time
        lastClickPos = mousePos
        currentMouseAction?.mouseUp()
        mouseActions.lastOrNull { it.matches(button, clickCount, modifiers) }?.also {
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

abstract class KeyEditorAction(val key: Key, val mods: ModifierPattern) {
    abstract fun keyPress()
    abstract fun keyDown()
    abstract fun keyUp()
    fun matches(key: Key, mods: Modifiers) = key == this.key && this.mods.matches(mods)
}

abstract class MouseEditorAction(val button: MouseButton, val clickCount: Int, val mods: ModifierPattern) {
    abstract fun mouseDown()
    abstract fun mouseUp()
    abstract fun mouseDrag(previousPos: Vec2i)
    fun matches(button: MouseButton, clickCount: Int, mods: Modifiers) = button == this.button && clickCount == this.clickCount && this.mods.matches(mods)
}
