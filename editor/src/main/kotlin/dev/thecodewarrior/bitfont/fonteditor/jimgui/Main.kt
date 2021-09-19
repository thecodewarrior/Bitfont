package dev.thecodewarrior.bitfont.fonteditor.jimgui

import dev.thecodewarrior.bitfont.fonteditor.jimgui.imgui.ImGui
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.Colors
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.Constants
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.extensions.u32
import dev.thecodewarrior.bitfont.fonteditor.jimgui.utils.keys
import org.ice1000.jimgui.JImGuiIO
import org.ice1000.jimgui.util.JniLoader
import kotlin.math.max
import org.ice1000.jimgui.JImGui

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
        Constants // load class

        documents = mutableListOf(
            BitfontDocument.blank()
        )

        targetFPS = 20

        var imgui: ImGui? = null
        runPer({ 1000L / targetFPS }, { jimgui ->
            ImGui.current = imgui ?: ImGui(jimgui).also { imgui = it }
            mainLoop(ImGui.current)
        })
    }

    fun runPer(millisSupplier: () -> Long, runnable: (JImGui) -> Unit) {
        try {
            JImGui().use { imGui ->
                var latestRefresh = System.currentTimeMillis()
                imGui.initBeforeMainLoop()
                while (!imGui.windowShouldClose()) {
                    val targetTime = millisSupplier()
                    val deltaTime = System.currentTimeMillis() - latestRefresh
                    if (targetTime > deltaTime) {
                        Thread.sleep(targetTime - deltaTime)
                    }
                    val currentTimeMillis = System.currentTimeMillis()
                    imGui.initNewFrame()
                    runnable(imGui)
                    imGui.render()
                    latestRefresh = currentTimeMillis
                }
            }
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
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
            "/%d (%2.0f%%)".format(targetFPS, 100*(1 - waitHistory.sum()/ waitHistory.size))
        imgui.foregroundDrawList.addText(0f, 0f, Colors.white.u32, fpsText)
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