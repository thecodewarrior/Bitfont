package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.utils.AttributeMap
import games.thecodewarrior.bitfont.utils.ExperimentalBitfont
import games.thecodewarrior.bitfont.utils.RangeMap

/**
 * This will be expanded when editing becomes a thing
 */
@ExperimentalBitfont
open class TextStorage : MutableAttributedString {
    constructor(plaintext: String): super(plaintext)
    constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    constructor(plaintext: String, attributes: Map<Attribute<*>, RangeMap<Int, Any>>): super(plaintext, attributes)
    constructor(other: AttributedString): super(other)
}