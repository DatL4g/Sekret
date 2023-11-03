package dev.datlag.sekret

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlin.experimental.xor

@OptIn(ExperimentalForeignApi::class)
fun getOriginalValue(
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

    return out.toKString()
}