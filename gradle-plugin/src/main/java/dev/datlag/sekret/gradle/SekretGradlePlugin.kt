package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.builder.*
import dev.datlag.sekret.gradle.helper.Utils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class SekretGradlePlugin : Plugin<Project> {

    private val Project.kotlinExtension: KotlinProjectExtension?
        get() = this.extensions.findByType<KotlinProjectExtension>()

    private fun Project.sekretConfig(task: Task? = null) =
        task?.extensions?.findByType(SekretGradleConfiguration::class)
            ?: task?.extensions?.findByType(SekretGradleConfiguration::class.java)
            ?: this.extensions.findByType(SekretGradleConfiguration::class)
            ?: this.extensions.findByType(SekretGradleConfiguration::class.java)
            ?: SekretGradleConfiguration()

    private fun Project.packageName(task: Task?): String = this.sekretConfig(task).packageName

    private fun Project.propertiesFile(task: Task?): File? {
            val defined = this.sekretConfig(task).propertiesFile
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

    private val Project.settings: Settings?
        get() = (this.rootProject as? GradleInternal?)?.settings

    private fun Project.applyToSettings(block: (Settings) -> Unit) {
        this.settings?.let(block) ?: this.rootProject.gradle.beforeProject {
            this.settings?.let(block)
        }
    }

    override fun apply(target: Project) {
        target.extensions.create(
            "sekret",
            SekretGradleConfiguration::class
        )

        val sekretDir = File(target.projectDir, "sekret")
        sekretDir.mkdirsSafely()

        val srcFolder = createSekretFolders(sekretDir)
        val generateTask = target.tasks.maybeCreate("generateSekret")

        target.applyToSettings { settings ->
            runCatching {
                target.findProject("sekret")?.path
            }.getOrNull()?.let {
                settings.include(it)
            }
        }

        generateTask.doFirst {
            createNativeSourceFiles(target, srcFolder)

            val propFile = target.propertiesFile(this) ?: throw IllegalStateException("No secret properties file found")
            val properties = Utils.propertiesFromFile(propFile)

            SekretFile.create(
                srcFolder,
                File(sekretDir, COMMON_MAIN_FOLDER).also {
                    it.mkdirsSafely()
                },
                properties,
                target.packageName(this)
            )
        }

        val buildTask = target.tasks.findByName("build") ?: target.tasks.withType(KotlinCompile::class).firstOrNull()
        buildTask?.dependsOn(generateTask)

        when (target.kotlinExtension) {
            is KotlinSingleTargetExtension<*> -> {
                target.dependencies {
                    runCatching {
                        target.findProject("sekret")
                    }.getOrNull()?.let {
                        add("implementation", it)
                    } ?: run {
                        sekretModuleNotLoaded(
                            target.path ?: String()
                        )
                    }
                }
            }
            is KotlinMultiplatformExtension -> {
                target.dependencies {
                    runCatching {
                        target.findProject("sekret")
                    }.getOrNull()?.let {
                        add("commonMainImplementation", it)
                    } ?: run {
                        sekretModuleNotLoaded(
                            target.path ?: String()
                        )
                    }
                }
            }
        }
    }

    private fun sekretModuleNotLoaded(path: String) {
        println("Seems like the ${path}${Project.PATH_SEPARATOR}sekret module isn't included to your settings, please add it")
    }

    private fun createSekretFolders(sekretDir: File): File {
        val buildFile = File(sekretDir, "build.gradle.kts")
        BuildFile.create(
            file = buildFile,
            deletePrevious = true
        )

        val interopFolder = File(sekretDir, NATIVE_INTEROP_FOLDER)
        interopFolder.mkdirsSafely()

        val interopFile = File(interopFolder, "sekret.def")
        HeaderFile.create(
            file = interopFile,
            deletePrevious = true
        )

        val srcFolder = File(sekretDir, NATIVE_MAIN_FOLDER)
        srcFolder.mkdirsSafely()

        return srcFolder
    }

    private fun createNativeSourceFiles(target: Project, task: Task?, srcFolder: File) {
        UtilsFile.create(srcFolder, target.packageName(task))
        ObfuscationFile.create(srcFolder, target.packageName(task))
    }

    private fun Task.createNativeSourceFiles(target: Project, srcFolder: File) = createNativeSourceFiles(target, this, srcFolder)

    companion object {
        private const val SOURCE_FOLDER = "src/"
        private const val COMMON_MAIN_FOLDER = "${SOURCE_FOLDER}commonMain/kotlin"
        private const val NATIVE_MAIN_FOLDER = "${SOURCE_FOLDER}nativeMain/kotlin/"
        private const val NATIVE_INTEROP_FOLDER = "${SOURCE_FOLDER}nativeInterop/cinterop/"
    }
}