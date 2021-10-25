package dev.thecodewarrior.bitfont.fonteditor.utils

import dev.thecodewarrior.bitfont.fonteditor.Input
import dev.thecodewarrior.bitfont.fonteditor.Input.IS_MAC
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.Platform
import java.util.Collections
import java.util.LinkedList

@Suppress("FunctionName")
inline fun nk_quick_keys(block: NkQuickKeys.() -> Unit) = NkQuickKeys.block()

fun keystrokeSymbol(keystroke: String): String = keystroke.split("+").joinToString("") {
    KeyRegistry[it.trim()]?.symbol ?: throw IllegalArgumentException("Unknown key `$it`")
}

object NkQuickKeys {
    val mutex = ""
    infix fun Boolean.and(keys: String) = if(this) keys else null
    infix fun Boolean.mutex(keys: String) = if(this) mutex else keys

    inline infix fun String?.pressed(callback: () -> Unit) = this === mutex || this.pressed().also { if(it) callback() }
    inline infix fun String?.down(callback: () -> Unit) = this === mutex || this.down().also { if(it) callback() }
    inline infix fun String?.released(callback: () -> Unit) = this === mutex || this.released().also { if(it) callback() }

    fun String?.pressed(repeat: Boolean = false) = this != null && checkKeys(this) { Input.isKeyPressed(it, repeat) }
    fun String?.repeated() = this != null && checkKeys(this) { Input.isKeyRepeated(it) }
    fun String?.down()     = this != null && checkKeys(this) { Input.isKeyDown(it) }
    fun String?.released() = this != null && checkKeys(this) { Input.isKeyReleased(it) }

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
                KeyItem.ModSuper -> stack.push(Input.isModifierDown(GLFW_MOD_SUPER))
                KeyItem.ModShift -> stack.push(Input.isModifierDown(GLFW_MOD_SHIFT))
                KeyItem.ModCtrl -> stack.push(Input.isModifierDown(GLFW_MOD_CONTROL))
                KeyItem.ModAlt -> stack.push(Input.isModifierDown(GLFW_MOD_ALT))
                is KeyItem.Key -> stack.push(active(item.key))
            }
        }
        return stack.pop()
    }

    private val cache = mutableMapOf<String, List<KeyItem>>()

    private fun parse(expression: String): List<KeyItem> {
        val exp = expression.lowercase()
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
                        output.add(if(IS_MAC) KeyItem.ModSuper else KeyItem.ModCtrl)
                    }
                    else -> {
                        val key = KeyRegistry[token]
                        if(key == null) {
                            throw IllegalArgumentException("Illegal token `$token` (whole expression: `$expression`)")
                        } else {
                            output.add(KeyItem.Key(key.code))
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
        class Key(val key: Int): Input() { override fun toString() = KeyRegistry[key].symbol }
        object ModSuper: Input() { override fun toString() = KeyRegistry.KEY_LEFT_SUPER.symbol }
        object ModShift: Input() { override fun toString() = KeyRegistry.KEY_LEFT_SHIFT.symbol }
        object ModCtrl: Input() { override fun toString() = KeyRegistry.KEY_LEFT_CONTROL.symbol }
        object ModAlt: Input() { override fun toString() = KeyRegistry.KEY_LEFT_ALT.symbol }
    }
}

object KeyRegistry {
    private val _keys = mutableListOf<Key>()
    val keys: List<Key> = Collections.unmodifiableList(_keys)

    val KEY_SPACE = key(GLFW_KEY_SPACE, "", "space")
    val KEY_APOSTROPHE = key(GLFW_KEY_APOSTROPHE, "'", "apostrophe", "'")
    val KEY_COMMA = key(GLFW_KEY_COMMA, ",", "comma", ",")
    val KEY_MINUS = key(GLFW_KEY_MINUS, "-", "minus", "-")
    val KEY_PERIOD = key(GLFW_KEY_PERIOD, ".", "period", ".")
    val KEY_SLASH = key(GLFW_KEY_SLASH, "/", "slash", "/")
    val KEY_0 = key(GLFW_KEY_0, "0", "0")
    val KEY_1 = key(GLFW_KEY_1, "1", "1")
    val KEY_2 = key(GLFW_KEY_2, "2", "2")
    val KEY_3 = key(GLFW_KEY_3, "3", "3")
    val KEY_4 = key(GLFW_KEY_4, "4", "4")
    val KEY_5 = key(GLFW_KEY_5, "5", "5")
    val KEY_6 = key(GLFW_KEY_6, "6", "6")
    val KEY_7 = key(GLFW_KEY_7, "7", "7")
    val KEY_8 = key(GLFW_KEY_8, "8", "8")
    val KEY_9 = key(GLFW_KEY_9, "9", "9")
    val KEY_SEMICOLON = key(GLFW_KEY_SEMICOLON, ";", "semicolon", ";")
    val KEY_EQUAL = key(GLFW_KEY_EQUAL, "=", "equal", "=")
    val KEY_A = key(GLFW_KEY_A, "A", "a")
    val KEY_B = key(GLFW_KEY_B, "B", "b")
    val KEY_C = key(GLFW_KEY_C, "C", "c")
    val KEY_D = key(GLFW_KEY_D, "D", "d")
    val KEY_E = key(GLFW_KEY_E, "E", "e")
    val KEY_F = key(GLFW_KEY_F, "F", "f")
    val KEY_G = key(GLFW_KEY_G, "G", "g")
    val KEY_H = key(GLFW_KEY_H, "H", "h")
    val KEY_I = key(GLFW_KEY_I, "I", "i")
    val KEY_J = key(GLFW_KEY_J, "J", "j")
    val KEY_K = key(GLFW_KEY_K, "K", "k")
    val KEY_L = key(GLFW_KEY_L, "L", "l")
    val KEY_M = key(GLFW_KEY_M, "M", "m")
    val KEY_N = key(GLFW_KEY_N, "N", "n")
    val KEY_O = key(GLFW_KEY_O, "O", "o")
    val KEY_P = key(GLFW_KEY_P, "P", "p")
    val KEY_Q = key(GLFW_KEY_Q, "Q", "q")
    val KEY_R = key(GLFW_KEY_R, "R", "r")
    val KEY_S = key(GLFW_KEY_S, "S", "s")
    val KEY_T = key(GLFW_KEY_T, "T", "t")
    val KEY_U = key(GLFW_KEY_U, "U", "u")
    val KEY_V = key(GLFW_KEY_V, "V", "v")
    val KEY_W = key(GLFW_KEY_W, "W", "w")
    val KEY_X = key(GLFW_KEY_X, "X", "x")
    val KEY_Y = key(GLFW_KEY_Y, "Y", "y")
    val KEY_Z = key(GLFW_KEY_Z, "Z", "z")
    val KEY_LEFT_BRACKET = key(GLFW_KEY_LEFT_BRACKET, "[", "left_bracket", "[")
    val KEY_BACKSLASH = key(GLFW_KEY_BACKSLASH, "\\", "backslash", "\\")
    val KEY_RIGHT_BRACKET = key(GLFW_KEY_RIGHT_BRACKET, "]", "right_bracket", "]")
    val KEY_GRAVE_ACCENT = key(GLFW_KEY_GRAVE_ACCENT, "`", "grave_accent", "`")
    val KEY_WORLD_1 = key(GLFW_KEY_WORLD_1, "world1", "world_1")
    val KEY_WORLD_2 = key(GLFW_KEY_WORLD_2, "world2", "world_2")
    val KEY_ESCAPE = key(GLFW_KEY_ESCAPE, "⎋", "escape")
    val KEY_ENTER = key(GLFW_KEY_ENTER, "⏎", "enter")
    val KEY_TAB = key(GLFW_KEY_TAB, "⇥", "tab")
    val KEY_BACKSPACE = key(GLFW_KEY_BACKSPACE, "⌫", "backspace")
    val KEY_INSERT = key(GLFW_KEY_INSERT, "Insert", "insert")
    val KEY_DELETE = key(GLFW_KEY_DELETE, "⌦", "delete")

    val KEY_RIGHT = key(GLFW_KEY_RIGHT, "→", "right")
    val KEY_LEFT = key(GLFW_KEY_LEFT, "←", "left")
    val KEY_DOWN = key(GLFW_KEY_DOWN, "↓", "down")
    val KEY_UP = key(GLFW_KEY_UP, "↑", "up")

    val KEY_PAGE_UP = key(GLFW_KEY_PAGE_UP, "⇞", "page_up")
    val KEY_PAGE_DOWN = key(GLFW_KEY_PAGE_DOWN, "⇟", "page_down")
    val KEY_HOME = key(GLFW_KEY_HOME, "⇤", "home")
    val KEY_END = key(GLFW_KEY_END, "⇥", "end")

    val KEY_CAPS_LOCK = key(GLFW_KEY_CAPS_LOCK, "⇪", "caps_lock")
    val KEY_SCROLL_LOCK = key(GLFW_KEY_SCROLL_LOCK, "ScrLock", "scroll_lock")
    val KEY_NUM_LOCK = key(GLFW_KEY_NUM_LOCK, "NumLock", "num_lock")
    val KEY_PRINT_SCREEN = key(GLFW_KEY_PRINT_SCREEN, "PrtScn", "print_screen")
    val KEY_PAUSE = key(GLFW_KEY_PAUSE, "⏸︎", "pause")

    val KEY_F1 = key(GLFW_KEY_F1, "F1", "f1")
    val KEY_F2 = key(GLFW_KEY_F2, "F2", "f2")
    val KEY_F3 = key(GLFW_KEY_F3, "F3", "f3")
    val KEY_F4 = key(GLFW_KEY_F4, "F4", "f4")
    val KEY_F5 = key(GLFW_KEY_F5, "F5", "f5")
    val KEY_F6 = key(GLFW_KEY_F6, "F6", "f6")
    val KEY_F7 = key(GLFW_KEY_F7, "F7", "f7")
    val KEY_F8 = key(GLFW_KEY_F8, "F8", "f8")
    val KEY_F9 = key(GLFW_KEY_F9, "F9", "f9")
    val KEY_F10 = key(GLFW_KEY_F10, "F10", "f10")
    val KEY_F11 = key(GLFW_KEY_F11, "F11", "f11")
    val KEY_F12 = key(GLFW_KEY_F12, "F12", "f12")
    val KEY_F13 = key(GLFW_KEY_F13, "F13", "f13")
    val KEY_F14 = key(GLFW_KEY_F14, "F14", "f14")
    val KEY_F15 = key(GLFW_KEY_F15, "F15", "f15")
    val KEY_F16 = key(GLFW_KEY_F16, "F16", "f16")
    val KEY_F17 = key(GLFW_KEY_F17, "F17", "f17")
    val KEY_F18 = key(GLFW_KEY_F18, "F18", "f18")
    val KEY_F19 = key(GLFW_KEY_F19, "F19", "f19")
    val KEY_F20 = key(GLFW_KEY_F20, "F20", "f20")
    val KEY_F21 = key(GLFW_KEY_F21, "F21", "f21")
    val KEY_F22 = key(GLFW_KEY_F22, "F22", "f22")
    val KEY_F23 = key(GLFW_KEY_F23, "F23", "f23")
    val KEY_F24 = key(GLFW_KEY_F24, "F24", "f24")
    val KEY_F25 = key(GLFW_KEY_F25, "F25", "f25")

    val KEY_KP_0 = key(GLFW_KEY_KP_0, "kp0", "kp_0")
    val KEY_KP_1 = key(GLFW_KEY_KP_1, "kp1", "kp_1")
    val KEY_KP_2 = key(GLFW_KEY_KP_2, "kp2", "kp_2")
    val KEY_KP_3 = key(GLFW_KEY_KP_3, "kp3", "kp_3")
    val KEY_KP_4 = key(GLFW_KEY_KP_4, "kp4", "kp_4")
    val KEY_KP_5 = key(GLFW_KEY_KP_5, "kp5", "kp_5")
    val KEY_KP_6 = key(GLFW_KEY_KP_6, "kp6", "kp_6")
    val KEY_KP_7 = key(GLFW_KEY_KP_7, "kp7", "kp_7")
    val KEY_KP_8 = key(GLFW_KEY_KP_8, "kp8", "kp_8")
    val KEY_KP_9 = key(GLFW_KEY_KP_9, "kp9", "kp_9")
    val KEY_KP_DECIMAL = key(GLFW_KEY_KP_DECIMAL, "kp.", "kp_decimal")
    val KEY_KP_DIVIDE = key(GLFW_KEY_KP_DIVIDE, "kp/", "kp_divide")
    val KEY_KP_MULTIPLY = key(GLFW_KEY_KP_MULTIPLY, "kp*", "kp_multiply")
    val KEY_KP_SUBTRACT = key(GLFW_KEY_KP_SUBTRACT, "kp-", "kp_subtract")
    val KEY_KP_ADD = key(GLFW_KEY_KP_ADD, "kp+", "kp_add")
    val KEY_KP_ENTER = key(GLFW_KEY_KP_ENTER, if(IS_MAC) "⌤" else "⎆", "kp_enter")
    val KEY_KP_EQUAL = key(GLFW_KEY_KP_EQUAL, "kp=", "kp_equal")

    val KEY_LEFT_SHIFT = key(GLFW_KEY_LEFT_SHIFT, "⇧",
        "left_shift",
        "shift"
    )
    val KEY_RIGHT_SHIFT = key(GLFW_KEY_RIGHT_SHIFT, "⇧", "right_shift")
    val KEY_LEFT_CONTROL = key(GLFW_KEY_LEFT_CONTROL, "⌃",
        "left_control",
        "control", "ctrl",
        *(if(!IS_MAC) arrayOf("prim") else emptyArray())
    )
    val KEY_RIGHT_CONTROL = key(GLFW_KEY_RIGHT_CONTROL, "⌃", "right_control")
    val KEY_LEFT_ALT = key(GLFW_KEY_LEFT_ALT, if(IS_MAC) "⌥" else "⎇",
        "left_alt",
        "alt",
        "opt", "option",
    )
    val KEY_RIGHT_ALT = key(GLFW_KEY_RIGHT_ALT, if(IS_MAC) "⌥" else "⎇", "right_alt")
    val KEY_LEFT_SUPER = key(GLFW_KEY_LEFT_SUPER, if(IS_MAC) "⌘" else "❖",
        "left_super",
        "sup", "super",
        "cmd", "command",
        "win", "windows",
        *(if(IS_MAC) arrayOf("prim") else emptyArray()),
    )
    val KEY_RIGHT_SUPER = key(GLFW_KEY_RIGHT_SUPER, if(IS_MAC) "⌘" else "❖", "right_super")

    val KEY_MENU = key(GLFW_KEY_MENU, "", "menu")

    private val byName: Map<String, Key> = keys.flatMap { key -> key.names.map { it to key } }.associate { it }
    private val byCode: Map<Int, Key> = keys.associateBy { it.code }

    operator fun get(name: String): Key? {
        return byName[name]
    }

    operator fun get(code: Int): Key {
        return byCode[code] ?: Key(code, "□", listOf("□"))
    }

    private fun key(code: Int, symbol: String, vararg names: String): Key {
        val key = Key(code, symbol, listOf(*names))
        _keys.add(key)
        return key
    }

    data class Key(val code: Int, val symbol: String, val names: List<String>)
}
