package dev.datlag.sekret

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun CPointer<JNIEnvVar>.newString(chars: CPointer<jCharVar>, length: Int): jString? {
    val method = pointed.pointed?.NewString ?: error("Could not find NewString method in JNI")
    return method.invoke(this, chars, length)
}

@OptIn(ExperimentalForeignApi::class)
actual fun jString.getStringUTFChars(env: CPointer<JNIEnvVar>): CPointer<ByteVar>? {
    val method = env.pointed.pointed?.GetStringUTFChars ?: error("Could not find GetStringUTFChars method in JNI")
    return method.invoke(env, this, null)
}