package games.thecodewarrior.bitfont.utils

import com.beust.klaxon.Klaxon
import games.thecodewarrior.bitfont.Main
import games.thecodewarrior.bitfont.utils.extensions.u32
import java.awt.Color
import java.io.InputStream

object Constants {
    fun resource(name: String): InputStream? = Main::class.java.getResourceAsStream(name)
}