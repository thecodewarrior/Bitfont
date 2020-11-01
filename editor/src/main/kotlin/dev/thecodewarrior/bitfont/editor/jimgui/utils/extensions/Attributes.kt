package dev.thecodewarrior.bitfont.editor.jimgui.utils.extensions

import dev.thecodewarrior.bitfont.typesetting.TextAttribute
import java.awt.Color

private val colorAttr = TextAttribute.get<Color>("color")
val TextAttribute.Companion.color get() = colorAttr
