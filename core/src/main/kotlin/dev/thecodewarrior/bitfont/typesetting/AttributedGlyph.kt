package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.data.Glyph
import dev.thecodewarrior.bitfont.utils.Attribute

open class AttributedGlyph(
    val codepoint: Int,
    val glyph: dev.thecodewarrior.bitfont.data.Glyph,
    val source: dev.thecodewarrior.bitfont.typesetting.AttributedString,
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
