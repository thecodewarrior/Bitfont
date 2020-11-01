package dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions

inline fun <T: Comparable<T>> T.clamp(min: T, max: T): T = if(this < min) min else if(this > max) max else this