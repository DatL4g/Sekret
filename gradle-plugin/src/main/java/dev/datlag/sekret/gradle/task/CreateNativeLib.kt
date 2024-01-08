package dev.datlag.sekret.gradle.task

import dev.datlag.sekret.gradle.mkdirsSafely
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

object CreateNativeLib {

    fun create(
        androidJniFolder: File?,
        desktopComposeResourceFolder: File?,
        sekretProject: Project?
    ) {
        if (sekretProject == null) {
            println("Sekret module not found, can't copy native libraries")
            return
        }

        if (androidJniFolder != null) {
            val androidArm32 = getBinPath("androidNativeArm32", sekretProject)
            val androidArm64 = getBinPath("androidNativeArm64", sekretProject)
            val androidX64 = getBinPath("androidNativeX64", sekretProject)
            val androidX86 = getBinPath("androidNativeX86", sekretProject)

            if (androidArm32 != null) {
                copyFileFromTo(androidArm32, File(androidJniFolder, "armeabi-v7a"))
            }

            if (androidArm64 != null) {
                copyFileFromTo(androidArm64, File(androidJniFolder, "arm64-v8a"))
            }

            if (androidX64 != null) {
                copyFileFromTo(androidX64, File(androidJniFolder, "x86_64"))
            }

            if (androidX86 != null) {
                copyFileFromTo(androidX86, File(androidJniFolder, "x86"))
            }
        }

        if (desktopComposeResourceFolder != null) {
            val linuxArm64 = getBinPath("linuxArm64", sekretProject)
            val linuxX64 = getBinPath("linuxX64", sekretProject)
            val mingwX64 = getBinPath("mingwX64", sekretProject)
            val macosArm64 = getBinPath("macosArm64", sekretProject)
            val macosX64 = getBinPath("macosX64", sekretProject)

            if (linuxArm64 != null) {
                copyFileFromTo(linuxArm64, File(desktopComposeResourceFolder, "linux-arm64"))
            }

            if (linuxX64 != null) {
                copyFileFromTo(linuxX64, File(desktopComposeResourceFolder, "linux-x64"))
            }

            if (mingwX64 != null) {
                copyFileFromTo(mingwX64, File(desktopComposeResourceFolder, "windows"))
            }

            if (macosArm64 != null) {
                copyFileFromTo(macosArm64, File(desktopComposeResourceFolder, "macos-arm64"))
            }

            if (macosX64 != null) {
                copyFileFromTo(macosX64, File(desktopComposeResourceFolder, "macos-x64"))
            }
        }
    }

    private fun getBinPath(target: String, sekretProject: Project): String? {
        val buildDir = sekretProject.layout.buildDirectory.asFile.get()
        return if (File(buildDir, "bin/$target/releaseShared").exists()) {
            File(buildDir, "bin/$target/releaseShared").canonicalPath
        } else if (File(buildDir, "bin/$target/debugShared").exists()) {
            File(buildDir, "bin/$target/debugShared").canonicalPath
        } else {
            null
        }
    }

    private fun copyFileFromTo(from: String, dest: File) {
        File(from).listFiles { _, name ->
            !name.endsWith(".h") && !name.endsWith(".def")
        }?.filterNotNull()?.forEach {
            dest.mkdirsSafely()

            it.copyRecursively(File(dest, it.name), true)
        }
    }
}