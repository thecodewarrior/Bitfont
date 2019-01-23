package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.data.BitFont
import games.thecodewarrior.bitfont.utils.ifMacSystem
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

    val clearColor = Vec4(0.45f, 0.55f, 0.6f, 1f)
    var showDemo = true

    val windows = mutableListOf<IMWindow>(
        FontWindow(BitFont("Font", 16, 10, 4, 9, 6)).also { it.visible = true }
    )

    init {
        glfw.init(ifMacSystem("3.2", "3.0"))
        ifMacSystem {
            glfw.windowHint {
                windowHint.profile = windowHint.Profile.core
                windowHint.forwardComp = true
            }
        }

        window = GlfwWindow(1280, 720, "Dear ImGui Lwjgl OpenGL3 example").apply {
            init()
        }

        glfw.swapInterval = 1   // Enable vsync

        // Setup ImGui binding
        ctx = Context()
        ImGui.io.iniFilename = null
        ImGui.io.configMacOSXBehaviors = ifMacSystem(true, false)
        LwjglGlfw.init(window, true, GlfwClientApi.OpenGL)

        ImGui.styleColorsDark()

        window.loop(::mainLoop)

        LwjglGlfw.shutdown()
        ctx.destroy()

        window.destroy()
        glfw.terminate()
    }

    fun mainLoop(stack: MemoryStack) {

        // Start the Dear ImGui frame
        LwjglGlfw.newFrame()

        with(ImGui) {
            if (showDemo)
                showDemoWindow(::showDemo)

            windows.toList().forEach { it.push() }
        }

        // Rendering
        glViewport(window.framebufferSize)
        glClearColor(clearColor)
        glClear(GL_COLOR_BUFFER_BIT)

        ImGui.render()
        ImplGL3.renderDrawData(ImGui.drawData!!)

        checkError("mainLoop") // TODO remove in production
    }
}