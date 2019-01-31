package games.thecodewarrior.bitfont.utils.extensions

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

val random: Random get() = ThreadLocalRandom.current()