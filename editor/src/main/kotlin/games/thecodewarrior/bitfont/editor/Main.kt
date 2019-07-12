package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.imgui.ImGui
import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.Constants
import games.thecodewarrior.bitfont.editor.utils.keys
import org.ice1000.jimgui.JImGuiIO
import org.ice1000.jimgui.util.JImGuiUtil
import org.ice1000.jimgui.util.JniLoader
import kotlin.math.max

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    Main.run()
}

object Main {

    lateinit var documents: MutableList<BitfontDocument>

    var targetFPS = 0
        set(value) {
            field = max(0, value)
            if(field == 0)
                targetFrameDuration = 0
            else
                targetFrameDuration = 1000/field
            waitHistory = DoubleArray(max(1, field*2))
        }
    var lastFrameTime = System.currentTimeMillis()
        private set
    var targetFrameDuration = 0
        private set
    var frameNum = 0
        private set
    var waitHistory = DoubleArray(20)
        private set

    fun run() {
        JniLoader.load()
        var imgui: ImGui? = null
        JImGuiUtil.runPer({ 1000L / targetFPS }, { jimgui ->
            ImGui.current = imgui ?: ImGui(jimgui).also { imgui = it }
            mainLoop(ImGui.current)
        })

        documents = mutableListOf(
            BitfontDocument.blank()
        )

        Constants // load class

        targetFPS = 20
    }

    fun mainLoop(imgui: ImGui) {
        val timeLeft = (lastFrameTime + targetFrameDuration) - System.currentTimeMillis()
        waitHistory[frameNum++ % waitHistory.size] = timeLeft / targetFrameDuration.toDouble()
        lastFrameTime = System.currentTimeMillis()

        if(documents.isEmpty())
            documents.add(BitfontDocument.blank())
        documents.toList().forEach { it.push(imgui) }
        val fpsText = "%4.1f".format(JImGuiIO.getFramerate()) + if(targetFPS == 0)
            ""
        else
            "/%d (%2.0f%%)".format(targetFPS, 100*(1 - waitHistory.sum()/waitHistory.size))
        imgui.windowDrawList.addText(0f, 0f, Colors.white.rgb, fpsText)
        imgui.keys {
            "prim+[" pressed {
                targetFPS--
            }
            "prim+]" pressed {
                targetFPS++
            }
        }
    }
}