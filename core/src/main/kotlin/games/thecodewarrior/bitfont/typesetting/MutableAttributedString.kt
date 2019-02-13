package games.thecodewarrior.bitfont.typesetting

import games.thecodewarrior.bitfont.data.Bitfont
import games.thecodewarrior.bitfont.utils.Attribute
import games.thecodewarrior.bitfont.utils.AttributeMap
import games.thecodewarrior.bitfont.utils.RangeMap
import games.thecodewarrior.bitfont.utils.TreeRangeMap
import java.awt.Color

/**
 * A private interface purely to simplify method organization
 */
private interface IMutableAttributedString {
    private fun getThis(): MutableAttributedString = this as MutableAttributedString

    //region append
    /*    //----------------- Implementations -----------------\\    */
    fun append(string: String, attributes: AttributeMap): MutableAttributedString
    fun append(string: AttributedString): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun append(string: String, attributes: Map<Attribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(attributes))
    @JvmDefault
    fun append(string: String, vararg attributes: Pair<Attribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(*attributes))
    //endregion

    //region setAttribute(s)
    /*    //----------------- Implementations -----------------\\    */
    fun <T> setAttribute(start: Int, end: Int, attribute: Attribute<T>, value: T): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun setAttributes(start: Int, end: Int, attributes: AttributeMap): MutableAttributedString {
        attributes.map.forEach { (attr, value) ->
            @Suppress("UNCHECKED_CAST")
            setAttribute(start, end, attr as Attribute<Any>, value)
        }
        return getThis()
    }
    @JvmDefault
    fun setAttributes(start: Int, end: Int, attributes: Map<Attribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(attributes))
    @JvmDefault
    fun setAttributes(start: Int, end: Int, vararg attributes: Pair<Attribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(*attributes))
    //endregion

    //region insert
    /*    //----------------- Implementations -----------------\\    */
    fun insert(pos: Int, string: String, attributes: AttributeMap): MutableAttributedString
    fun insert(pos: Int, string: AttributedString): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun insert(pos: Int, string: String, attributes: Map<Attribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(attributes))
    @JvmDefault
    fun insert(pos: Int, string: String, vararg attributes: Pair<Attribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(*attributes))
    //endregion

    //region applyAttributes
    /*    //----------------- Implementations -----------------\\    */
    fun <T> applyAttributes(attribute: Attribute<T>, values: RangeMap<Int, T>, offset: Int): MutableAttributedString
    /*    //-------------------- Overloads --------------------\\    */
    @JvmDefault
    fun applyAttributes(attributes: Map<Attribute<*>, RangeMap<Int, Any>>, offset: Int): MutableAttributedString {
        attributes.forEach { (attr, values) ->
            @Suppress("UNCHECKED_CAST")
            applyAttributes(attr as Attribute<Any>, values, offset)
        }
        return getThis()
    }
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

class MutableAttributedString: AttributedString, IMutableAttributedString {
    private val buffer: StringBuffer = StringBuffer(super.plaintext)

    override val attributes: MutableMap<Attribute<*>, RangeMap<Int, Any>> = super.attributes.toMutableMap()
    override val plaintext: String get() = buffer.toString()

    constructor(plaintext: String): super(plaintext)
    constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    internal constructor(plaintext: String, attributes: Map<Attribute<*>, RangeMap<Int, Any>>): super(plaintext, attributes)
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

    override fun <T> setAttribute(start: Int, end: Int, attribute: Attribute<T>, value: T): MutableAttributedString {
        attributes.getOrPut(attribute) { TreeRangeMap() }[start, end] = value
        return this
    }

    override fun <T> applyAttributes(attribute: Attribute<T>, values: RangeMap<Int, T>, offset: Int): MutableAttributedString {
        val ourMap = attributes.getOrPut(attribute) { TreeRangeMap() }
        values.entries.forEach { (start, end, value) ->
            ourMap[start + offset, end + offset] = value
        }
        return this
    }

    override fun insert(pos: Int, string: String, attributes: AttributeMap): MutableAttributedString {
        buffer.insert(pos, string)
        this.attributes.forEach { key, value ->
            value.shift(pos, Int.MAX_VALUE) { it + string.length }
        }
        setAttributes(pos, pos+string.length, attributes)
        return this
    }

    override fun insert(pos: Int, string: AttributedString): MutableAttributedString {
        buffer.insert(pos, string.plaintext)
        this.attributes.forEach { key, value ->
            value.shift(pos, Int.MAX_VALUE/2) { it + string.plaintext.length }
        }
        applyAttributes(string.getAllAttributes(), pos)
        return this
    }

    override fun substring(start: Int, end: Int): AttributedString {
        return AttributedString(
            plaintext.substring(start, end),
            attributes.mapValues {
                val map = it.value.copy { it - start }
                map.clear(Int.MIN_VALUE, 0)
                map.clear(end-start, Int.MAX_VALUE)
                map
            }
        )
    }
}

