package games.thecodewarrior.bitfont.utils

import java.awt.Font

object ReferenceFonts {
    val familyNames: Array<String>
    val faceNames: Array<Array<String>>

    private val _referenceFonts: MutableMap<String, MutableMap<String, Font>> = mutableMapOf()
    val referenceFonts: Map<String, Map<String, Font>> = _referenceFonts

    init {
        Constants.resource("reference/fonts.txt")!!.bufferedReader().lineSequence().forEach { line ->
            val (family, name, file) = line.split("|").map { it.trim() }
            val resource = Constants.resource("reference/$file")!!
            _referenceFonts.getOrPut(family) { mutableMapOf() }[name] = Font.createFont(Font.TRUETYPE_FONT, resource)!!
        }

        familyNames = referenceFonts.keys.sorted().toTypedArray()
        faceNames = familyNames.map { referenceFonts[it]!!.keys.sorted().toTypedArray() }.toTypedArray()
    }

    fun setFontSize(family: Int, size: Float) {
        setFontSize(
            familyNames[family],
            size
        )
    }

    fun setFontSize(family: String, size: Float) {
        val faces = _referenceFonts[family]!!
        faces.keys.forEach {
            faces[it] = faces[it]!!.deriveFont(size)
        }
    }

    operator fun get(family: Int, face: Int): Font {
        return this[
            familyNames[family],
            faceNames[family][face]
        ]
    }

    operator fun get(family: String, face: String): Font {
        return referenceFonts[family]!![face]!!
    }
}