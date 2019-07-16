package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.Attribute
import dev.thecodewarrior.bitfont.utils.AttributeMap
import dev.thecodewarrior.bitfont.utils.ExperimentalBitfont
import dev.thecodewarrior.bitfont.utils.RangeMap

/**
 * This will be expanded when editing becomes a thing
 */
@ExperimentalBitfont
open class TextStorage : MutableAttributedString {
    constructor(plaintext: String): super(plaintext)
    constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    constructor(plaintext: String, attributes: Map<Attribute<*>, RangeMap<Int, Any>>): super(plaintext, attributes)
    constructor(other: dev.thecodewarrior.bitfont.typesetting.AttributedString): super(other)
}