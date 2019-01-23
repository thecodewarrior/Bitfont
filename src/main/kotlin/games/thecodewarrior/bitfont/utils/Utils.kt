package games.thecodewarrior.bitfont.utils

import imgui.ImGui
import org.lwjgl.system.Platform
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel

inline fun <T> ifMac(f: () -> T): T? {
    if(ImGui.io.configMacOSXBehaviors)
        return f()
    return null
}
inline fun <T> ifMac(mac: T, others: () -> T): T {
    return if (ImGui.io.configMacOSXBehaviors) mac else others()
}
fun <T> ifMac(mac: T, others: T): T {
    return ifMac(mac) { others }
}

inline fun <T> ifMacSystem(f: () -> T): T? {
    if(Platform.get() == Platform.MACOSX)
        return f()
    return null
}
inline fun <T> ifMacSystem(mac: T, others: () -> T): T {
    return if (Platform.get() == Platform.MACOSX) mac else others()
}
fun <T> ifMacSystem(mac: T, others: T): T {
    return ifMacSystem(mac) { others }
}

fun Color.toHexString(): String {
    val a = if(alpha != 255) "%02x".format(alpha) else ""
    val r = "%02x".format(red)
    val g = "%02x".format(green)
    val b = "%02x".format(blue)

    return "#$a$r$g$b"
}

fun Color(hex: String): Color {
    val hexStr = hex.removePrefix("#")
    val a: Int
    val r: Int
    val g: Int
    val b: Int

    when(hexStr.length) {
        3 -> {
            a = 255
            r = "${hexStr[0]}${hexStr[0]}".toInt(16)
            g = "${hexStr[1]}${hexStr[1]}".toInt(16)
            b = "${hexStr[2]}${hexStr[2]}".toInt(16)
        }
        4 -> {
            a = "${hexStr[0]}${hexStr[0]}".toInt(16)
            r = "${hexStr[1]}${hexStr[1]}".toInt(16)
            g = "${hexStr[2]}${hexStr[2]}".toInt(16)
            b = "${hexStr[3]}${hexStr[3]}".toInt(16)
        }
        6 -> {
            a = 255
            r = hexStr.substring(0..1).toInt(16)
            g = hexStr.substring(2..3).toInt(16)
            b = hexStr.substring(4..5).toInt(16)
        }
        8 -> {
            a = hexStr.substring(0..1).toInt(16)
            r = hexStr.substring(2..3).toInt(16)
            g = hexStr.substring(4..5).toInt(16)
            b = hexStr.substring(6..7).toInt(16)
        }
        else ->
            throw IllegalArgumentException("Hex value '$hex' is not valid hex " +
                "(valid formats are #RGB, #ARGB, #RRGGBB, and #AARRGGBB)")
    }

    return Color(r, g, b, a)
}

fun IndexColorModel(vararg palette: Color): IndexColorModel {
    if(palette.size < 2 || palette.size > 65536) {
        throw IllegalArgumentException("Palette size ${palette.size} not in range [2, 65535].")
    }
    var bits = 0
    var maxIndex = palette.size-1
    while(maxIndex != 0) {
        bits++
        maxIndex = maxIndex ushr 1
    }

    val reds   = palette.map { it.red.toByte()   }.toByteArray()
    val greens = palette.map { it.green.toByte() }.toByteArray()
    val blues  = palette.map { it.blue.toByte()  }.toByteArray()
    val alphas = palette.map { it.alpha.toByte() }.toByteArray()
    return IndexColorModel(bits, palette.size, reds, greens, blues, alphas)
}

fun byteArrayOf(vararg ints: Int): ByteArray {
    return ints.map { it.toByte() }.toByteArray()
}

fun BufferedImage.isColor(x: Int, y: Int, color: Color): Boolean {
    return this.getRGB(x, y) == color.rgb
}

fun BufferedImage.isColor(startX: Int, startY: Int, width: Int, height: Int, color: Color): Boolean {
    val array = IntArray(width*height)
    this.getRGB(startX, startY, width, height, array, 0, width)
    return array.all { it == color.rgb }
}

fun BufferedImage.pixels(): Sequence<Pixel> {
    return PixelSequence(this)
}

data class Pixel(val x: Int, val y: Int, val color: Color)

class PixelSequence(val image: BufferedImage): Sequence<Pixel> {
    override fun iterator(): Iterator<Pixel> {
        return PixelIterator()
    }

    private inner class PixelIterator: Iterator<Pixel> {
        private var i = 0

        override fun hasNext(): Boolean {
            return i < image.width * image.height
        }

        override fun next(): Pixel {
            val x = i % image.width
            val y = i / image.width
            i++
            return Pixel(x, y, Color(image.getRGB(x, y)))
        }
    }
}
