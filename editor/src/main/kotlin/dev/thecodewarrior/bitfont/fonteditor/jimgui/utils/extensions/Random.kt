package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.extensions

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

val random: Random get() = ThreadLocalRandom.current()