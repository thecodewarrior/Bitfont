package games.thecodewarrior.bitfont.utils

import games.thecodewarrior.bitfont.App
import games.thecodewarrior.bitfont.utils.extensions.u32
import imgui.Color
import java.awt.Font
import java.io.InputStream
import java.util.SortedMap
import java.util.TreeMap

object Constants {
    val editorBackground = col("ff_0A0A0A")
    val editorAxes = col("ff_ff7a64")
    val editorSelection = col("ff_ff7a64")
    val editorGrid = col("ff_3b3b46")
    val editorGuides = col("ff_3d73ad")
    val editorKernProfile = col("ff_861813")

    fun col(hex: String): Int {
        val int = hex.replace("_", "").toUInt(16)

        return Color(
            (int shr 16 and 0xFFu).toInt(),
            (int shr  8 and 0xFFu).toInt(),
            (int shr  0 and 0xFFu).toInt(),
            (int shr 24 and 0xFFu).toInt()
        ).u32
    }

    fun resource(name: String): InputStream? = App::class.java.getResourceAsStream(name)
}