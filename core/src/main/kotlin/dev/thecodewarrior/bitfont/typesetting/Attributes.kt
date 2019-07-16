package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Bitfont
import dev.thecodewarrior.bitfont.utils.Attribute
import java.awt.Color

private val fontAttr = Attribute.get<dev.thecodewarrior.bitfont.data.Bitfont>("font")
val Attribute.Companion.font get() = fontAttr
