package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.builder.*
import dev.datlag.sekret.gradle.helper.Encoder
import dev.datlag.sekret.gradle.helper.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.io.File

class SekretGradlePlugin : Plugin<Project> {

    private val Project.kotlinExtension: KotlinProjectExtension?
        get() = this.extensions.findByType<KotlinProjectExtension>()

    private fun Project.sekretConfig() =
        this.extensions.findByType(SekretGradleConfiguration::class.java) ?: SekretGradleConfiguration()

    private val Project.packageName: String
        get() = this.sekretConfig().packageName

    private val Project.khashVersion: String
        get() = this.sekretConfig().khashVersion

    private val Project.recreateGeneratedFiles: Boolean
        get() = this.sekretConfig().recreateGeneratedFiles

    private val Project.propertiesFile: File?
        get() {
            val defined = this.sekretConfig().propertiesFile
            val definedFile = this.file(defined)
            val defaultName = SekretGradleConfiguration().propertiesFile

            fun getRootFile(): File {
                val rootDefinedFile = rootProject.file(definedFile)
                return if (rootDefinedFile.existsSafely() && rootDefinedFile.canReadSafely()) {
                    if (rootDefinedFile.isDirectorySafely()) {
                        File(rootDefinedFile, defaultName)
                    } else {
                        rootDefinedFile
                    }
                } else {
                    definedFile
                }
            }

            var propFile = if (definedFile.existsSafely() && definedFile.canReadSafely()) {
                if (definedFile.isDirectorySafely()) {
                    File(definedFile, defaultName)
                } else {
                    definedFile
                }
            } else {
                getRootFile()
            }

            if (!propFile.existsSafely() || !propFile.canReadSafely()) {
                propFile = getRootFile()
            }

            return if (!propFile.existsSafely() || !propFile.canReadSafely()) {
                null
            } else {
                propFile
            }
        }

    override fun apply(target: Project) {
        target.extensions.create(
            "sekret",
            SekretGradleConfiguration::class.java
        )

        with(target) {
            when (val current = kotlinExtension) {
                is KotlinMultiplatformExtension -> {
                    val sekretDir = File(projectDir, "sekret")
                    sekretDir.mkdirsSafely()

                    val srcFolder = createSekretFolders(target, sekretDir)
                    createNativeSourceFiles(target, srcFolder)

                    val propFile = propertiesFile ?: throw IllegalStateException("No secret properties file found")
                    val properties = Utils.propertiesFromFile(propFile)

                    SekretFile.create(
                        srcFolder,
                        File(sekretDir, COMMON_MAIN_FOLDER).also {
                            it.mkdirsSafely()
                        },
                        properties,
                        packageName
                    )
                }
            }
        }
    }

    private fun createSekretFolders(target: Project, sekretDir: File): File {
        val buildFile = File(sekretDir, "build.gradle.kts")
        BuildFile.create(
            file = buildFile,
            deletePrevious = target.recreateGeneratedFiles,
            packageName = target.packageName,
            khashVersion = target.khashVersion
        )

        val interopFolder = File(sekretDir, NATIVE_INTEROP_FOLDER)
        interopFolder.mkdirsSafely()

        val interopFile = File(interopFolder, "sekret.def")
        HeaderFile.create(
            file = interopFile,
            deletePrevious = target.recreateGeneratedFiles
        )

        val srcFolder = File(sekretDir, NATIVE_MAIN_FOLDER)
        srcFolder.mkdirsSafely()

        return srcFolder
    }

    private fun createNativeSourceFiles(target: Project, srcFolder: File) {
        UtilsFile.create(srcFolder, target.packageName)
        ObfuscationFile.create(srcFolder, target.packageName)
    }

    companion object {
        private const val SOURCE_FOLDER = "src/"
        private const val COMMON_MAIN_FOLDER = "${SOURCE_FOLDER}commonMain/kotlin"
        private const val NATIVE_MAIN_FOLDER = "${SOURCE_FOLDER}nativeMain/kotlin/"
        private const val NATIVE_INTEROP_FOLDER = "${SOURCE_FOLDER}nativeInterop/cinterop/"
    }
}