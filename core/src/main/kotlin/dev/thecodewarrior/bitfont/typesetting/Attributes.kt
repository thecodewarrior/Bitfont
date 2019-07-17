package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.Attribute
import java.awt.Color

private val fontAttr = Attribute.get<Bitfont>("font")
val Attribute.Companion.font get() = fontAttr
