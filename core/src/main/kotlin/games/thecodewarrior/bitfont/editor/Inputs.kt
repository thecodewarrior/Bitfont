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
