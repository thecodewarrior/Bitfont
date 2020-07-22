package dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

val random: Random get() = ThreadLocalRandom.current()