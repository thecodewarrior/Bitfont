package dev.thecodewarrior.bitfont.editor.utils.extensions

import org.ice1000.jimgui.JImVec4
import java.awt.Color

typealias IColor = JImVec4
typealias JColor = Color

// imgui

val IColor.u32: Int
    get() = toU32()

val IColor.awt: JColor
    get() = toAWT()

// awt

val JColor.im: IColor
    get() = JImVec4.fromAWT(this)

val JColor.u32: Int
    get() = this.im.u32

fun JColor.copy(
    red: Int = this.red,
    green: Int = this.green,
    blue: Int = this.blue,
    alpha: Int = this.alpha
) = JColor(red, green, blue, alpha)

fun JColor.copy(
    red: Float = this.red / 255f,
    green: Float = this.green / 255f,
    blue: Float = this.blue / 255f,
    alpha: Float = this.alpha / 255f
) = JColor(red, green, blue, alpha)
