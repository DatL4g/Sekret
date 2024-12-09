package dev.datlag.sekret

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun CPointer<JNIEnvVar>.newString(chars: CPointer<jCharVar>, length: Int): jString? {
    val method = pointed.pointed?.NewString ?: return null
    return method.invoke(this, chars, length)
}

@OptIn(ExperimentalForeignApi::class)
actual fun jString.getStringUTFChars(env: CPointer<JNIEnvVar>): CPointer<ByteVar>? {
    val method = env.pointed.pointed?.GetStringUTFChars ?: return null
    return method.invoke(env, (this as? jstring), null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun CPointer<JNIEnvVar>.newIntArray(size: Int): jIntArray? {
    val method = pointed.pointed?.NewIntArray ?: return null
    return method.invoke(this, size)
}

@OptIn(ExperimentalForeignApi::class)
actual fun CPointer<JNIEnvVar>.fill(target: jIntArray, value: IntArray): jIntArray? {
    val method = pointed.pointed?.SetIntArrayRegion ?: return null
    value.usePinned { pinnedArray ->
        val pointer = pinnedArray.addressOf(0)
        method.invoke(this, (target as? jintArray), 0, value.size, pointer)
    }
    return target
}