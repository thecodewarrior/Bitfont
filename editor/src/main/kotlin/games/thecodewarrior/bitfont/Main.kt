package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Colors
import games.thecodewarrior.bitfont.utils.Constants
import games.thecodewarrior.bitfont.utils.ifMacSystem
import games.thecodewarrior.bitfont.utils.opengl.Java2DTexture
import glm_.vec4.Vec4
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

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    Main
}

object Main {

    val window: GlfwWindow
    val ctx: Context

    var showDemo = true

    val documents: MutableList<BitfontDocument>

    init {
        glfw.init(ifMacSystem("3.2", "3.0"))
        ifMacSystem {
            glfw.windowHint {
                windowHint.profile = windowHint.Profile.core
                windowHint.forwardComp = true
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

        window.loop(::mainLoop)

        LwjglGlfw.shutdown()
        ctx.destroy()

        window.destroy()
        glfw.terminate()
    }

    fun mainLoop(stack: MemoryStack) {

        Java2DTexture.cleanUpTextures()

        // Start the Dear ImGui frame
        LwjglGlfw.newFrame()

        with(ImGui) {
            if (showDemo)
                showDemoWindow(::showDemo)

            documents.toList().forEach { it.push() }
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