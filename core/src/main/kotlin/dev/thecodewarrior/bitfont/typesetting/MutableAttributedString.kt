package dev.thecodewarrior.bitfont.typesetting

import dev.thecodewarrior.bitfont.utils.RangeMap
import dev.thecodewarrior.bitfont.utils.TreeRangeMap
import java.util.Collections
import java.util.IdentityHashMap

public class MutableAttributedString: AttributedString {
    private val buffer: StringBuffer = StringBuffer(super.plaintext)
    private val markers: MutableSet<Marker> = Collections.newSetFromMap(IdentityHashMap())

    override val attributes: MutableMap<TextAttribute<*>, RangeMap<Int, Any>> = super.attributes.toMutableMap()
    override val plaintext: String get() = buffer.toString()

    public constructor(plaintext: String): super(plaintext)
    public constructor(plaintext: String, attributes: AttributeMap): super(plaintext, attributes)
    public constructor(other: AttributedString): super(other)

    public fun registerMarker(marker: Marker) {
        markers.add(marker)
    }

    public fun removeMarker(marker: Marker) {
        markers.remove(marker)
    }

    private fun moveMarkers(start: Int, offset: Int) {
        markers.forEach {
            if(it.position >= start)
                it.position += offset
        }
    }

//region String manipulation

    public fun append(string: String, attributes: AttributeMap): MutableAttributedString {
        version++
        val start = buffer.length
        buffer.append(string)
        setAttributes(start, buffer.length, attributes)
        moveMarkers(start, string.length)
        return this
    }

    public fun append(string: AttributedString): MutableAttributedString {
        version++
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
        version++
        buffer.insert(pos, string)
        this.attributes.forEach { key, value ->
            value.shift(pos, null) { it + string.length }
        }
        setAttributes(pos, pos+string.length, attributes)
        moveMarkers(pos, string.length)
        return this
    }

    public fun insert(pos: Int, string: AttributedString): MutableAttributedString {
        version++
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
        version++
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

    public fun replace(start: Int, end: Int, string: String, attributes: AttributeMap): MutableAttributedString {
        version++
        val delta = string.length - (end - start)
        this.attributes.forEach { (_, ranges) ->
            ranges.clear(start, end)
            ranges.shift(end, null) { it + delta }
        }
        moveMarkers(end, delta)

        buffer.replace(start, end, string)
        setAttributes(start, end, attributes)
        return this
    }

    public fun replace(start: Int, end: Int, string: AttributedString): MutableAttributedString {
        version++
        val delta = string.length - (end - start)
        this.attributes.forEach { (_, ranges) ->
            ranges.clear(start, end)
            ranges.shift(end, null) { it + delta }
        }
        moveMarkers(end, delta)

        buffer.replace(start, end, string.plaintext)
        applyAttributes(string.getAllAttributes(), start)
        return this
    }

    public fun replace(start: Int, end: Int, string: String, attributes: Map<TextAttribute<*>, Any>): MutableAttributedString
        = this.replace(start, end, string, AttributeMap(attributes))
    public fun replace(start: Int, end: Int, string: String, vararg attributes: Pair<TextAttribute<*>, Any>): MutableAttributedString
        = this.replace(start, end, string, AttributeMap(*attributes))

//endregion

//region Attribute manipulation

    public fun <T> setAttribute(start: Int, end: Int, attribute: TextAttribute<T>, value: T): MutableAttributedString {
        version++
        attributes.getOrPut(attribute) { TreeRangeMap() }[start, end] = value
        return this
    }

    public fun setAttributes(start: Int, end: Int, attributes: AttributeMap): MutableAttributedString {
        version++
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
        version++
        val ourMap = attributes.getOrPut(attribute) { TreeRangeMap() }
        values.entries.forEach { (start, end, value) ->
            ourMap[start + offset, end + offset] = value
        }
        return this
    }

    public fun applyAttributes(attributes: Map<TextAttribute<*>, RangeMap<Int, Any>>, offset: Int): MutableAttributedString {
        version++
        attributes.forEach { (attr, values) ->
            @Suppress("UNCHECKED_CAST")
            applyAttributes(attr as TextAttribute<Any>, values, offset)
        }
        return this
    }

    public fun removeAttributes(start: Int, end: Int, vararg attributes: TextAttribute<*>): MutableAttributedString {
        version++
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
     */
    public interface Marker {
        public var position: Int
    }
}

