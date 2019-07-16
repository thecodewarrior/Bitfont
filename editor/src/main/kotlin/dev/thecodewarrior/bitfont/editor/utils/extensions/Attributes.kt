package dev.thecodewarrior.bitfont.editor.utils.extensions

import dev.thecodewarrior.bitfont.utils.Attribute
import java.awt.Color

private val colorAttr = Attribute.get<Color>("color")
val Attribute.Companion.color get() = colorAttr
