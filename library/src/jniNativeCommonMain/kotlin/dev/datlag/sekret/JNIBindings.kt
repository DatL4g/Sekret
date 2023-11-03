package dev.datlag.sekret

import kotlinx.cinterop.*
import platform.posix.int32_t
import platform.posix.uint16_t
import platform.posix.uint8_t

@OptIn(ExperimentalForeignApi::class)
typealias JNIEnv = CPointer<JNIInterface>

@OptIn(ExperimentalForeignApi::class)
typealias JNIEnvVar = CPointerVarOf<JNIEnv>

@OptIn(ExperimentalForeignApi::class)
typealias jObject = CPointer<*>

@OptIn(ExperimentalForeignApi::class)
typealias jString = jObject

typealias jChar = uint16_t

@OptIn(ExperimentalForeignApi::class)
typealias jCharVar = UShortVarOf<jChar>

typealias jInt = int32_t

typealias jSize = jInt

typealias jBoolean = uint8_t

@OptIn(ExperimentalForeignApi::class)
typealias jBooleanVar = UByteVarOf<jBoolean>