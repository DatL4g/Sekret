package dev.datlag.sekret

import kotlinx.serialization.Serializable

/**
 * Configure Sekret decryption behavior.
 *
 * @param jni specify JNI related configuration (Android and Desktop JVM)
 */
@Serializable
data class SekretConfig(
    val jni: JNI = JNI()
) {
    /**
     * Configure decryption behavior on JNI.
     *
     * @param decryptDirectly if true decrypts directly in native code, might expose keys to JNI tracing.
     * @param fallbackToDirectDecryption if true decrypts in native code if non-native decryption fails.
     * See [decryptDirectly]
     */
    @Serializable
    data class JNI(
        val decryptDirectly: Boolean = false,
        val fallbackToDirectDecryption: Boolean = true,
    ) {
        class Builder {
            var decryptDirectly: Boolean = false
            var fallbackToDirectDecryption: Boolean = true

            fun build(): JNI = JNI(decryptDirectly, fallbackToDirectDecryption)
        }
    }

    class Builder {
        var jni: JNI = JNI()
            private set

        fun jni(block: JNI.Builder.() -> Unit) = apply {
            jni = JNI.Builder().apply(block).build()
        }

        fun build(): SekretConfig = SekretConfig(jni)
    }
}

fun SekretConfig(block: SekretConfig.Builder.() -> Unit) = SekretConfig.Builder().apply(block).build()