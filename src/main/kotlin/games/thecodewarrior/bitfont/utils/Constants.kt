package games.thecodewarrior.bitfont.utils

import com.beust.klaxon.Klaxon
import games.thecodewarrior.bitfont.Main
import games.thecodewarrior.bitfont.utils.extensions.u32
import imgui.Color
import java.io.InputStream

object Constants {
    val editorBackground = col("ff_0A0A0A")
    val editorAxes = SimpleColors.orange
    val editorSelection = SimpleColors.magenta
    val editorGrid = col("ff_3b3b46")
    val editorGuides = SimpleColors.blue
    val editorAdvance = SimpleColors.green

    fun col(hex: String): Int {
        val int = hex.replace("_", "").toUInt(16)

        return Color(
            (int shr 16 and 0xFFu).toInt(),
            (int shr  8 and 0xFFu).toInt(),
            (int shr  0 and 0xFFu).toInt(),
            (int shr 24 and 0xFFu).toInt()
        ).u32
    }

    fun resource(name: String): InputStream? = Main::class.java.getResourceAsStream(name)

    object SimpleColors {
        val maroon = col("ff_800000")
        val red = col("ff_e6194b")
        val pink = col("ff_fabebe")

        val brown = col("ff_9a6324")
        val orange = col("ff_f58231")

        val yellow = col("ff_ffe119")
        val beige = col("ff_fffac8")

        val green = col("ff_3cb44b")
        val mint = col("ff_aaffc3")

        val teal = col("ff_469990")
        val cyan = col("ff_42d4f4")

        val navy = col("ff_000075")
        val blue = col("ff_4363d8")

        val lavender = col("ff_e6beff")
        val magenta = col("ff_f032e6")

        val black = col("ff_000000")
        val grey = col("ff_a9a9a9")
        val white = col("ff_ffffff")

        val maroonAwt = Color("800000")
        val redAwt = Color("e6194b")
        val pinkAwt = Color("fabebe")

        val brownAwt = Color("9a6324")
        val orangeAwt = Color("f58231")

        val yellowAwt = Color("ffe119")
        val beigeAwt = Color("fffac8")

        val greenAwt = Color("3cb44b")
        val mintAwt = Color("aaffc3")

        val tealAwt = Color("469990")
        val cyanAwt = Color("42d4f4")

        val navyAwt = Color("000075")
        val blueAwt = Color("4363d8")

        val lavenderAwt = Color("e6beff")
        val magentaAwt = Color("f032e6")

        val blackAwt = Color("000000")
        val greyAwt = Color("a9a9a9")
        val whiteAwt = Color("ffffff")
    }
}