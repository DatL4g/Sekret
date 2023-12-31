package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.builder.*
import dev.datlag.sekret.gradle.helper.Utils
import dev.datlag.sekret.gradle.task.CreateNativeLib
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
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.ObservableSet
import java.io.File
import java.util.Properties

class SekretGradlePlugin : Plugin<Project> {

    private val Project.kotlinExtension: KotlinProjectExtension?
        get() = this.extensions.findByType<KotlinProjectExtension>()

    private val Project.kotlinMultiplatform: KotlinMultiplatformExtension?
        get() = this.extensions.findByType<KotlinMultiplatformExtension>()
            ?: (this.extensions.findByName("kotlin") as? KotlinMultiplatformExtension)

    private fun Project.sekretConfig(task: Task? = null) =
        task?.extensions?.findByType(SekretGradleConfiguration::class)
            ?: task?.extensions?.findByType(SekretGradleConfiguration::class.java)
            ?: this.extensions.findByType(SekretGradleConfiguration::class)
            ?: this.extensions.findByType(SekretGradleConfiguration::class.java)
            ?: SekretGradleConfiguration()

    private fun Project.packageName(task: Task?): String = this.sekretConfig(task).packageName.trim()

    private fun Project.password(task: Task?): String = this.sekretConfig(task).password?.ifBlank {
        packageName(task)
    }?.trim() ?: packageName(task)

    private fun Project.generateJS(task: Task?): Boolean? = this.sekretConfig(task).generateJsSourceSet

    private fun Project.exposeModule(task: Task?): Boolean = this.sekretConfig(task).exposeModule

    private fun Project.androidJNIFolder(task: Task?): String? = this.sekretConfig(task).androidJniFolder
    private fun Project.desktopComposeResourceFolder(task: Task?): String? = this.sekretConfig(task).desktopComposeResourceFolder

    private fun Project.propertiesFile(task: Task?): File? {
            val defined = this.sekretConfig(task).propertiesFile
            val definedFile = this.file(defined)
            val defaultName = SekretGradleConfiguration().propertiesFile

            fun getRootFile(): File {
                val rootDefinedFile = this.rootProject.file(definedFile)
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

    private val KotlinCompilation<*>.allKotlinSourceSetsObservable
        get() = this.allKotlinSourceSets as ObservableSet<KotlinSourceSet>

    private val KotlinCompilation<*>.kotlinSourceSetsObservable
        get() = this.kotlinSourceSets as ObservableSet<KotlinSourceSet>

    override fun apply(target: Project) {
        target.extensions.create(
            "sekret",
            SekretGradleConfiguration::class
        )

        val sekretDir = File(target.projectDir, "sekret")
        val generateTask = target.tasks.maybeCreate("generateSekret").apply {
            group = "sekret"
        }

        target.applyToSettings { settings ->
            runCatching {
                target.findProject("sekret")?.path
            }.getOrNull()?.let {
                settings.include(it)
            }
        }

        generateTask.doFirst {
            val sourceInfo = ModuleStructure.create(
                sekretDir,
                target.packageName(this),
                getVersion(),
                target.kotlinMultiplatform?.sourceSets?.names?.let { BuildFile.Target.fromSourceSetNames(it) }
                    ?: setOf(BuildFile.Target.Desktop.JVM),
                target.generateJS(this)
            )

            val propFile = target.propertiesFile(this) ?: throw IllegalStateException("No secret properties file found")
            val properties = Utils.propertiesFromFile(propFile)

            SekretFile.create(
                sourceInfo,
                properties,
                target.packageName(this),
                target.password(this)
            )
        }

        val buildTask = target.tasks.findByName("build") ?: target.tasks.withType(KotlinCompile::class).firstOrNull()
        buildTask?.dependsOn(generateTask)

        val createTask = target.tasks.maybeCreate("createNativeLib").apply {
            group = "sekret"
        }
        val sekretProject = runCatching {
            target.findProject("sekret")
        }.getOrNull()
        val sekretAssemble = sekretProject?.getTasksByName("assemble", false)?.firstOrNull { t -> t.name == "assemble" }

        createTask.dependsOn(generateTask, sekretAssemble)

        createTask.doLast {
            val androidFolder = target.androidJNIFolder(this)?.let { File(it) }
            val desktopComposeFolder = target.desktopComposeResourceFolder(this)?.let { File(it) }

            CreateNativeLib.create(
                androidFolder,
                desktopComposeFolder,
                sekretProject ?: runCatching {
                    target.findProject("sekret")
                }.getOrNull()
            )
        }

        when (target.kotlinExtension) {
            is KotlinSingleTargetExtension<*> -> {
                target.dependencies {
                    runCatching {
                        target.findProject("sekret")
                    }.getOrNull()?.let {
                        val exposure = if (target.exposeModule(null)) {
                            "api"
                        } else {
                            "implementation"
                        }

                        add(exposure, it)
                    } ?: run {
                        sekretModuleNotLoaded(target.path)
                    }
                }
            }
            is KotlinMultiplatformExtension -> {
                target.dependencies {
                    runCatching {
                        target.findProject("sekret")
                    }.getOrNull()?.let {
                        val exposure = if (target.exposeModule(null)) {
                            "commonMainApi"
                        } else {
                            "commonMainImplementation"
                        }

                        add(exposure, it)
                    } ?: run {
                        sekretModuleNotLoaded(target.path)
                    }
                }
            }
        }
    }

    private fun sekretModuleNotLoaded(path: String) {
        println("Seems like the ${sekretModule(path)} module isn't included to your settings, please add it")
    }

    private fun sekretModule(path: String): String {
        val separator = Project.PATH_SEPARATOR
        return if (path.endsWith(separator)) {
            "${path}sekret"
        } else {
            "${path}${separator}sekret"
        }
    }

    private fun getVersion(): String {
        return runCatching {
            val props = Properties()
            props.load(javaClass.classLoader.getResourceAsStream("sekret_plugin.properties"))
            props.getProperty("version")
        }.getOrNull()?.ifBlank { null } ?: VERSION
    }

    companion object {
        private const val VERSION = "0.4.0"
    }
}