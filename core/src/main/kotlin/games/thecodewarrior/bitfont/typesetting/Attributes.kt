package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Attribute
import java.awt.Color

private val fontAttr = Attribute.get<Bitfont>("font")
val Attribute.Companion.font get() = fontAttr
