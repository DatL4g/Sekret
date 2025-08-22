package dev.datlag.sekret.gradle.generator

import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.canWriteSafely
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.common.mkdirsSafely
import org.gradle.api.Project
import java.io.File

object ModuleGenerator {

    fun createBase(directory: File): File {
        directory.mkdirsSafely()
        return directory
    }

    fun createForTargets(
        directory: File,
        targets: Iterable<Target>
    ): SourceStructure {
        val hasNativeTarget = targets.any { it.isNative }
        val commonJS = targets.any { it.isJS }

        return SourceStructure(
            commonMain = File(directory, COMMON_MAIN_FOLDER),
            nativeMain = File(directory, NATIVE_MAIN_FOLDER),
            jniNativeMain = File(directory, JNI_NATIVE_MAIN_FOLDER),
            jniMain = File(directory, JNI_MAIN_FOLDER),
            webMain = File(directory, WEB_MAIN_FOLDER)
        ).also {
            it.mkdirs(
                nativeTarget = hasNativeTarget,
                jsTarget = commonJS
            )
        }
    }

    private const val SOURCE_FOLDER = "src/"
    private const val COMMON_MAIN_FOLDER = "$SOURCE_FOLDER/commonMain/kotlin"
    private const val NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/nativeMain/kotlin"
    private const val JNI_NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/jniNativeMain/kotlin"
    private const val JNI_MAIN_FOLDER = "$SOURCE_FOLDER/jniMain/kotlin"
    private const val WEB_MAIN_FOLDER = "$SOURCE_FOLDER/webMain/kotlin"

    data class SourceStructure(
        val commonMain: File,
        val nativeMain: File,
        val jniNativeMain: File,
        val jniMain: File,
        val webMain: File
    ) {
        fun mkdirs(
            nativeTarget: Boolean,
            jsTarget: Boolean
        ) {
            if (nativeTarget) {
                nativeMain.mkdirsSafely()
                jniNativeMain.mkdirsSafely()
                jniMain.mkdirsSafely()
            }
            if (jsTarget) {
                webMain.mkdirsSafely()
            }
        }

        val hasNative: Boolean
            get() = nativeMain.existsSafely() && nativeMain.canWriteSafely()

        private val hasJNINative: Boolean
            get() = jniNativeMain.existsSafely() && jniNativeMain.canWriteSafely()

        private val hasJNIDefault: Boolean
            get() = jniMain.existsSafely() && jniMain.canWriteSafely()

        val hasJNI: Boolean
            get() = hasJNINative && hasJNIDefault

        val hasJS: Boolean
            get() = webMain.existsSafely() && webMain.canWriteSafely()
    }
}