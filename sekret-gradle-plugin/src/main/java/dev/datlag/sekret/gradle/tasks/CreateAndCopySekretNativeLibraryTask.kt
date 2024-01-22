package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.*
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateAndCopySekretNativeLibraryTask : DefaultTask() {

    init {
        group = "sekret"
    }

    private val sekretProject: Project?
        get() = runCatching {
            project.findProject("sekret")
        }.getOrNull()

    fun setupDependingTasks() {
        val assembleTask = sekretProject?.findMatchingTask("assemble")
        val generateTask = project.findMatchingTaskWithType<GenerateSekretTask>(GenerateSekretTask.NAME)

        if (assembleTask != null && generateTask != null) {
            dependsOn(generateTask, assembleTask)
        } else if (assembleTask != null) {
            dependsOn(assembleTask)
        } else if (generateTask != null) {
            dependsOn(generateTask)
        }
    }

    @TaskAction
    fun createAndCopy() {
        val config = project.sekretExtension.properties
        if (!config.enabled.getOrElse(false)) {
            return
        }

        val sekretDir = ModuleGenerator.createBase(project)
        val sekretBuildDir = sekretProject?.layout?.buildDirectory?.orNull?.asFile ?: File(sekretDir, "build")

        val androidJniFolder = config.androidJNIFolder.orNull?.asFile
        if (androidJniFolder != null) {
            val androidArm32 = getBinPath("androidNativeArm32", sekretBuildDir)
            val androidArm64 = getBinPath("androidNativeArm64", sekretBuildDir)
            val androidX64 = getBinPath("androidNativeX64", sekretBuildDir)
            val androidX86 = getBinPath("androidNativeX86", sekretBuildDir)

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

        val desktopComposeResourcesFolder = config.desktopComposeResourcesFolder.orNull?.asFile
        if (desktopComposeResourcesFolder != null) {
            val linuxArm64 = getBinPath("linuxArm64", sekretBuildDir)
            val linuxX64 = getBinPath("linuxX64", sekretBuildDir)
            val mingwX64 = getBinPath("mingwX64", sekretBuildDir)
            val macosArm64 = getBinPath("macosArm64", sekretBuildDir)
            val macosX64 = getBinPath("macosX64", sekretBuildDir)

            if (linuxArm64 != null) {
                copyFileFromTo(linuxArm64, File(desktopComposeResourcesFolder, "linux-arm64"))
            }

            if (linuxX64 != null) {
                copyFileFromTo(linuxX64, File(desktopComposeResourcesFolder, "linux-x64"))
            }

            if (mingwX64 != null) {
                copyFileFromTo(mingwX64, File(desktopComposeResourcesFolder, "windows"))
            }

            if (macosArm64 != null) {
                copyFileFromTo(macosArm64, File(desktopComposeResourcesFolder, "macos-arm64"))
            }

            if (macosX64 != null) {
                copyFileFromTo(macosX64, File(desktopComposeResourcesFolder, "macos-x64"))
            }
        }
    }

    private fun getBinPath(target: String, buildDir: File): String? {
        return if (File(buildDir, "bin/$target/releaseShared").existsSafely()) {
            File(buildDir, "bin/$target/releaseShared").canonicalPath
        } else if (File(buildDir, "bin/$target/releaseStatic").existsSafely()) {
            File(buildDir, "bin/$target/releaseStatic").canonicalPath
        } else if (File(buildDir, "bin/$target/debugShared").existsSafely()) {
            File(buildDir, "bin/$target/debugShared").canonicalPath
        } else if (File(buildDir, "bin/$target/debugStatic").existsSafely()) {
            File(buildDir, "bin/$target/debugStatic").canonicalPath
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

    companion object {
        internal const val NAME = "createAndCopySekretNativeLibrary"
    }
}