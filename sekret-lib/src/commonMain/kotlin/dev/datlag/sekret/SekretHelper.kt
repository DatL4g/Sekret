package dev.datlag.sekret

import kotlin.experimental.xor

data object SekretHelper {

    fun getNativeValue(
        secret: IntArray,
        key: String
    ): String {
        val obfuscatorBytes = SHA256.digest(key.encodeToByteArray())
        val hex = obfuscatorBytes.fold("") { str, it ->
            val value = it.toUByte().toString(16)
            str + if (value.length == 1) {
                "0$value"
            } else {
                value
            }
        }
        val hexBytes = hex.encodeToByteArray()

        val out = secret.mapIndexed { index, it ->
            val obfuscatorByte = hexBytes[index % hexBytes.size]
            it.toByte().xor(obfuscatorByte)
        }.toByteArray()

        return out.decodeToString()
    }

    data object Random {
        fun seed(x: Long): Long {
            val z = (x xor (x ushr 33)) * 0x62a9d9ed799705f5L
            return ((z xor (z ushr 28)) * -0x34db2f5a3773ca4dL) ushr 32
        }

        fun next(state: Long): Long {
            var s0 = (state and 0xffff).toShort()
            var s1 = ((state ushr 16) and 0xffff).toShort()
            var next = s0
            next = (next + s1).toShort()
            next = rotL(next, 9)
            next = (next + s0).toShort()

            s1 = (s1.toInt() xor s0.toInt()).toShort()
            s0 = rotL(s0, 13)
            s0 = (s0.toInt() xor s1.toInt()).toShort()
            s0 = (s0.toInt() xor (s1.toInt() shl 5)).toShort()
            s1 = rotL(s1, 10)

            var result = next.toLong()
            result = result shl 16
            result = result or s1.toLong()
            result = result shl 16
            result = result or s0.toLong()
            return result
        }

        private fun rotL(x: Short, k: Int): Short {
            return ((x.toInt() shl k) or (x.toInt() ushr (32 - k))).toShort()
        }
    }

    data object Deobfuscator {

        const val MAX_CHUNK_LENGTH: Int = 0x1fff

        fun getString(id: Long, chunks: Iterable<String>): String {
            var state = Random.seed(id and 0xffffffffL)
            state = Random.next(state)

            val low = (state ushr 32) and 0xffff
            state = Random.next(state)

            val high = (state ushr 16) and 0xffff0000
            val index = ((id ushr 32) xor low xor high).toInt()
            state = getCharAt(index, chunks, state)

            val length = ((state ushr 32) and 0xffffL).toInt()
            val chars = CharArray(length)

            for (i in 0..<length) {
                state = getCharAt(index + i + 1, chunks, state)
                chars[i] = Char(((state ushr 32) and 0xffffL).toUShort())
            }

            return chars.concatToString()
        }

        private fun getCharAt(charIndex: Int, chunks: Iterable<String>, state: Long): Long {
            val nextState = Random.next(state)
            val chunk = chunks.toList()[charIndex / MAX_CHUNK_LENGTH]

            return nextState xor (chunk[charIndex % MAX_CHUNK_LENGTH].code.toLong() shl 32)
        }
    }
}