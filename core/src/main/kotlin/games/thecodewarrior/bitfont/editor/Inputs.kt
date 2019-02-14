package games.thecodewarrior.bitfont.editor


enum class Key {
    UNKNOWN(),
    SPACE(),
    APOSTROPHE(),
    COMMA(),
    MINUS(),
    PERIOD(),
    SLASH(),
    NUM_0(),
    NUM_1(),
    NUM_2(),
    NUM_3(),
    NUM_4(),
    NUM_5(),
    NUM_6(),
    NUM_7(),
    NUM_8(),
    NUM_9(),
    SEMICOLON(),
    EQUAL(),
    A(),
    B(),
    C(),
    D(),
    E(),
    F(),
    G(),
    H(),
    I(),
    J(),
    K(),
    L(),
    M(),
    N(),
    O(),
    P(),
    Q(),
    R(),
    S(),
    T(),
    U(),
    V(),
    W(),
    X(),
    Y(),
    Z(),
    LEFT_BRACKET(),
    BACKSLASH(),
    RIGHT_BRACKET(),
    GRAVE_ACCENT(),
    WORLD_1(),
    WORLD_2(),

    ESCAPE(),
    ENTER(),
    TAB(),
    BACKSPACE(),
    INSERT(),
    DELETE(),
    RIGHT(),
    LEFT(),
    DOWN(),
    UP(),
    PAGE_UP(),
    PAGE_DOWN(),
    HOME(),
    END(),
    CAPS_LOCK(),
    SCROLL_LOCK(),
    NUM_LOCK(),
    PRINT_SCREEN(),
    PAUSE(),
    F1(),
    F2(),
    F3(),
    F4(),
    F5(),
    F6(),
    F7(),
    F8(),
    F9(),
    F10(),
    F11(),
    F12(),
    F13(),
    F14(),
    F15(),
    F16(),
    F17(),
    F18(),
    F19(),
    F20(),
    F21(),
    F22(),
    F23(),
    F24(),
    F25(),
    KP_0(),
    KP_1(),
    KP_2(),
    KP_3(),
    KP_4(),
    KP_5(),
    KP_6(),
    KP_7(),
    KP_8(),
    KP_9(),
    KP_DECIMAL(),
    KP_DIVIDE(),
    KP_MULTIPLY(),
    KP_SUBTRACT(),
    KP_ADD(),
    KP_ENTER(),
    KP_EQUAL(),
    LEFT_SHIFT(),
    LEFT_CONTROL(),
    LEFT_ALT(),
    LEFT_SUPER(),
    RIGHT_SHIFT(),
    RIGHT_CONTROL(),
    RIGHT_ALT(),
    RIGHT_SUPER(),
    MENU();

    companion object
}

enum class Modifier {
    SHIFT(),
    CONTROL(),
    ALT(),
    SUPER(),
    CAPS_LOCK(),
    NUM_LOCK();

    val bitmask = 1 shl this.ordinal

    companion object
}

class Modifiers private constructor(private val bits: Int) {
    constructor(vararg mods: Modifier): this(mods.fold(0) { bits, mod -> bits or mod.bitmask })

    val modifiers: List<Modifier> get() = Modifier.values().filter { it in this }

    operator fun contains(mod: Modifier) = bits and mod.bitmask != 0
    operator fun contains(mods: Modifiers) = bits and mods.bits == mods.bits

    operator fun plus(mod: Modifier) = Modifiers(bits or mod.bitmask)
    operator fun plus(mods: Modifiers) = Modifiers(bits or mods.bits)

    operator fun minus(mod: Modifier) = Modifiers(bits and mod.bitmask.inv())
    operator fun minus(mods: Modifiers) = Modifiers(bits and mods.bits.inv())

    companion object
}

enum class MouseButton {
    UNKNOWN(),
    BUTTON_1(),
    BUTTON_2(),
    BUTTON_3(),
    BUTTON_4(),
    BUTTON_5(),
    BUTTON_6(),
    BUTTON_7(),
    BUTTON_8(),
    BUTTON_9(),
    BUTTON_10(),
    BUTTON_11(),
    BUTTON_12(),
    BUTTON_13(),
    BUTTON_14(),
    BUTTON_15(),
    BUTTON_16();

    companion object {
        val LEFT   = BUTTON_1
        val RIGHT  = BUTTON_2
        val MIDDLE = BUTTON_3
    }
}
