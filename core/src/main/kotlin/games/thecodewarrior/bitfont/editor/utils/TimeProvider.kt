package games.thecodewarrior.bitfont.editor.utils

interface TimeProvider {
    /**
     * The current time in milliseconds relative to some fixed base time
     */
    val time: Long
}

object SystemTimeProvider: TimeProvider {
    override val time: Long
        get() = System.currentTimeMillis()
}