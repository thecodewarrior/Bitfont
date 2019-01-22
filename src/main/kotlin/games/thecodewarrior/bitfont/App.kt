package games.thecodewarrior.bitfont

import glm_.func.common.clamp
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.functionalProgramming.button

object App {

    var f = 0f
    val clearColor = Vec4(0.45f, 0.55f, 0.6f, 1f)
    var showDemo = true


    val mainWindow = FontWindow()

    fun mainLoop() = with(ImGui) {
        // 1. Show the big demo window (Most of the sample code is in ImGui::ShowDemoWindow()! You can browse its code to learn more about Dear ImGui!).
        if (showDemo)
            showDemoWindow(::showDemo)

        mainWindow.visible = true
        mainWindow.push()
    }

}