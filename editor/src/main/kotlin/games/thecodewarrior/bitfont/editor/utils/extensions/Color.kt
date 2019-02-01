package games.thecodewarrior.bitfont.editor.utils.extensions

import glm_.vec4.Vec4
import imgui.ImGui.u32

typealias IColor = imgui.Color
typealias JColor = java.awt.Color

// imgui

val IColor.u32: Int
    get() = value.u32

fun IColor.toAwt(): JColor
    = JColor(value.x, value.y, value.z, value.w)

// awt

val JColor.u32: Int
    get() = IColor(red, green, blue, alpha).u32

val JColor.vec4: Vec4
    get() = Vec4(red/255f, green/255f, blue/255f, alpha/255f)

fun JColor.toImGui(): IColor
    = IColor(red, green, blue, alpha)
