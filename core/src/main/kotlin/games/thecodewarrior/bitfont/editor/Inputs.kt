package games.thecodewarrior.bitfont.editor

/**
 * Note: Some keys may not be supported by all front-ends
 */
enum class Key {
    /** Unrecognized key */
    UNKNOWN,

    SPACE,
    APOSTROPHE,
    COMMA,
    MINUS,
    PERIOD,
    SLASH,
    NUM_0,
    NUM_1,
    NUM_2,
    NUM_3,
    NUM_4,
    NUM_5,
    NUM_6,
    NUM_7,
    NUM_8,
    NUM_9,
    SEMICOLON,
    EQUAL,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J,
    K,
    L,
    M,
    N,
    O,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X,
    Y,
    Z,
    /** left bracket ([) key */
    LEFT_BRACKET,
    /** backslash (\) key */
    BACKSLASH,
    /** right bracket (]) key */
    RIGHT_BRACKET,
    /** grave accent (`) key */
    GRAVE_ACCENT,
    /** section symbol (macOS) */
    SECTION,
    /** non-US key 1 (exists in GFLW) */
    WORLD_1,
    /** non-US key 1 (exists in GFLW) */
    WORLD_2,

    /** (Japanese keyboard) */
    KANA,
    /** (Japanese keyboard) */
    CONVERT,
    /** (Japanese keyboard) */
    NOCONVERT,
    /** (Japanese keyboard) */
    YEN,
    /** (Japanese keyboard) */
    CIRCUMFLEX,
    /** (Japanese NEC PC98 keyboard) */
    AT,
    /** (Japanese NEC PC98 keyboard) */
    COLON,
    /** (Japanese NEC PC98 keyboard) */
    UNDERLINE,
    /** (Japanese keyboard) */
    KANJI,
    /** (Japanese NEC PC98 keyboard) */
    STOP,
    /** (Japanese AX keyboard) */
    AX,
    /** (Japanese J3100 keyboard) */
    UNLABELED,

    /** escape/esc key */
    ESCAPE,
    /** enter/return key */
    ENTER,
    /** tab key */
    TAB,
    /** backspace key (macOS: delete) */
    BACKSPACE,
    /** insert key */
    INSERT,
    /** delete key (macOS: forward delete) */
    DELETE,
    /** clear key (macOS) */
    CLEAR,
    /** rightward arrow key */
    RIGHT,
    /** leftward arrow key */
    LEFT,
    /** downward arrow key */
    DOWN,
    /** upward arrow key */
    UP,
    /** page up key */
    PAGE_UP,
    /** page down key */
    PAGE_DOWN,
    /** home key */
    HOME,
    /** end key */
    END,
    /** caps lock key */
    CAPS_LOCK,
    /** scroll lock key */
    SCROLL_LOCK,
    /** num lock key */
    NUM_LOCK,
    /** print screen key */
    PRINT_SCREEN,

    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,
    F13,
    F14,
    F15,
    F16,
    F17,
    F18,
    F19,
    F20,
    F21,
    F22,
    F23,
    F24,
    F25,

    /** Keypad digit 0 */
    KP_0,
    /** Keypad digit 1 */
    KP_1,
    /** Keypad digit 2 */
    KP_2,
    /** Keypad digit 3 */
    KP_3,
    /** Keypad digit 4 */
    KP_4,
    /** Keypad digit 5 */
    KP_5,
    /** Keypad digit 6 */
    KP_6,
    /** Keypad digit 7 */
    KP_7,
    /** Keypad digit 8 */
    KP_8,
    /** Keypad digit 9 */
    KP_9,
    /** Keypad decimal point (.) key */
    KP_DECIMAL,
    /** Keypad divide (/) key */
    KP_DIVIDE,
    /** Keypad multiply (*) key */
    KP_MULTIPLY,
    /** Keypad subtract (-) key */
    KP_SUBTRACT,
    /** Keypad add (+) key */
    KP_ADD,
    /** Keypad enter key */
    KP_ENTER,
    /** Keypad equals (=) key */
    KP_EQUAL,
    /** Keypad comma (,) key */
    KP_COMMA,

    /** Function/fn modifier key */
    FUNCTION,
    /** Left shift modifier key */
    LEFT_SHIFT,
    /** Left control modifier key */
    LEFT_CONTROL,
    /** Left alt/option modifier key */
    LEFT_ALT,
    /** Left windows/command/super modifier key */
    LEFT_SUPER,
    /** Right shift modifier key */
    RIGHT_SHIFT,
    /** Right control modifier key */
    RIGHT_CONTROL,
    /** Right alt/option modifier key */
    RIGHT_ALT,
    /** Right windows/command/super modifier key */
    RIGHT_SUPER,

    /** pause key */
    PAUSE,
    /** Menu/app menu key (windows/linux) */
    MENU,
    /** Power button */
    POWER,
    /** Sleep button */
    SLEEP,
    /** SysRq/Sys Req/System Request key */
    SYSRQ
    ;

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

    val modifiers: Set<Modifier> = Modifier.values().filter { it in this }.toSet()
    val count = modifiers.size

    operator fun contains(mod: Modifier) = bits and mod.bitmask != 0
    operator fun contains(mods: Modifiers) = bits and mods.bits == mods.bits

    operator fun plus(mod: Modifier) = Modifiers(bits or mod.bitmask)
    operator fun plus(mods: Modifiers) = Modifiers(bits or mods.bits)

    operator fun minus(mod: Modifier) = Modifiers(bits and mod.bitmask.inv())
    operator fun minus(mods: Modifiers) = Modifiers(bits and mods.bits.inv())

    fun matches(mods: Modifiers) = this == mods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Modifiers) return false

        if (bits != other.bits) return false

        return true
    }

    override fun hashCode(): Int {
        return bits
    }

    companion object
}

class ModifierPattern(val required: Set<Modifier>, optional: Set<Modifier>) {
    val optional: Set<Modifier> = optional - required

    val all = required + optional

    // The `as Array<Modifier>` is required because mods is `Array<out Modifier>` due to being a vararg
    // However, as Modifier is an enum the `out` doesn't mean anything, so it's safe to cast
    @Suppress("UNCHECKED_CAST")
    fun require(vararg mods: Modifier) = ModifierPattern(required + setOf(*mods as Array<Modifier>), optional)
    @Suppress("UNCHECKED_CAST")
    fun optional(vararg mods: Modifier) = ModifierPattern(required, optional + setOf(*mods as Array<Modifier>))

    fun matches(mods: Modifiers) = mods.modifiers.containsAll(required) && all.containsAll(mods.modifiers)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModifierPattern) return false

        if (required != other.required) return false
        if (optional != other.optional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = required.hashCode()
        result = 31 * result + optional.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        val WILDCARD = ModifierPattern(emptySet(), Modifier.values().toSet())
        @JvmStatic
        val NONE = ModifierPattern(emptySet(), emptySet())

        // The `as Array<Modifier>` is required because mods is `Array<out Modifier>` due to being a vararg
        // However, as Modifier is an enum the `out` doesn't mean anything, so it's safe to cast
        @Suppress("UNCHECKED_CAST")
        fun require(vararg mods: Modifier) = ModifierPattern(setOf(*mods as Array<Modifier>), emptySet())
        @Suppress("UNCHECKED_CAST")
        fun optional(vararg mods: Modifier) = ModifierPattern(emptySet(), setOf(*mods as Array<Modifier>))
    }
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
