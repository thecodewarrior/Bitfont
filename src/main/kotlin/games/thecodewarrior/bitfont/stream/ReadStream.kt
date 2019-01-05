package games.thecodewarrior.bitfont.stream

import java.io.Closeable
import java.io.DataInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

class ReadStream(private val inputStream: InputStream): Closeable {
    private val stream = DataInputStream(inputStream)

    val version = readUInt()

    fun readUByte() = stream.readUnsignedByte().toUByte()
    fun readUShort() = stream.readUnsignedShort().toUShort()
    fun readUInt() = stream.readInt().toUInt()
    fun readULong() = stream.readLong().toULong()

    fun readBoolean() = stream.readBoolean()
    fun readByte() = stream.readByte()
    fun readShort() = stream.readShort()
    fun readInt() = stream.readInt()
    fun readLong() = stream.readLong()

    fun readDouble() = stream.readDouble()
    fun readFloat() = stream.readFloat()

    fun readString(length: IndexSize = IndexSize.INT) = String(readByteArray(length), StandardCharsets.UTF_8)

    inline fun <reified T: Any> readObject() = findConstructor(T::class)(this)
    inline fun <reified T: Any> readNullableObject() = if(readBoolean()) findConstructor(T::class)(this) else null

    fun readUBytes(count: Int) = UByteArray(count) { readUByte() }
    fun readUShorts(count: Int) = UShortArray(count) { readUShort() }
    fun readUInts(count: Int) = UIntArray(count) { readUInt() }
    fun readULongs(count: Int) = ULongArray(count) { readULong() }

    fun readBooleans(count: Int) = BooleanArray(count) { stream.readBoolean() }
    fun readBytes(count: Int) = ByteArray(count) { readByte() }
    fun readShorts(count: Int) = ShortArray(count) { readShort() }
    fun readInts(count: Int) = IntArray(count) { readInt() }
    fun readLongs(count: Int) = LongArray(count) { readLong() }

    fun readDoubles(count: Int) = DoubleArray(count) { readDouble() }
    fun readFloats(count: Int) = FloatArray(count) { readFloat() }

    fun readStrings(count: Int, length: IndexSize = IndexSize.INT) = Array(count) { readString(length) }

    inline fun <reified T: Any> readObjects(count: Int) = Array(count) { readObject<T>() }
    inline fun <reified T: Any> readNullableObjects(count: Int) = Array(count) { readNullableObject<T>() }

    fun readUByteArray(index: IndexSize = IndexSize.INT) = UByteArray(readCount(index)) { readUByte() }
    fun readUShortArray(index: IndexSize = IndexSize.INT) = UShortArray(readCount(index)) { readUShort() }
    fun readUIntArray(index: IndexSize = IndexSize.INT) = UIntArray(readCount(index)) { readUInt() }
    fun readULongArray(index: IndexSize = IndexSize.INT) = ULongArray(readCount(index)) { readULong() }

    fun readBooleanArray(index: IndexSize = IndexSize.INT) = BooleanArray(readCount(index)) { readBoolean() }
    fun readByteArray(index: IndexSize = IndexSize.INT) = ByteArray(readCount(index)) { readByte() }
    fun readShortArray(index: IndexSize = IndexSize.INT) = ShortArray(readCount(index)) { readShort() }
    fun readIntArray(index: IndexSize = IndexSize.INT) = IntArray(readCount(index)) { readInt() }
    fun readLongArray(index: IndexSize = IndexSize.INT) = LongArray(readCount(index)) { readLong() }

    fun readDoubleArray(index: IndexSize = IndexSize.INT) = DoubleArray(readCount(index)) { readDouble() }
    fun readFloatArray(index: IndexSize = IndexSize.INT) = FloatArray(readCount(index)) { readFloat() }

    fun readStringArray(index: IndexSize = IndexSize.INT, length: IndexSize = IndexSize.INT) = Array(readCount(index)) { readString(length) }

    inline fun <reified T: Any> readObjectArray(index: IndexSize = IndexSize.INT) = Array(readCount(index)) { readObject<T>() }
    inline fun <reified T: Any> readNullableObjectArray(index: IndexSize = IndexSize.INT) = Array(readCount(index)) { readNullableObject<T>() }

    fun readUByteList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readUByte() }
    fun readUShortList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readUShort() }
    fun readUIntList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readUInt() }
    fun readULongList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readULong() }

    fun readBooleanList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readBoolean() }
    fun readByteList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readByte() }
    fun readShortList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readShort() }
    fun readIntList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readInt() }
    fun readLongList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readLong() }

    fun readDoubleList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readDouble() }
    fun readFloatList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readFloat() }

    fun readStringList(index: IndexSize = IndexSize.INT, length: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readString(length) }

    inline fun <reified T: Any> readObjectList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readObject<T>() }
    inline fun <reified T: Any> readNullableObjectList(index: IndexSize = IndexSize.INT) = MutableList(readCount(index)) { readNullableObject<T>() }

    /**
     * Reads a positive number of the specified size. The result is guaranteed to be nonnegitive.
     */
    fun readCount(index: IndexSize) = when(index) {
        IndexSize.BYTE -> readUByte().toInt()
        IndexSize.SHORT -> readUShort().toInt()
        IndexSize.INT -> max(0, readInt())
    }

    override fun close() {
        stream.close()
    }

    companion object {
        private val constructors = mutableMapOf<KClass<*>, (ReadStream) -> Any>()

        fun <T: Any> findConstructor(clazz: KClass<T>): (ReadStream) -> T {
            val constructor = constructors.getOrPut(clazz) {
                val c = clazz.java.getConstructor(ReadStream::class.java)
                    ?: throw ReadException("Couldn't find constructor for ${clazz.java.name} with ReadStream argument")
                c.isAccessible = true
                return@getOrPut { c.newInstance(it) }
            }
            @Suppress("UNCHECKED_CAST")
            return constructor as (ReadStream) -> T
        }

        fun <T: Any> addConstructor(clazz: KClass<T>, constructor: (ReadStream) -> T) {
            constructors[clazz] = constructor
        }
        fun <T: Any> addConstructor(clazz: Class<T>, constructor: (ReadStream) -> T) {
            addConstructor(clazz.kotlin, constructor)
        }
        inline fun <reified T: Any> addConstructor(noinline constructor: (ReadStream) -> T) {
            addConstructor(T::class, constructor)
        }
    }
}
