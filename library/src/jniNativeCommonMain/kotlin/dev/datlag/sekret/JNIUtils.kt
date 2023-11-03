package dev.datlag.sekret

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
fun CPointer<JNIEnvVar>.newString(chars: CPointer<jCharVar>, length: Int): jString? {
    val method = pointed.pointed?.NewString ?: error("Could not find NewString method in JNI")
    return method.invoke(this, chars, length)
}

@OptIn(ExperimentalForeignApi::class)
fun String.toJString(env: CPointer<JNIEnvVar>): jString? = memScoped {
    env.newString(wcstr.ptr, length)
}

@OptIn(ExperimentalForeignApi::class)
fun jString.getStringUTFChars(env: CPointer<JNIEnvVar>): CPointer<ByteVar>? {
    val method = env.pointed.pointed?.GetStringUTFChars ?: error("Could not find GetStringUTFChars method in JNI")
    return method.invoke(env, this, null)
}

@OptIn(ExperimentalForeignApi::class)
fun jString.getString(env: CPointer<JNIEnvVar>): String {
    val chars = getStringUTFChars(env)
    return chars?.toKStringFromUtf8() ?: error("Unable to create String from the given jString")
}