package games.thecodewarrior.bitfont.editor

import games.thecodewarrior.bitfont.editor.utils.Colors
import games.thecodewarrior.bitfont.editor.utils.Constants
import games.thecodewarrior.bitfont.editor.utils.extensions.u32
import games.thecodewarrior.bitfont.editor.utils.ifMacSystem
import games.thecodewarrior.bitfont.editor.utils.keys
import games.thecodewarrior.bitfont.editor.utils.opengl.Java2DTexture
import glm_.vec2.Vec2
import gln.checkError
import gln.glClearColor
import gln.glViewport
import imgui.Context
import imgui.ImGui
import imgui.destroy
import imgui.impl.ImplGL3
import imgui.impl.LwjglGlfw
import imgui.impl.LwjglGlfw.GlfwClientApi
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.system.MemoryStack
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.glfw.windowHint
import kotlin.math.max
import kotlin.math.min

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    Main.run()
}

object Main {

    lateinit var window: GlfwWindow
    lateinit var ctx: Context

    var showDemo = true

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
        glfw.init(ifMacSystem("3.2", "3.0"))
        ifMacSystem {
            glfw.windowHint {
                profile = windowHint.Profile.core
                forwardComp = true
            }
        }

        window = GlfwWindow(1280, 720, "Bitfont font editor").apply {
            init()
        }
        window.setSizeLimit(800 .. 2400, 300 .. 1500)

        glfw.swapInterval = 1   // Enable vsync

        // Setup ImGui binding
        ctx = Context()
        ImGui.io.iniFilename = null
        ImGui.io.configMacOSXBehaviors = ifMacSystem(true, false)
        LwjglGlfw.init(window, true, GlfwClientApi.OpenGL)

        ImGui.styleColorsDark()

        documents = mutableListOf(
            BitfontDocument.blank()
        )

        Constants // load class

        targetFPS = 20
        window.loop(Main::mainLoop)

        LwjglGlfw.shutdown()
        ctx.destroy()

        window.destroy()
        glfw.terminate()
    }

    fun mainLoop(stack: MemoryStack) {
        val timeLeft = (lastFrameTime + targetFrameDuration) - System.currentTimeMillis()
        if(timeLeft > 0)
            Thread.sleep(timeLeft)
        waitHistory[frameNum++ % waitHistory.size] = timeLeft / targetFrameDuration.toDouble()
        lastFrameTime = System.currentTimeMillis()

        Java2DTexture.cleanUpTextures()

        // Start the Dear ImGui frame
        LwjglGlfw.newFrame()

        with(ImGui) {
            if (showDemo)
                showDemoWindow(Main::showDemo)

            documents.toList().forEach { it.push() }
            val fpsText = "%4.1f".format(io.framerate) + if(targetFPS == 0)
                ""
            else
                "/%d (%2.0f%%)".format(targetFPS, 100*(1 - waitHistory.sum()/waitHistory.size))
            overlayDrawList.addText(Vec2(0, 0), Colors.white.u32, fpsText.toCharArray())
            keys {
                "prim+[" pressed {
                    targetFPS--
                }
                "prim+]" pressed {
                    targetFPS++
                }
            }
        }

        // Rendering
        glViewport(window.framebufferSize)
        glClearColor(Colors.main.background)
        glClear(GL_COLOR_BUFFER_BIT)

        Java2DTexture.updateTextures()

        ImGui.render()
        ImplGL3.renderDrawData(ImGui.drawData!!)

        checkError("mainLoop") // TODO remove in production
    }
}