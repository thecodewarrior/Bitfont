package games.thecodewarrior.bitfont.stream

import java.io.Closeable
import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import kotlin.math.max

class WriteStream(private val outputStream: OutputStream, val version: UInt): Closeable {
    private val stream = DataOutputStream(outputStream)

    init {
        writeUInt(version)
    }

    fun writeUByte(value: UByte) = self { stream.writeByte(value.toByte().toInt()) }
    fun writeUShort(value: UShort) = self { stream.writeShort(value.toShort().toInt()) }
    fun writeUInt(value: UInt) = self { stream.writeInt(value.toInt()) }
    fun writeULong(value: ULong) = self { stream.writeLong(value.toLong()) }

    fun writeBoolean(value: Boolean) = self { stream.writeBoolean(value) }
    fun writeByte(value: Byte) = self { stream.writeByte(value.toInt()) }
    fun writeShort(value: Short) = self { stream.writeShort(value.toInt()) }
    fun writeInt(value: Int) = self { stream.writeInt(value) }
    fun writeLong(value: Long) = self { stream.writeLong(value) }

    fun writeFloat(value: Float) = self { stream.writeFloat(value) }
    fun writeDouble(value: Double) = self { stream.writeDouble(value) }

    fun writeString(value: String, length: IndexSize = IndexSize.INT) = self { writeByteArray(value.toByteArray(StandardCharsets.UTF_8), length) }

    inline fun <reified T: Any> writeObject(value: T) = self { findWriter(T::class.java)(value, this) }
    inline fun <reified T: Any> writeNullableObject(value: T?) = self {
        writeBoolean(value != null)
        if(value != null)
            findWriter(T::class.java)(value, this)
    }

    fun writeUBytes(values: UByteArray) = self { values.forEach { value -> writeUByte(value) } }
    fun writeUShorts(values: UShortArray) = self { values.forEach { value -> writeUShort(value) } }
    fun writeUInts(values: UIntArray) = self { values.forEach { value -> writeUInt(value) } }
    fun writeULongs(values: ULongArray) = self { values.forEach { value -> writeULong(value) } }

    fun writeBooleans(values: BooleanArray) = self { values.forEach { value -> writeBoolean(value) } }
    fun writeBytes(values: ByteArray) = self { values.forEach { value -> writeByte(value) } }
    fun writeShorts(values: ShortArray) = self { values.forEach { value -> writeShort(value) } }
    fun writeInts(values: IntArray) = self { values.forEach { value -> writeInt(value) } }
    fun writeLongs(values: LongArray) = self { values.forEach { value -> writeLong(value) } }

    fun writeFloats(values: FloatArray) = self { values.forEach { value -> writeFloat(value) } }
    fun writeDoubles(values: DoubleArray) = self { values.forEach { value -> writeDouble(value) } }

    fun writeStrings(values: Array<String>, length: IndexSize = IndexSize.INT) = self { values.forEach { value -> writeString(value, length) } }

    inline fun <reified T: Any> writeObjects(values: Array<T>) = self { values.forEach { value -> writeObject(value) } }
    inline fun <reified T: Any> writeNullableObjects(values: Array<T?>) = self { values.forEach { value -> writeNullableObject(value) } }

    fun writeUByteArray(values: UByteArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUByte(value) } }
    fun writeUShortArray(values: UShortArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUShort(value) } }
    fun writeUIntArray(values: UIntArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUInt(value) } }
    fun writeULongArray(values: ULongArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeULong(value) } }

    fun writeBooleanArray(values: BooleanArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeBoolean(value) } }
    fun writeByteArray(values: ByteArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeByte(value) } }
    fun writeShortArray(values: ShortArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeShort(value) } }
    fun writeIntArray(values: IntArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeInt(value) } }
    fun writeLongArray(values: LongArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeLong(value) } }

    fun writeFloatArray(values: FloatArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeFloat(value) } }
    fun writeDoubleArray(values: DoubleArray, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeDouble(value) } }

    fun writeStringArray(values: Array<String>, index: IndexSize = IndexSize.INT, length: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeString(value, length) } }

    inline fun <reified T: Any> writeObjectArray(values: Array<T>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeObject(value) } }
    inline fun <reified T: Any> writeNullableObjectArray(values: Array<T?>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeNullableObject(value) } }

    fun writeUByteList(values: List<UByte>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUByte(value) } }
    fun writeUShortList(values: List<UShort>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUShort(value) } }
    fun writeUIntList(values: List<UInt>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeUInt(value) } }
    fun writeULongList(values: List<ULong>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeULong(value) } }

    fun writeBooleanList(values: List<Boolean>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeBoolean(value) } }
    fun writeByteList(values: List<Byte>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeByte(value) } }
    fun writeShortList(values: List<Short>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeShort(value) } }
    fun writeIntList(values: List<Int>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeInt(value) } }
    fun writeLongList(values: List<Long>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeLong(value) } }

    fun writeFloatList(values: List<Float>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeFloat(value) } }
    fun writeDoubleList(values: List<Double>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeDouble(value) } }

    fun writeStringList(values: List<String>, index: IndexSize = IndexSize.INT, length: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeString(value, length) } }

    inline fun <reified T: Any> writeObjectList(values: List<T>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeObject(value) } }
    inline fun <reified T: Any> writeNullableObjectList(values: List<T?>, index: IndexSize = IndexSize.INT) = self { writeCount(index, values.size); values.forEach { value -> writeNullableObject(value) } }

    /**
     * Writes a positive number of the specified size. The written value is clamped to be nonnegative.
     */
    fun writeCount(index: IndexSize, count: Int) = when(index) {
        IndexSize.BYTE -> writeUByte(count.toUByte())
        IndexSize.SHORT -> writeUShort(count.toUShort())
        IndexSize.INT -> writeInt(max(0, count))
    }

    override fun close() {
        stream.close()
    }

    inline fun self(callback: () -> Unit): WriteStream {
        callback()
        return this
    }

    companion object {
        private val writers = mutableMapOf<Class<*>, (Any, WriteStream) -> Unit>()

        fun <T: Any> findWriter(clazz: Class<T>): (T, WriteStream) -> Unit {
            return writers.getOrPut(clazz) {
                val m = clazz.getMethod("write", WriteStream::class.java)
                        ?: throw ReadException("Couldn't find `write` method for ${clazz.name} with WriteStream argument")
                m.isAccessible = true
                return@getOrPut { value, stream -> m.invoke(value, stream) }
            }
        }

        fun <T: Any> addWriter(clazz: Class<T>, writer: (T, WriteStream) -> Unit) {
            @Suppress("UNCHECKED_CAST")
            writers[clazz] = writer as (Any, WriteStream) -> Unit
        }
        inline fun <reified T: Any> addWriter(noinline writer: (T, WriteStream) -> Unit) {
            addWriter(T::class.java, writer)
        }
    }
}
