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
internal fun jString.getString(env: CPointer<JNIEnvVar>): String? {
    val chars = getStringUTFChars(env)
    return chars?.toKStringFromUtf8()
}

@OptIn(ExperimentalForeignApi::class)
expect fun CPointer<JNIEnvVar>.newIntArray(size: Int): jIntArray?

@OptIn(ExperimentalForeignApi::class)
expect fun CPointer<JNIEnvVar>.fill(target: jIntArray, value: IntArray): jIntArray?
