package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.ExperimentalBitfont
import dev.thecodewarrior.bitfont.utils.RangeMap
import dev.thecodewarrior.bitfont.utils.TreeRangeMap

/**
 * A private interface purely to simplify method organization
 */
@ExperimentalBitfont
private interface IMutableAttributedString {
    private fun getThis(): MutableAttributedString = this as MutableAttributedString

    //region append
    /*    //----------------- Implementations -----------------\\    */
    fun append(string: String, attributes: AttributeMap): MutableAttributedString
    @ExperimentalBitfont
    fun append(string: AttributedString): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun append(string: String, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(attributes))
    @JvmDefault
    fun append(string: String, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(*attributes))
    //endregion

    //region setAttribute(s)
    /*    //----------------- Implementations -----------------\\    */
    fun <T> setAttribute(start: Int, end: Int, attribute: TextAttribute<T>, value: T): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun setAttributes(start: Int, end: Int, attributes: AttributeMap): MutableAttributedString {
        attributes.map.forEach { (attr, value) ->
            @Suppress("UNCHECKED_CAST")
            setAttribute(start, end, attr as TextAttribute<Any>, value)
        }
        return getThis()
    }
    @JvmDefault
    fun setAttributes(start: Int, end: Int, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(attributes))
    @JvmDefault
    fun setAttributes(start: Int, end: Int, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(*attributes))
    //endregion

    //region insert
    /*    //----------------- Implementations -----------------\\    */
    fun insert(pos: Int, string: String, attributes: AttributeMap): MutableAttributedString
    @ExperimentalBitfont
    fun insert(pos: Int, string: AttributedString): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun insert(pos: Int, string: String, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(attributes))
    @JvmDefault
    fun insert(pos: Int, string: String, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(*attributes))
    //endregion

    //region applyAttributes
    /*    //----------------- Implementations -----------------\\    */
    fun <T> applyAttributes(attribute: TextAttribute<T>, values: RangeMap<Int, T>, offset: Int): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun applyAttributes(attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>, offset: Int): MutableAttributedString {
        attributes.forEach { (attr, values) ->
            @Suppress("UNCHECKED_CAST")
            applyAttributes(attr as TextAttribute<Any>, values, offset)
        }
        return getThis()
    }
    //endregion

    //region removeAttributes
    /*    //----------------- Implementations -----------------\\    */
    fun removeAttributes(start: Int, end: Int, vararg attributes: TextAttribute<*>): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    //endregion

    //region removeAttributes
    /*    //----------------- Implementations -----------------\\    */
    fun delete(start: Int, end: Int): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    //endregion

    /*
    //region append
    /*    //----------------- Implementations -----------------\\    */
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun xxx(): MutableAttributedString
        = this.xxx()
    //endregion
    */
}

@ExperimentalBitfont
open class MutableAttributedString: AttributedString, IMutableAttributedString {
    private val buffer: StringBuffer = StringBuffer(super.plaintext)

    override val attributes: MutableMap<TextAttribute<*>, RangeMap<Int, Any>> = super.attributes.toMutableMap()
    override val plaintext: String get() = buffer.toString()

    constructor(plaintext: String): super(plaintext)
    constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    internal constructor(plaintext: String, attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>): super(plaintext, attributes)
    constructor(other: AttributedString): super(other)

    override fun append(string: String, attributes: AttributeMap): MutableAttributedString {
        val start = buffer.length
        buffer.append(string)
        setAttributes(start, buffer.length, attributes)
        return this
    }

    override fun append(string: AttributedString): MutableAttributedString {
        val start = buffer.length
        buffer.append(string.plaintext)
        applyAttributes(string.getAllAttributes(), start)
        return this
    }

    override fun <T> setAttribute(start: Int, end: Int, attribute: TextAttribute<T>, value: T): MutableAttributedString {
        attributes.getOrPut(attribute) { TreeRangeMap() }[start, end] = value
        return this
    }

    override fun <T> applyAttributes(attribute: TextAttribute<T>, values: RangeMap<Int, T>, offset: Int): MutableAttributedString {
        val ourMap = attributes.getOrPut(attribute) { TreeRangeMap() }
        values.entries.forEach { (start, end, value) ->
            ourMap[start + offset, end + offset] = value
        }
        return this
    }

    override fun insert(pos: Int, string: String, attributes: AttributeMap): MutableAttributedString {
        buffer.insert(pos, string)
        this.attributes.forEach { key, value ->
            value.shift(pos, null) { it + string.length }
        }
        setAttributes(pos, pos+string.length, attributes)
        return this
    }

    override fun insert(pos: Int, string: AttributedString): MutableAttributedString {
        buffer.insert(pos, string.plaintext)
        this.attributes.forEach { key, value ->
            value.shift(pos, null) { it + string.length }
        }
        applyAttributes(string.getAllAttributes(), pos)
        return this
    }

    override fun removeAttributes(start: Int, end: Int, vararg attributes: TextAttribute<*>): MutableAttributedString {
        if(attributes.isEmpty()) {
            this.attributes.forEach { key, value ->
                value.clear(start, end)
            }
        } else {
            attributes.forEach { attr ->
                this.attributes[attr]?.clear(start, end)
            }
        }
        return this
    }

    override fun delete(start: Int, end: Int): MutableAttributedString {
        buffer.delete(start, end)
        this.attributes.forEach { attr, ranges ->
            val len = end - start
            ranges.clear(start, end)
            ranges.shift(end, null) { it - len }
        }
        return this
    }

    override fun substring(start: Int, end: Int): AttributedString {
        return AttributedString(
            plaintext.substring(start, end),
            attributes.mapValues {
                val map = it.value.copy { it - start }
                map.clear(Int.MIN_VALUE, 0)
                map.clear(end - start, Int.MAX_VALUE)
                map
            }
        )
    }
}

