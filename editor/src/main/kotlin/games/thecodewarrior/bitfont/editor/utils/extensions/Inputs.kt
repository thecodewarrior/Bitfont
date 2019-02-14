package games.thecodewarrior.bitfont.editor.utils.extensions

import games.thecodewarrior.bitfont.editor.Key
import games.thecodewarrior.bitfont.editor.Modifier
import games.thecodewarrior.bitfont.editor.Modifiers
import games.thecodewarrior.bitfont.editor.MouseButton
import org.lwjgl.glfw.GLFW

fun Key.Companion.fromGlfw(glfw: Int) = when(glfw) {
    GLFW.GLFW_KEY_UNKNOWN -> Key.UNKNOWN
    GLFW.GLFW_KEY_SPACE -> Key.SPACE
    GLFW.GLFW_KEY_APOSTROPHE -> Key.APOSTROPHE
    GLFW.GLFW_KEY_COMMA -> Key.COMMA
    GLFW.GLFW_KEY_MINUS -> Key.MINUS
    GLFW.GLFW_KEY_PERIOD -> Key.PERIOD
    GLFW.GLFW_KEY_SLASH -> Key.SLASH
    GLFW.GLFW_KEY_0 -> Key.NUM_0
    GLFW.GLFW_KEY_1 -> Key.NUM_1
    GLFW.GLFW_KEY_2 -> Key.NUM_2
    GLFW.GLFW_KEY_3 -> Key.NUM_3
    GLFW.GLFW_KEY_4 -> Key.NUM_4
    GLFW.GLFW_KEY_5 -> Key.NUM_5
    GLFW.GLFW_KEY_6 -> Key.NUM_6
    GLFW.GLFW_KEY_7 -> Key.NUM_7
    GLFW.GLFW_KEY_8 -> Key.NUM_8
    GLFW.GLFW_KEY_9 -> Key.NUM_9
    GLFW.GLFW_KEY_SEMICOLON -> Key.SEMICOLON
    GLFW.GLFW_KEY_EQUAL -> Key.EQUAL
    GLFW.GLFW_KEY_A -> Key.A
    GLFW.GLFW_KEY_B -> Key.B
    GLFW.GLFW_KEY_C -> Key.C
    GLFW.GLFW_KEY_D -> Key.D
    GLFW.GLFW_KEY_E -> Key.E
    GLFW.GLFW_KEY_F -> Key.F
    GLFW.GLFW_KEY_G -> Key.G
    GLFW.GLFW_KEY_H -> Key.H
    GLFW.GLFW_KEY_I -> Key.I
    GLFW.GLFW_KEY_J -> Key.J
    GLFW.GLFW_KEY_K -> Key.K
    GLFW.GLFW_KEY_L -> Key.L
    GLFW.GLFW_KEY_M -> Key.M
    GLFW.GLFW_KEY_N -> Key.N
    GLFW.GLFW_KEY_O -> Key.O
    GLFW.GLFW_KEY_P -> Key.P
    GLFW.GLFW_KEY_Q -> Key.Q
    GLFW.GLFW_KEY_R -> Key.R
    GLFW.GLFW_KEY_S -> Key.S
    GLFW.GLFW_KEY_T -> Key.T
    GLFW.GLFW_KEY_U -> Key.U
    GLFW.GLFW_KEY_V -> Key.V
    GLFW.GLFW_KEY_W -> Key.W
    GLFW.GLFW_KEY_X -> Key.X
    GLFW.GLFW_KEY_Y -> Key.Y
    GLFW.GLFW_KEY_Z -> Key.Z
    GLFW.GLFW_KEY_LEFT_BRACKET -> Key.LEFT_BRACKET
    GLFW.GLFW_KEY_BACKSLASH -> Key.BACKSLASH
    GLFW.GLFW_KEY_RIGHT_BRACKET -> Key.RIGHT_BRACKET
    GLFW.GLFW_KEY_GRAVE_ACCENT -> Key.GRAVE_ACCENT
    GLFW.GLFW_KEY_WORLD_1 -> Key.WORLD_1
    GLFW.GLFW_KEY_WORLD_2 -> Key.WORLD_2

    GLFW.GLFW_KEY_ESCAPE -> Key.ESCAPE
    GLFW.GLFW_KEY_ENTER -> Key.ENTER
    GLFW.GLFW_KEY_TAB -> Key.TAB
    GLFW.GLFW_KEY_BACKSPACE -> Key.BACKSPACE
    GLFW.GLFW_KEY_INSERT -> Key.INSERT
    GLFW.GLFW_KEY_DELETE -> Key.DELETE
    GLFW.GLFW_KEY_RIGHT -> Key.RIGHT
    GLFW.GLFW_KEY_LEFT -> Key.LEFT
    GLFW.GLFW_KEY_DOWN -> Key.DOWN
    GLFW.GLFW_KEY_UP -> Key.UP
    GLFW.GLFW_KEY_PAGE_UP -> Key.PAGE_UP
    GLFW.GLFW_KEY_PAGE_DOWN -> Key.PAGE_DOWN
    GLFW.GLFW_KEY_HOME -> Key.HOME
    GLFW.GLFW_KEY_END -> Key.END
    GLFW.GLFW_KEY_CAPS_LOCK -> Key.CAPS_LOCK
    GLFW.GLFW_KEY_SCROLL_LOCK -> Key.SCROLL_LOCK
    GLFW.GLFW_KEY_NUM_LOCK -> Key.NUM_LOCK
    GLFW.GLFW_KEY_PRINT_SCREEN -> Key.PRINT_SCREEN
    GLFW.GLFW_KEY_PAUSE -> Key.PAUSE
    GLFW.GLFW_KEY_F1 -> Key.F1
    GLFW.GLFW_KEY_F2 -> Key.F2
    GLFW.GLFW_KEY_F3 -> Key.F3
    GLFW.GLFW_KEY_F4 -> Key.F4
    GLFW.GLFW_KEY_F5 -> Key.F5
    GLFW.GLFW_KEY_F6 -> Key.F6
    GLFW.GLFW_KEY_F7 -> Key.F7
    GLFW.GLFW_KEY_F8 -> Key.F8
    GLFW.GLFW_KEY_F9 -> Key.F9
    GLFW.GLFW_KEY_F10 -> Key.F10
    GLFW.GLFW_KEY_F11 -> Key.F11
    GLFW.GLFW_KEY_F12 -> Key.F12
    GLFW.GLFW_KEY_F13 -> Key.F13
    GLFW.GLFW_KEY_F14 -> Key.F14
    GLFW.GLFW_KEY_F15 -> Key.F15
    GLFW.GLFW_KEY_F16 -> Key.F16
    GLFW.GLFW_KEY_F17 -> Key.F17
    GLFW.GLFW_KEY_F18 -> Key.F18
    GLFW.GLFW_KEY_F19 -> Key.F19
    GLFW.GLFW_KEY_F20 -> Key.F20
    GLFW.GLFW_KEY_F21 -> Key.F21
    GLFW.GLFW_KEY_F22 -> Key.F22
    GLFW.GLFW_KEY_F23 -> Key.F23
    GLFW.GLFW_KEY_F24 -> Key.F24
    GLFW.GLFW_KEY_F25 -> Key.F25
    GLFW.GLFW_KEY_KP_0 -> Key.KP_0
    GLFW.GLFW_KEY_KP_1 -> Key.KP_1
    GLFW.GLFW_KEY_KP_2 -> Key.KP_2
    GLFW.GLFW_KEY_KP_3 -> Key.KP_3
    GLFW.GLFW_KEY_KP_4 -> Key.KP_4
    GLFW.GLFW_KEY_KP_5 -> Key.KP_5
    GLFW.GLFW_KEY_KP_6 -> Key.KP_6
    GLFW.GLFW_KEY_KP_7 -> Key.KP_7
    GLFW.GLFW_KEY_KP_8 -> Key.KP_8
    GLFW.GLFW_KEY_KP_9 -> Key.KP_9
    GLFW.GLFW_KEY_KP_DECIMAL -> Key.KP_DECIMAL
    GLFW.GLFW_KEY_KP_DIVIDE -> Key.KP_DIVIDE
    GLFW.GLFW_KEY_KP_MULTIPLY -> Key.KP_MULTIPLY
    GLFW.GLFW_KEY_KP_SUBTRACT -> Key.KP_SUBTRACT
    GLFW.GLFW_KEY_KP_ADD -> Key.KP_ADD
    GLFW.GLFW_KEY_KP_ENTER -> Key.KP_ENTER
    GLFW.GLFW_KEY_KP_EQUAL -> Key.KP_EQUAL
    GLFW.GLFW_KEY_LEFT_SHIFT -> Key.LEFT_SHIFT
    GLFW.GLFW_KEY_LEFT_CONTROL -> Key.LEFT_CONTROL
    GLFW.GLFW_KEY_LEFT_ALT -> Key.LEFT_ALT
    GLFW.GLFW_KEY_LEFT_SUPER -> Key.LEFT_SUPER
    GLFW.GLFW_KEY_RIGHT_SHIFT -> Key.RIGHT_SHIFT
    GLFW.GLFW_KEY_RIGHT_CONTROL -> Key.RIGHT_CONTROL
    GLFW.GLFW_KEY_RIGHT_ALT -> Key.RIGHT_ALT
    GLFW.GLFW_KEY_RIGHT_SUPER -> Key.RIGHT_SUPER
    GLFW.GLFW_KEY_MENU -> Key.MENU
    else -> Key.UNKNOWN
}

fun Modifiers.Companion.fromGlfw(glfw: Int) = Modifiers(
    *listOfNotNull(
        if(glfw and GLFW.GLFW_MOD_SHIFT != 0) Modifier.SHIFT else null,
        if(glfw and GLFW.GLFW_MOD_CONTROL != 0) Modifier.CONTROL else null,
        if(glfw and GLFW.GLFW_MOD_ALT != 0) Modifier.ALT else null,
        if(glfw and GLFW.GLFW_MOD_SUPER != 0) Modifier.SUPER else null,
        if(glfw and GLFW.GLFW_MOD_CAPS_LOCK != 0) Modifier.CAPS_LOCK else null,
        if(glfw and GLFW.GLFW_MOD_NUM_LOCK != 0) Modifier.NUM_LOCK else null
    ).toTypedArray()
)

fun MouseButton.Companion.fromGlfw(glfw: Int): MouseButton {
    if(glfw + 1 !in 1 until MouseButton.values().size)
        return MouseButton.UNKNOWN
    return MouseButton.values()[glfw+1]
}
