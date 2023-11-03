package dev.datlag.sekret

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
expect class JNIInterface : CStructVar {
    var NewString: CPointer<CFunction<(CPointer<JNIEnvVar>?, CPointer<jCharVar>?, jSize) -> jString?>>?
    var GetStringUTFChars: CPointer<CFunction<(CPointer<JNIEnvVar>?, jString?, CPointer<jBooleanVar>?) -> CPointer<ByteVar>?>>?
}