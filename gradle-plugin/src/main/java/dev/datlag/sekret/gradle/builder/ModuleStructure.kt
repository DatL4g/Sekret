package dev.datlag.sekret.gradle.builder

import dev.datlag.sekret.gradle.existsSafely
import dev.datlag.sekret.gradle.mkdirsSafely
import java.io.File

object ModuleStructure {

    fun create(sekretDir: File, packageName: String, version: String, sourceSets: Set<BuildFile.Target>): SourceInfo {
        sekretDir.mkdirsSafely()

        if (!File(sekretDir, "build.gradle.kts").existsSafely()) {
            BuildFile.create(sekretDir, packageName, version, sourceSets)
        }

        val sourceInfo = SourceInfo(
            nativeMain = File(sekretDir, NATIVE_MAIN_FOLDER),
            jniNativeMain = File(sekretDir, JNI_NATIVE_MAIN_FOLDER),
            jniMain = File(sekretDir, JNI_MAIN_FOLDER)
        )
        sourceInfo.mkdirs()

        return sourceInfo
    }

    private const val SOURCE_FOLDER = "src/"
    private const val NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/nativeMain/kotlin"
    private const val JNI_NATIVE_MAIN_FOLDER = "$SOURCE_FOLDER/jniNativeMain/kotlin"
    private const val JNI_MAIN_FOLDER = "$SOURCE_FOLDER/jniMain/kotlin"

    data class SourceInfo(
        val nativeMain: File,
        val jniNativeMain: File,
        val jniMain: File
    ) {
        fun mkdirs() {
            nativeMain.mkdirsSafely()
            jniNativeMain.mkdirsSafely()
            jniMain.mkdirsSafely()
        }
    }
}