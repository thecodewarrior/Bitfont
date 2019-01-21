package games.thecodewarrior.bitfont

import games.thecodewarrior.bitfont.utils.ifMac
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
import org.lwjgl.system.Platform
import uno.glfw.GlfwWindow
import uno.glfw.glfw
import uno.glfw.windowHint

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")
    HelloWorld_lwjgl()
}

private class HelloWorld_lwjgl {

    val window: GlfwWindow
    val ctx: Context


    init {
        glfw.init(ifMac("3.2", "3.0"))
        if(Platform.get() == Platform.MACOSX) {
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
//         glslVersion = 330 // set here your desidered glsl version
        ctx = Context()
        ImGui.io.iniFilename = null
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableKeyboard  // Enable Keyboard Controls
        //io.configFlags = io.configFlags or ConfigFlag.NavEnableGamepad   // Enable Gamepad Controls
        LwjglGlfw.init(window, true, GlfwClientApi.OpenGL)

        // Setup Style
        ImGui.styleColorsDark()
//        ImGui.styleColorsClassic()

        // Load Fonts
        /*  - If no fonts are loaded, dear imgui will use the default font. You can also load multiple fonts and use
                pushFont()/popFont() to select them.
            - addFontFromFileTTF() will return the Font so you can store it if you need to select the font among multiple.
            - If the file cannot be loaded, the function will return null. Please handle those errors in your application
                (e.g. use an assertion, or display an error and quit).
            - The fonts will be rasterized at a given size (w/ oversampling) and stored into a texture when calling
                FontAtlas.build()/getTexDataAsXXXX(), which ImGui_ImplXXXX_NewFrame below will call.
            - Read 'misc/fonts/README.txt' for more instructions and details.
            - Remember that in C/C++ if you want to include a backslash \ in a string literal you need to write
                a double backslash \\ ! */
        //io.Fonts->AddFontDefault();
        //io.Fonts->AddFontFromFileTTF("../../misc/fonts/Roboto-Medium.ttf", 16.0f);
        //io.Fonts->AddFontFromFileTTF("../../misc/fonts/Cousine-Regular.ttf", 15.0f);
        //io.Fonts->AddFontFromFileTTF("../../misc/fonts/DroidSans.ttf", 16.0f);
        //io.Fonts->AddFontFromFileTTF("../../misc/fonts/ProggyTiny.ttf", 10.0f);
//        ImGui.io.fonts.addFontFromFileTTF("extraFonts/ArialUni.ttf", 18f, glyphRanges = imgui.glyphRanges.japanese)!!
//        val a = IO.fonts.addFontFromFileTTF("misc/fonts/ArialUni.ttf", 18f)!!
//        val b = IO.fonts.addFontFromFileTTF("misc/fonts/ArialUni.ttf", 30f)!!

        /*  Main loop
            This automatically also polls events, swaps buffers and resets the appBuffer
            Poll and handle events (inputs, window resize, etc.)
            You can read the io.wantCaptureMouse, io.wantCaptureKeyboard flags to tell if dear imgui wants to use your inputs.
            - When io.wantCaptureMouse is true, do not dispatch mouse input data to your main application.
            - When io.wantCaptureKeyboard is true, do not dispatch keyboard input data to your main application.
            Generally you may always pass all inputs to dear imgui, and hide them from your application based on those two flags.          */
        window.loop(::mainLoop)

        LwjglGlfw.shutdown()
        ctx.destroy()

        window.destroy()
        glfw.terminate()
    }

    fun mainLoop(stack: MemoryStack) {

        // Start the Dear ImGui frame
        LwjglGlfw.newFrame()

        App.mainLoop()

        // Rendering
        glViewport(window.framebufferSize)
        glClearColor(App.clearColor)
        glClear(GL_COLOR_BUFFER_BIT)

        ImGui.render()
        ImplGL3.renderDrawData(ImGui.drawData!!)

        checkError("mainLoop") // TODO remove in production
    }
}