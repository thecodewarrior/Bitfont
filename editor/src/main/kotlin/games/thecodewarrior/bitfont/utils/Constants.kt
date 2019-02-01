package games.thecodewarrior.bitfont.utils

import com.beust.klaxon.Klaxon
import games.thecodewarrior.bitfont.Main
import games.thecodewarrior.bitfont.utils.extensions.u32
import games.thecodewarrior.bitfont.utils.nameslist.NamesList
import java.awt.Color
import java.io.InputStream

object Constants {
    fun resource(name: String): InputStream? = Main::class.java.getResourceAsStream(name)
    val namesList = NamesList()

    init {
        val before = System.currentTimeMillis()
        try {
            namesList.read(resource("NamesList.txt")!!)
        } catch(e: Exception) {
            e.printStackTrace()
        }
        val after = System.currentTimeMillis()
        println("Loaded NamesList.txt in ${after - before}ms")
    }
}