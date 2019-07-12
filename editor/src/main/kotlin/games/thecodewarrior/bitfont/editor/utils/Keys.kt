package games.thecodewarrior.bitfont.editor.utils

import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.utils.extensions.splitWithDelimiters
import org.ice1000.jimgui.JImDefaultKeys
import org.ice1000.jimgui.JImGui
import java.util.LinkedList
import java.util.Locale

inline fun ImGui.keys(callback: ImGuiKeys.() -> Unit) = ImGuiKeys.callback()

object ImGuiKeys {
    val mutex = ""
    infix fun Boolean.and(keys: String) = if(this) keys else null
    infix fun Boolean.mutex(keys: String) = if(this) mutex else keys

    inline infix fun String?.pressed(callback: () -> Unit) = this === mutex || this.pressed().also { if(it) callback() }
    inline infix fun String?.down(callback: () -> Unit) = this === mutex || this.down().also { if(it) callback() }
    inline infix fun String?.released(callback: () -> Unit) = this === mutex || this.released().also { if(it) callback() }

    fun String?.pressed()  = this != null && checkKeys(this) { ImGui.isKeyPressed(it, false)  }
    fun String?.down()     = this != null && checkKeys(this) { ImGui.isKeyDown(it)     }
    fun String?.released() = this != null && checkKeys(this) { ImGui.isKeyReleased(it) }

    fun checkKeys(expression: String, active: (Int) -> Boolean): Boolean {
        val rpn = parse(expression)
        val stack = LinkedList<Boolean>()

        rpn.forEach { item ->
            when(item) {
                KeyItem.AndOp -> {
                    val a = stack.pop()
                    val b = stack.pop()
                    stack.push(a && b)
                }
                KeyItem.OrOp -> {
                    val a = stack.pop()
                    val b = stack.pop()
                    stack.push(a || b)
                }
                KeyItem.ModSuper -> stack.push(ImGui.IO.keySuper)
                KeyItem.ModShift -> stack.push(ImGui.IO.keySuper)
                KeyItem.ModCtrl -> stack.push(ImGui.IO.keySuper)
                KeyItem.ModAlt -> stack.push(ImGui.IO.keySuper)
                is KeyItem.Key -> stack.push(active(item.key))
            }
        }
        return stack.pop()
    }

    private val cache = mutableMapOf<String, List<KeyItem>>()

    private fun parse(expression: String): List<KeyItem> {
        val exp = expression.toLowerCase(Locale.ROOT)
        return cache.getOrPut(exp) {
            val tokens = exp.replace("\\s+".toRegex(), "").splitWithDelimiters("\\+|\\(|\\)|\\|").filter { it != "" }
            val output = mutableListOf<KeyItem>()
            val ops = LinkedList<KeyItem.Op>()

            tokens.forEach { token ->
                when(token) {
                    "+" -> ops.push(KeyItem.AndOp)
                    "|" -> ops.push(KeyItem.OrOp)
                    "(" -> ops.push(KeyItem.ParenOp)
                    ")" -> {
                        pop@ while(ops.isNotEmpty()) {
                            val op = ops.pop()
                            if(op == KeyItem.ParenOp) break@pop
                            output.add(op)
                        }
                    }
                    "cmd", "command", "sup", "super", "win", "windows" -> {
                        output.add(KeyItem.ModSuper)
                    }
                    "opt", "option", "alt" -> {
                        output.add(KeyItem.ModAlt)
                    }
                    "shift" -> {
                        output.add(KeyItem.ModShift)
                    }
                    "ctrl", "control" -> {
                        output.add(KeyItem.ModCtrl)
                    }
                    "prim" -> {
                        output.add(ifMac(KeyItem.ModSuper, KeyItem.ModCtrl))
                    }
                    "sec" -> {
                        output.add(ifMac(KeyItem.ModCtrl, KeyItem.ModSuper))
                    }
                    else -> {
                        val keyCode = JImDefaultKeys.A //keysByName[token]
                        if(keyCode == null) {
                            throw IllegalArgumentException("Illegal token `$token` (whole expression: `$expression`)")
                        } else {
                            output.add(KeyItem.Key(keyCode))
                        }
                    }
                }
            }
            while(ops.isNotEmpty()) { output.add(ops.pop()) }
            output
        }
    }

    private sealed class KeyItem {
        abstract class Op: KeyItem()
        object AndOp: Op() { override fun toString() = "+" }
        object OrOp: Op() { override fun toString() = "|" }
        object ParenOp: Op() { override fun toString() = "(" }

        abstract class Input: KeyItem()
        class Key(val key: Int): Input() { override fun toString() = null /*namesByKey[key]*/ ?: "?" }
        object ModSuper: Input() { override fun toString() = "Super" }
        object ModShift: Input() { override fun toString() = "Shift" }
        object ModCtrl: Input() { override fun toString() = "Ctrl" }
        object ModAlt: Input() { override fun toString() = "Alt" }
    }
}

//private val keysByName = mutableMapOf(
//    "space" to GLFW.GLFW_KEY_SPACE,
//    "apostrophe" to GLFW.GLFW_KEY_APOSTROPHE,
//    "comma" to GLFW.GLFW_KEY_COMMA,
//    "minus" to GLFW.GLFW_KEY_MINUS,
//    "period" to GLFW.GLFW_KEY_PERIOD,
//    "slash" to GLFW.GLFW_KEY_SLASH,
//    "'" to GLFW.GLFW_KEY_APOSTROPHE,
//    "," to GLFW.GLFW_KEY_COMMA,
//    "-" to GLFW.GLFW_KEY_MINUS,
//    "." to GLFW.GLFW_KEY_PERIOD,
//    "/" to GLFW.GLFW_KEY_SLASH,
//    "0" to GLFW.GLFW_KEY_0,
//    "1" to GLFW.GLFW_KEY_1,
//    "2" to GLFW.GLFW_KEY_2,
//    "3" to GLFW.GLFW_KEY_3,
//    "4" to GLFW.GLFW_KEY_4,
//    "5" to GLFW.GLFW_KEY_5,
//    "6" to GLFW.GLFW_KEY_6,
//    "7" to GLFW.GLFW_KEY_7,
//    "8" to GLFW.GLFW_KEY_8,
//    "9" to GLFW.GLFW_KEY_9,
//    "semicolon" to GLFW.GLFW_KEY_SEMICOLON,
//    "equal" to GLFW.GLFW_KEY_EQUAL,
//    ";" to GLFW.GLFW_KEY_SEMICOLON,
//    "=" to GLFW.GLFW_KEY_EQUAL,
//    "a" to GLFW.GLFW_KEY_A,
//    "b" to GLFW.GLFW_KEY_B,
//    "c" to GLFW.GLFW_KEY_C,
//    "d" to GLFW.GLFW_KEY_D,
//    "e" to GLFW.GLFW_KEY_E,
//    "f" to GLFW.GLFW_KEY_F,
//    "g" to GLFW.GLFW_KEY_G,
//    "h" to GLFW.GLFW_KEY_H,
//    "i" to GLFW.GLFW_KEY_I,
//    "j" to GLFW.GLFW_KEY_J,
//    "k" to GLFW.GLFW_KEY_K,
//    "l" to GLFW.GLFW_KEY_L,
//    "m" to GLFW.GLFW_KEY_M,
//    "n" to GLFW.GLFW_KEY_N,
//    "o" to GLFW.GLFW_KEY_O,
//    "p" to GLFW.GLFW_KEY_P,
//    "q" to GLFW.GLFW_KEY_Q,
//    "r" to GLFW.GLFW_KEY_R,
//    "s" to GLFW.GLFW_KEY_S,
//    "t" to GLFW.GLFW_KEY_T,
//    "u" to GLFW.GLFW_KEY_U,
//    "v" to GLFW.GLFW_KEY_V,
//    "w" to GLFW.GLFW_KEY_W,
//    "x" to GLFW.GLFW_KEY_X,
//    "y" to GLFW.GLFW_KEY_Y,
//    "z" to GLFW.GLFW_KEY_Z,
//    "left_bracket" to GLFW.GLFW_KEY_LEFT_BRACKET,
//    "backslash" to GLFW.GLFW_KEY_BACKSLASH,
//    "right_bracket" to GLFW.GLFW_KEY_RIGHT_BRACKET,
//    "grave_accent" to GLFW.GLFW_KEY_GRAVE_ACCENT,
//    "[" to GLFW.GLFW_KEY_LEFT_BRACKET,
//    "\\" to GLFW.GLFW_KEY_BACKSLASH,
//    "]" to GLFW.GLFW_KEY_RIGHT_BRACKET,
//    "`" to GLFW.GLFW_KEY_GRAVE_ACCENT,
//    "world_1" to GLFW.GLFW_KEY_WORLD_1,
//    "world_2" to GLFW.GLFW_KEY_WORLD_2,
//    "escape" to GLFW.GLFW_KEY_ESCAPE,
//    "enter" to GLFW.GLFW_KEY_ENTER,
//    "tab" to GLFW.GLFW_KEY_TAB,
//    "backspace" to GLFW.GLFW_KEY_BACKSPACE,
//    "insert" to GLFW.GLFW_KEY_INSERT,
//    "delete" to GLFW.GLFW_KEY_DELETE,
//    "right" to GLFW.GLFW_KEY_RIGHT,
//    "left" to GLFW.GLFW_KEY_LEFT,
//    "down" to GLFW.GLFW_KEY_DOWN,
//    "up" to GLFW.GLFW_KEY_UP,
//    "page_up" to GLFW.GLFW_KEY_PAGE_UP,
//    "page_down" to GLFW.GLFW_KEY_PAGE_DOWN,
//    "home" to GLFW.GLFW_KEY_HOME,
//    "end" to GLFW.GLFW_KEY_END,
//    "caps_lock" to GLFW.GLFW_KEY_CAPS_LOCK,
//    "scroll_lock" to GLFW.GLFW_KEY_SCROLL_LOCK,
//    "num_lock" to GLFW.GLFW_KEY_NUM_LOCK,
//    "print_screen" to GLFW.GLFW_KEY_PRINT_SCREEN,
//    "pause" to GLFW.GLFW_KEY_PAUSE,
//    "f1" to GLFW.GLFW_KEY_F1,
//    "f2" to GLFW.GLFW_KEY_F2,
//    "f3" to GLFW.GLFW_KEY_F3,
//    "f4" to GLFW.GLFW_KEY_F4,
//    "f5" to GLFW.GLFW_KEY_F5,
//    "f6" to GLFW.GLFW_KEY_F6,
//    "f7" to GLFW.GLFW_KEY_F7,
//    "f8" to GLFW.GLFW_KEY_F8,
//    "f9" to GLFW.GLFW_KEY_F9,
//    "f10" to GLFW.GLFW_KEY_F10,
//    "f11" to GLFW.GLFW_KEY_F11,
//    "f12" to GLFW.GLFW_KEY_F12,
//    "f13" to GLFW.GLFW_KEY_F13,
//    "f14" to GLFW.GLFW_KEY_F14,
//    "f15" to GLFW.GLFW_KEY_F15,
//    "f16" to GLFW.GLFW_KEY_F16,
//    "f17" to GLFW.GLFW_KEY_F17,
//    "f18" to GLFW.GLFW_KEY_F18,
//    "f19" to GLFW.GLFW_KEY_F19,
//    "f20" to GLFW.GLFW_KEY_F20,
//    "f21" to GLFW.GLFW_KEY_F21,
//    "f22" to GLFW.GLFW_KEY_F22,
//    "f23" to GLFW.GLFW_KEY_F23,
//    "f24" to GLFW.GLFW_KEY_F24,
//    "f25" to GLFW.GLFW_KEY_F25,
//    "kp_0" to GLFW.GLFW_KEY_KP_0,
//    "kp_1" to GLFW.GLFW_KEY_KP_1,
//    "kp_2" to GLFW.GLFW_KEY_KP_2,
//    "kp_3" to GLFW.GLFW_KEY_KP_3,
//    "kp_4" to GLFW.GLFW_KEY_KP_4,
//    "kp_5" to GLFW.GLFW_KEY_KP_5,
//    "kp_6" to GLFW.GLFW_KEY_KP_6,
//    "kp_7" to GLFW.GLFW_KEY_KP_7,
//    "kp_8" to GLFW.GLFW_KEY_KP_8,
//    "kp_9" to GLFW.GLFW_KEY_KP_9,
//    "kp_decimal" to GLFW.GLFW_KEY_KP_DECIMAL,
//    "kp_divide" to GLFW.GLFW_KEY_KP_DIVIDE,
//    "kp_multiply" to GLFW.GLFW_KEY_KP_MULTIPLY,
//    "kp_subtract" to GLFW.GLFW_KEY_KP_SUBTRACT,
//    "kp_add" to GLFW.GLFW_KEY_KP_ADD,
//    "kp_enter" to GLFW.GLFW_KEY_KP_ENTER,
//    "kp_equal" to GLFW.GLFW_KEY_KP_EQUAL,
//    "left_shift" to GLFW.GLFW_KEY_LEFT_SHIFT,
//    "left_control" to GLFW.GLFW_KEY_LEFT_CONTROL,
//    "left_alt" to GLFW.GLFW_KEY_LEFT_ALT,
//    "left_super" to GLFW.GLFW_KEY_LEFT_SUPER,
//    "right_shift" to GLFW.GLFW_KEY_RIGHT_SHIFT,
//    "right_control" to GLFW.GLFW_KEY_RIGHT_CONTROL,
//    "right_alt" to GLFW.GLFW_KEY_RIGHT_ALT,
//    "right_super" to GLFW.GLFW_KEY_RIGHT_SUPER,
//    "menu" to GLFW.GLFW_KEY_MENU,
//    "last" to GLFW.GLFW_KEY_LAST
//)
//
//private val namesByKey = keysByName.entries.associate { it.value to it.key }
