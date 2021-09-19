package dev.thecodewarrior.bitfont.fonteditor.utils

import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*

@Suppress("NOTHING_TO_INLINE")
inline fun NkRect.set(x: Number, y: Number, w: Number, h: Number): NkRect {
    return nk_rect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), this)
}