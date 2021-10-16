package dev.thecodewarrior.bitfont.fonteditor

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.Platform

object Input {
    private val keyState = KeyTracker()
    private val mouseState = KeyTracker()
    private val modifierState = KeyTracker()
    private var _scrollX: Float = 0f
    private var _scrollY: Float = 0f

    fun flush() {
        keyState.flush()
        mouseState.flush()
        modifierState.flush()
        _scrollX = 0f
        _scrollY = 0f
    }

    fun addScroll(x: Float, y: Float) {
        _scrollX += x
        _scrollY += y
    }

    val scrollX: Float get() = _scrollX
    val scrollY: Float get() = _scrollY

    fun setKey(key: Int, state: Boolean, repeated: Boolean) = keyState.setState(key, state, repeated)
    fun setMouse(button: Int, state: Boolean) = mouseState.setState(button, state, false)
    fun setModifier(modifier: Int, state: Boolean) = modifierState.setState(modifier, state, false)

    fun isKeyDown(key: Int) = keyState.isDown(key)
    fun isKeyPressed(key: Int) = isKeyPressed(key, true)
    fun isKeyPressed(key: Int, repeat: Boolean) = keyState.isPressed(key) || (repeat && keyState.isRepeated(key))
    fun isKeyReleased(key: Int) = keyState.isReleased(key)

    fun isModifierDown(modifier: Int) = modifierState.isDown(modifier)

    fun isMouseDown(button: Int) = mouseState.isDown(button)
    fun isMousePressed(button: Int) = mouseState.isPressed(button)
    fun isMouseReleased(button: Int) = mouseState.isReleased(button)

    val IS_MAC = Platform.get() === Platform.MACOSX
    val PRIMARY_MODIFIER = if(IS_MAC) GLFW_MOD_SUPER else GLFW_MOD_CONTROL
}

class KeyTracker {
    private val keysDown = IntOpenHashSet()
    private val pressed = IntOpenHashSet()
    private val repeated = IntOpenHashSet()
    private val released = IntOpenHashSet()

    fun flush() {
        pressed.clear()
        repeated.clear()
        released.clear()
    }

    fun setState(key: Int, state: Boolean, repeated: Boolean) {
        val isDown = key in keysDown
        if (state && !isDown) {
            pressed.add(key)
        } else if (!state && isDown) {
            released.add(key)
        }
        if(repeated)
            this.repeated.add(key)
        if (state) {
            keysDown.add(key)
        } else {
            keysDown.remove(key)
        }
    }

    fun isDown(key: Int): Boolean = key in keysDown
    fun isPressed(key: Int): Boolean = key in pressed
    fun isReleased(key: Int): Boolean = key in released
    fun isRepeated(key: Int): Boolean = key in repeated
}