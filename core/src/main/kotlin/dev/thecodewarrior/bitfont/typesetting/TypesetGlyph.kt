package dev.thecodewarrior.bitfont.typesetting

public open class TypesetGlyph(
    /**
     * The line-relative position of this glyph
     */
    public var posX: Int,
    /**
     * The line-relative position of this glyph
     */
    public var posY: Int,
    public val codepoint: Int,
    public val textObject: TextObject,
    public val source: AttributedString,
    /**
     * The index of the codepoint in the source string
     */
    public val index: Int,
): TextObject by textObject {
    /**
     * The X coordinate after this character
     */
    public val afterX: Int
        get() = posX + textObject.advance
    /**
     * The index after this codepoint
     */
    public val afterIndex: Int
        get() = index + Character.charCount(codepoint)

    private var attributeOverrides: MutableMap<TextAttribute<*>, Any?>? = null

    /**
     * Get the value of [attr]
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(attr: TextAttribute<T>): T? {
        val attributeOverrides = attributeOverrides
        if(attributeOverrides != null && attr in attributeOverrides)
            return attributeOverrides[attr] as T?
        return source[attr, index]
    }

    /**
     * Set the attribute override for [attr]
     */
    public operator fun <T> set(attr: TextAttribute<T>, value: T?) {
        attributeOverrides = (attributeOverrides ?: mutableMapOf()).also {
            it[attr] = value
        }
    }

    /**
     * Remove the attribute override for [attr]
     */
    public fun <T> remove(attr: TextAttribute<T>) {
        attributeOverrides?.remove(attr)
    }
}
