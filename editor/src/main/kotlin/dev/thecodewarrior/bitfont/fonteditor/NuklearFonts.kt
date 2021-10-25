package dev.thecodewarrior.bitfont.fonteditor

import java.io.InputStream
import java.util.concurrent.CompletableFuture

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

class FontList private constructor(private val entries: List<FontEntry>): Iterable<TTFFont> {
    fun filter(style: String? = null, weight: String? = null): FontList = FontList(entries.filter {
        (style == null || it.style == style) && (weight == null || it.weight == weight)
    })

    operator fun plus(other: FontList): FontList {
        return FontList(entries + other.entries)
    }

    override fun iterator(): Iterator<TTFFont> = object : Iterator<TTFFont> {
        val entryIterator = entries.iterator()
        override fun hasNext(): Boolean = entryIterator.hasNext()
        override fun next(): TTFFont = entryIterator.next().font.get()
    }

    private data class FontEntry(val style: String, val weight: String, val font: CompletableFuture<TTFFont>)

    companion object {
        fun read(base: String, stream: InputStream): FontList {
            return FontList(stream.bufferedReader().lineSequence().mapNotNull { line ->
                if (line.trim().startsWith("//")) {
                    null
                } else {
                    val (style, sort, weight, file) = line.trim().split(":")
                    FontEntry(style.trim(), weight.trim(), CompletableFuture.supplyAsync {
                        TTFFont(base + file.trim())
                    })
                }
            }.toList())
        }
    }
}
