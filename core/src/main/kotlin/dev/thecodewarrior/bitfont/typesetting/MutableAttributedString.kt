package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.RangeMap
import dev.thecodewarrior.bitfont.utils.TreeRangeMap

public open class MutableAttributedString: AttributedString {
    protected val buffer: StringBuffer = StringBuffer(super.plaintext)
    protected val markers: MutableSet<Marker> = mutableSetOf()

    override val attributes: MutableMap<TextAttribute<*>, RangeMap<Int, Any>> = super.attributes.toMutableMap()
    override val plaintext: String get() = buffer.toString()

    public constructor(plaintext: String): super(plaintext)
    public constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    internal constructor(plaintext: String, attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>): super(plaintext, attributes)
    public constructor(other: AttributedString): super(other)

    public fun registerMarker(marker: Marker) {
        if(markers.none { it === marker })
            markers.add(marker)
    }

    public fun removeMarker(marker: Marker) {
        markers.removeIf { it === marker }
    }

    private fun moveMarkers(start: Int, offset: Int) {
        markers.forEach {
            if(it.position >= start)
                it.position += offset
        }
    }

//region String manipulation

    public fun append(string: String, attributes: AttributeMap): MutableAttributedString {
        val start = buffer.length
        buffer.append(string)
        setAttributes(start, buffer.length, attributes)
        moveMarkers(start, string.length)
        return this
    }

    public fun append(string: AttributedString): MutableAttributedString {
        val start = buffer.length
        buffer.append(string.plaintext)
        applyAttributes(string.getAllAttributes(), start)
        moveMarkers(start, string.length)
        return this
    }

    public fun append(string: String, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(attributes))
    public fun append(string: String, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.append(string, AttributeMap(*attributes))

    public fun insert(pos: Int, string: String, attributes: AttributeMap): MutableAttributedString {
        buffer.insert(pos, string)
        this.attributes.forEach { key, value ->
            value.shift(pos, null) { it + string.length }
        }
        setAttributes(pos, pos+string.length, attributes)
        moveMarkers(pos, string.length)
        return this
    }

    public fun insert(pos: Int, string: AttributedString): MutableAttributedString {
        buffer.insert(pos, string.plaintext)
        this.attributes.forEach { key, value ->
            value.shift(pos, null) { it + string.length }
        }
        applyAttributes(string.getAllAttributes(), pos)
        moveMarkers(pos, string.length)
        return this
    }

    public fun insert(pos: Int, string: String, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(attributes))
    public fun insert(pos: Int, string: String, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.insert(pos, string, AttributeMap(*attributes))

    public fun delete(start: Int, end: Int): MutableAttributedString {
        buffer.delete(start, end)
        this.attributes.forEach { attr, ranges ->
            val len = end - start
            ranges.clear(start, end)
            ranges.shift(end, null) { it - len }
        }
        markers.forEach {
            if(it.position in start until end)
                it.position = start
        }
        moveMarkers(end, start - end)
        return this
    }

//endregion

//region Attribute manipulation

    public fun <T> setAttribute(start: Int, end: Int, attribute: TextAttribute<T>, value: T): MutableAttributedString {
        attributes.getOrPut(attribute) { TreeRangeMap() }[start, end] = value
        return this
    }

    public fun setAttributes(start: Int, end: Int, attributes: AttributeMap): MutableAttributedString {
        attributes.map.forEach { (attr, value) ->
            @Suppress("UNCHECKED_CAST")
            setAttribute(start, end, attr as TextAttribute<Any>, value)
        }
        return this
    }

    public fun setAttributes(start: Int, end: Int, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(attributes))
    public fun setAttributes(start: Int, end: Int, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.setAttributes(start, end, AttributeMap(*attributes))

    public fun <T> applyAttributes(attribute: TextAttribute<T>, values: RangeMap<Int, T>, offset: Int): MutableAttributedString {
        val ourMap = attributes.getOrPut(attribute) { TreeRangeMap() }
        values.entries.forEach { (start, end, value) ->
            ourMap[start + offset, end + offset] = value
        }
        return this
    }

    public fun applyAttributes(attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>, offset: Int): MutableAttributedString {
        attributes.forEach { (attr, values) ->
            @Suppress("UNCHECKED_CAST")
            applyAttributes(attr as TextAttribute<Any>, values, offset)
        }
        return this
    }

    public fun removeAttributes(start: Int, end: Int, vararg attributes: TextAttribute<*>): MutableAttributedString {
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

//endregion

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

    /**
     * A marker for an index that will move as text is inserted or removed.
     * (using equals()/hashCode() by identity is important)
     */
    public open class Marker(position: Int) {
        public open var position: Int = position
            set(value) {
                if(field != value) {
                    field = value
                    moved()
                }
            }

        public open fun moved() {}
    }
}

