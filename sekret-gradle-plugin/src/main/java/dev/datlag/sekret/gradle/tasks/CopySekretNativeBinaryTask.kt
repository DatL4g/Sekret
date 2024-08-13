package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.common.mkdirsSafely
import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class CopySekretNativeBinaryTask : DefaultTask() {

    @get:Input
    open val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Optional
    @get:InputDirectory
    open val buildDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:InputDirectory
    open val sekretDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Optional
    @get:OutputDirectory
    open val androidDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Optional
    @get:OutputDirectory
    open val desktopComposeDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    open val projectLayout: ProjectLayout = project.layout

    @get:Inject
    abstract val fileSystem: FileSystemOperations

    private val sekretDir: File
        get() = sekretDirectory.asFile.orNull
            ?: projectLayout.projectDirectory.dir("sekret").asFile

    init {
        group = "sekret"
    }

    @TaskAction
    fun copy() {
        if (!enabled.getOrElse(false)) {
            return
        }

        val sekretBuildDir = buildDirectory.asFile.orNull ?: File(ModuleGenerator.createBase(sekretDir), "build")

        val androidJniFolder = androidDirectory.asFile.orNull
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

        val desktopComposeResourcesFolder = desktopComposeDirectory.asFile.orNull
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
        fileSystem.copy {
            from(File(from))
            into(dest.mkdirsSafely())
            exclude("**/*.h", "**/*.def")
        }
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        val sekretProject = project.findProject("sekret")

        enabled.set(extension.properties.enabled)
        buildDirectory.set(sekretProject?.layout?.buildDirectory?.orNull?.asFile)
        sekretDirectory.set(sekretProject?.projectDir ?: File(project.projectDir, "sekret"))
        androidDirectory.set(extension.properties.nativeCopy.androidJNIFolder)
        desktopComposeDirectory.set(extension.properties.nativeCopy.desktopComposeResourcesFolder)
    }

    companion object {
        internal const val NAME = "copySekretNativeBinary"
    }
}