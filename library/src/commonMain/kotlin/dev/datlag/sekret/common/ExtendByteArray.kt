package dev.datlag.sekret.common

/**
 * Writes a long split into 8 bytes.
 * @param [offset] start index
 * @param [value] the value to insert
 * Thanks to manu0466
 */
internal fun ByteArray.putLong(offset: Int, value: Long) {
    for (i in 7 downTo 0) {
        val temp = (value ushr (i * 8)).toUByte()
        this[offset + 7 - i] = temp.toByte()
    }
}

/**
 * Converts the given byte array into an int array via big-endian conversion (4 bytes become 1 int).
 * @throws IllegalArgumentException if the byte array size is not a multiple of 4.
 */
internal fun ByteArray.toIntArray(): IntArray {
    if (this.size % 4 != 0) {
        throw IllegalArgumentException("Byte array length must be a multiple of 4")
    }

    val array = IntArray(this.size / 4)
    for (i in array.indices) {
        val integer = arrayOf(this[i* 4], this[i* 4 + 1], this[i* 4 + 2], this[i* 4 + 3])
        array[i] = integer.toInt()
    }
    return array
}

/**
 * Copies an array from the specified source array, beginning at the
 * specified position, to the specified position of the destination array.
 */
internal fun ByteArray.copy(srcPos: Int, dest: ByteArray, destPos: Int, length: Int) {
    this.copyInto(dest, destPos, srcPos, srcPos + length)
}

/**
 * Converts the first 4 bytes into their integer representation following the big-endian conversion.
 * @throws NumberFormatException if the array size is less than 4
 */
internal fun Array<Byte>.toInt(): Int {
    if (this.size < 4) throw NumberFormatException("The array size is less than 4")
    return (this[0].toUByte().toInt() shl 24) + (this[1].toUByte().toInt() shl 16) + (this[2].toUByte().toInt() shl 8) + (this[3].toUByte().toInt() shl 0)
}