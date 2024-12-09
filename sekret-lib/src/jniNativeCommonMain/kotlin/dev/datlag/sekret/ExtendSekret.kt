package dev.datlag.sekret

import dev.datlag.sekret.*
import dev.datlag.sekret.getString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public fun SekretHelper.getNativeValue(
    secret: IntArray,
    key: jString,
    env: CPointer<JNIEnvVar>
): jString? {
    val obfuscator = key.getString(env) ?: return null
    return SekretHelper.getNativeValue(secret, obfuscator).toJString(env)
}