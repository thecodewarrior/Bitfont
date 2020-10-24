package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.utils.Attribute

public open class AttributedGlyph(
    public val codepoint: Int,
    public val glyph: Glyph,
    public val source: AttributedString,
    public val codepointIndex: Int,
    public val characterIndex: Int
) {
    private var attributeOverrides: MutableMap<Attribute<*>, Any?>? = null

    /**
     * Get the value of [attr]
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(attr: Attribute<T>): T? {
        val attributeOverrides = attributeOverrides
        if(attributeOverrides != null && attr in attributeOverrides)
            return attributeOverrides[attr] as T?
        return source[attr, characterIndex]
    }

    /**
     * Set the attribute override for [attr]
     */
    public operator fun <T> set(attr: Attribute<T>, value: T?) {
        attributeOverrides = (attributeOverrides ?: mutableMapOf()).also {
            it[attr] = value
        }
    }

    /**
     * Remove the attribute override for [attr]
     */
    public fun <T> remove(attr: Attribute<T>) {
        attributeOverrides?.remove(attr)
    }
}
