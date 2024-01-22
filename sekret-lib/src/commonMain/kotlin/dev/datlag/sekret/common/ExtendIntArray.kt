package dev.datlag.sekret.common

/**
 * Converts the given int array into a byte array via big-endian conversion
 * (1 int becomes 4 bytes).
 * @return The converted array.
 */
internal fun IntArray.toByteArray(): ByteArray {
    val array = ByteArray(this.size * 4)
    for (i in this.indices) {
        val bytes = this[i].toBytes()
        array[i * 4] = bytes[0]
        array[i * 4 + 1] = bytes[1]
        array[i * 4 + 2] = bytes[2]
        array[i * 4 + 3] = bytes[3]
    }
    return array
}

/**
 * Copies an array from the specified source array, beginning at the
 * specified position, to the specified position of the destination array.
 */
internal fun IntArray.copy(srcPos: Int, dest: IntArray, destPos: Int, length: Int) {
    this.copyInto(dest, destPos, srcPos, srcPos + length)
}