package dev.thecodewarrior.bitfont.editor.utils.extensions

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

val random: Random get() = ThreadLocalRandom.current()