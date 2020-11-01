package dev.thecodewarrior.bitfont.typesetting

public open class TypesetGlyph(
    public var posX: Int, public var posY: Int,
    public val codepoint: Int,
    public val textObject: TextObject,
    public val source: AttributedString,
    public val codepointIndex: Int,
    public val characterIndex: Int
): TextObject by textObject {
    public val afterX: Int
        get() = posX + textObject.advance

    private var attributeOverrides: MutableMap<TextAttribute<*>, Any?>? = null

    /**
     * Get the value of [attr]
     */
    @Suppress("UNCHECKED_CAST")
    public operator fun <T> get(attr: TextAttribute<T>): T? {
        val attributeOverrides = attributeOverrides
        if(attributeOverrides != null && attr in attributeOverrides)
            return attributeOverrides[attr] as T?
        return source[attr, characterIndex]
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
