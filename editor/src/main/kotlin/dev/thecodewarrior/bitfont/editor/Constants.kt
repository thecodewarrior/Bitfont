package dev.thecodewarrior.bitfont.editor

import dev.thecodewarrior.bitfont.editor.jimgui.utils.nameslist.NamesList
import org.lwjgl.BufferUtils
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer

object Constants {
    val namesList = NamesList()

    fun resourceOrNull(name: String): InputStream? {
        return GLFWDemo::class.java.getResourceAsStream(name)
    }

    fun resource(name: String): InputStream {
        return resourceOrNull(name) ?: throw FileNotFoundException(name)
    }

    fun readResourceBuffer(name: String): ByteBuffer {
        val bytes = resource(name).readBytes()
        val buffer = BufferUtils.createByteBuffer(bytes.size)
        buffer.put(bytes)
        buffer.flip()
        return buffer
    }

    init {
        val before = System.currentTimeMillis()
        try {
            namesList.read(resource("NamesList.txt"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val after = System.currentTimeMillis()
        println("Loaded NamesList.txt in ${after - before}ms")
    }
}