package dev.thecodewarrior.bitfont.fonteditor.jimgui.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.awt.Font

object ReferenceFonts {
    val serifDisplay: FallbackFont
    val serifMono: FallbackFont
    val serif: FallbackFont

    val sansDisplay: FallbackFont
    val sansMono: FallbackFont
    val sans: FallbackFont

    val styles: List<String>
    val styleFonts: List<FallbackFont>

    var fonts = mutableMapOf<String, Font>()

    init {
        val all = mutableListOf<String>()
        val serifd = mutableListOf<String>()
        val serifm = mutableListOf<String>()
        val serif = mutableListOf<String>()

        val sansd = mutableListOf<String>()
        val sansm = mutableListOf<String>()
        val sans = mutableListOf<String>()

        val other = mutableListOf<String>()

        Constants.resource("reference/fonts.txt")!!.bufferedReader().lineSequence().drop(1).forEach { line ->
            val (style, _, _, file) = line.trim().split(":")

            all.add(file)
            when(style) {
                "serifd" -> serifd.add(file)
                "serifm" -> serifm.add(file)
                "serif" -> serif.add(file)

                "sansd" -> sansd.add(file)
                "sansm" -> sansm.add(file)
                "sans" -> sans.add(file)

                "other" -> other.add(file)
            }
        }

        serifDisplay = FallbackFont(fonts, serifd + serif + sans + other)
        serifMono = FallbackFont(fonts, serifm + serif + sans + other)
        ReferenceFonts.serif = FallbackFont(fonts, serif + sans + other)

        sansDisplay = FallbackFont(fonts, sansd + sans + other)
        sansMono = FallbackFont(fonts, sansm + sans + other)
        ReferenceFonts.sans = FallbackFont(fonts, sans + other)

        all.forEach {
            val resource = Constants.resource("reference/$it")!!
            fonts[it] = Font.createFont(Font.TRUETYPE_FONT, resource)!!
        }

        styles = listOf(
            "Serif", "Sans-Serif",
            "Serif Display", "Sans-Serif Display",
            "Serif Mono", "Sans-Serif Mono"
        )
        styleFonts = listOf(
            ReferenceFonts.serif, ReferenceFonts.sans,
            serifDisplay, sansDisplay,
            serifMono, sansMono
        )
    }

    fun style(i: Int): FallbackFont {
        return styleFonts[i]
    }
}

class FallbackFont(val fonts: Map<String, Font>, val route: List<String>) {
    val cache = Int2ObjectOpenHashMap<String>()

    operator fun get(codepoint: Int): Font {
        return fonts[fontName(codepoint)]!!
    }

    fun fontName(codepoint: Int): String {
        return cache.getOrPut(codepoint) {
            for(name in route) {
                if(fonts[name]!!.canDisplay(codepoint)) return@getOrPut name
            }
            return@getOrPut route[0]
        }
    }
}