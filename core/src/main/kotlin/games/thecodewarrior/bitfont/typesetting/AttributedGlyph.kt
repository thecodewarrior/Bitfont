package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Glyph
import games.thecodewarrior.bitfont.utils.Attribute

class AttributedGlyph(
    val codepoint: Int,
    val glyph: Glyph,
    val source: AttributedString,
    val codepointIndex: Int,
    val characterIndex: Int
) {
    private var attributeOverrides: MutableMap<Attribute<*>, Any?>? = null

    /**
     * Get the value of [attr]
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(attr: Attribute<T>): T? {
        val attributeOverrides = attributeOverrides
        if(attributeOverrides != null && attr in attributeOverrides)
            return attributeOverrides[attr] as T?
        return source[attr, characterIndex]
    }

    /**
     * Set the attribute override for [attr]
     */
    operator fun <T> set(attr: Attribute<T>, value: T?) {
        attributeOverrides = (attributeOverrides ?: mutableMapOf()).also {
            it[attr] = value
        }
    }

    /**
     * Remove the attribute override for [attr]
     */
    fun <T> remove(attr: Attribute<T>) {
        attributeOverrides?.remove(attr)
    }
}
