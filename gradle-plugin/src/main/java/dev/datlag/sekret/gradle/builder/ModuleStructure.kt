package dev.datlag.sekret.gradle.builder

import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.existsSafely
import dev.datlag.sekret.gradle.mkdirsSafely
import java.io.File

object ModuleStructure {

    fun create(
        sekretDir: File,
        packageName: String,
        version: String,
        sourceSets: Set<BuildFile.Target>,
        forceJS: Boolean?
    ): SourceInfo {
        sekretDir.mkdirsSafely()

        if (!File(sekretDir, "build.gradle.kts").existsSafely()) {
            BuildFile.create(sekretDir, packageName, version, sourceSets, forceJS)
        }

        val hasJsTarget = forceJS == true || sourceSets.any {
            it is BuildFile.Target.JS
        }

        val sourceInfo = SourceInfo(
            nativeMain = File(sekretDir, NATIVE_MAIN_FOLDER),
            jniNativeMain = File(sekretDir, JNI_NATIVE_MAIN_FOLDER),
            jniMain = File(sekretDir, JNI_MAIN_FOLDER),
            jsMain = File(sekretDir, JS_MAIN_FOLDER)
        )
        sourceInfo.mkdirs(hasJsTarget)

        return sourceInfo
    }

    private const val SOURCE_FOLDER = "src/"
    private const val NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/nativeMain/kotlin"
    private const val JNI_NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/jniNativeMain/kotlin"
    private const val JNI_MAIN_FOLDER = "$SOURCE_FOLDER/jniMain/kotlin"
    private const val JS_MAIN_FOLDER = "$SOURCE_FOLDER/jsMain/kotlin"

    data class SourceInfo(
        val nativeMain: File,
        val jniNativeMain: File,
        val jniMain: File,
        val jsMain: File
    ) {
        fun mkdirs(hasJsTarget: Boolean) {
            nativeMain.mkdirsSafely()
            jniNativeMain.mkdirsSafely()
            jniMain.mkdirsSafely()

            if (hasJsTarget) {
                jsMain.mkdirsSafely()
            }
        }

        val hasJs: Boolean
            get() = jsMain.existsSafely() && jsMain.canWriteSafely()
    }
}