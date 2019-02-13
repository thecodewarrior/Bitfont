package games.thecodewarrior.bitfont.editor.utils.extensions

import games.thecodewarrior.bitfont.utils.Attribute
import java.awt.Color

private val colorAttr = Attribute.get<Color>("color")
val Attribute.Companion.color get() = colorAttr
