package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.utils.GlobalAllocations
import java.io.InputStream

object NuklearFonts {
    val noto: FontList = FontList.read("reference/Noto/", Constants.resource("reference/Noto/fonts.txt"))
    val notoCJK: FontList = FontList.read("reference/Noto-CJK/", Constants.resource("reference/Noto-CJK/fonts.txt"))

    val sans: FontList = noto.filter(style = "Sans") + noto.filter(style = "Serif") +
        notoCJK.filter(style = "Sans") + notoCJK.filter(style = "Serif")
    val serif: FontList = noto.filter(style = "Serif") + noto.filter(style = "Sans") +
        notoCJK.filter(style = "Serif") + notoCJK.filter(style = "Sans")
    val mono: FontList = noto.filter(style = "Mono")

    private val sansFonts = mutableMapOf<Pair<String, Float>, FontAtlas>()
    private val serifFonts = mutableMapOf<Pair<String, Float>, FontAtlas>()
    private val monoFonts = mutableMapOf<Pair<String, Float>, FontAtlas>()

    @JvmStatic
    fun getSans(weight: String, size: Float): FontAtlas = sansFonts.getOrPut(weight to size) {
        FontAtlas(sans.filter(weight = weight), size)
    }

    @JvmStatic
    fun getSerif(weight: String, size: Float): FontAtlas = serifFonts.getOrPut(weight to size) {
        FontAtlas(serif.filter(weight = weight), size)
    }

    @JvmStatic
    fun getMono(weight: String, size: Float): FontAtlas = monoFonts.getOrPut(weight to size) {
        FontAtlas(mono.filter(weight = weight), size)
    }
}

data class FontList(val entries: List<TTFFont>): List<TTFFont> by entries {
    fun filter(style: String? = null, weight: String? = null): FontList = FontList(entries.filter {
        (style == null || it.style == style) && (weight == null || it.weight == weight)
    })

    inline fun filter(block: (TTFFont) -> Boolean): FontList = FontList(entries.filter { block(it) })

    operator fun plus(other: FontList): FontList {
        return FontList(entries + other.entries)
    }

    companion object {
        fun read(base: String, stream: InputStream): FontList {
            return FontList(stream.bufferedReader().lineSequence().mapNotNull { line ->
                if (line.trim().startsWith("//")) {
                    null
                } else {
                    val (style, sort, weight, file) = line.trim().split(":")
                    TTFFont(style.trim(), weight.trim(), base + file.trim())
                }
            }.toList())
        }
    }
}
