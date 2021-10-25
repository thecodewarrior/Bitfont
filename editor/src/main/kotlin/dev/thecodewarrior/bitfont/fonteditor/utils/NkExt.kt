@file:Suppress("NOTHING_TO_INLINE")
package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.nuklear.NkColor
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import java.awt.Color

inline fun NkRect.set(x: Number, y: Number, w: Number, h: Number): NkRect {
    return this.set(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat())
}

inline fun NkColor.set(color: Color): NkColor {
    return this.set(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte())
}
