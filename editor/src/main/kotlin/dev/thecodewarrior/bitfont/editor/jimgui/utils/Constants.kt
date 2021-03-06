package dev.thecodewarrior.bitfont.editor.jimgui.utils

import dev.thecodewarrior.bitfont.editor.jimgui.Main
import dev.thecodewarrior.bitfont.editor.jimgui.utils.nameslist.NamesList
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