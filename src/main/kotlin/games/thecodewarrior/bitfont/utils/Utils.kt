package games.thecodewarrior.bitfont.utils

import org.lwjgl.system.Platform

fun <T> ifMac(mac: T, others: () -> T): T {
    return if (Platform.get() == Platform.MACOSX) mac else others()
}
fun <T> ifMac(mac: T, others: T): T {
    return ifMac(mac) { others }
}