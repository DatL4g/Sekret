package dev.datlag.sekret

import kotlinx.cinterop.*
import kotlin.experimental.xor

@OptIn(ExperimentalForeignApi::class)
expect fun CPointer<JNIEnvVar>.newString(chars: CPointer<jCharVar>, length: Int): jString?

@OptIn(ExperimentalForeignApi::class)
internal fun String.toJString(env: CPointer<JNIEnvVar>): jString? = memScoped {
    env.newString(wcstr.ptr, length)
}

@OptIn(ExperimentalForeignApi::class)
expect fun jString.getStringUTFChars(env: CPointer<JNIEnvVar>): CPointer<ByteVar>?

@OptIn(ExperimentalForeignApi::class)
internal fun jString.getString(env: CPointer<JNIEnvVar>): String {
    val chars = getStringUTFChars(env)
    return chars?.toKStringFromUtf8() ?: error("Unable to create String from the given jString")
}

@OptIn(ExperimentalForeignApi::class)
public fun getOriginalValue(
    secret: IntArray,
    key: jString,
    env: CPointer<JNIEnvVar>
): jString? {
    val obfuscator = key.getString(env)
    return getOriginalValue(secret, obfuscator).toJString(env)
}