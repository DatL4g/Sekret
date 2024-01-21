package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.tasks.CreateAndCopySekretNativeLibraryTask
import dev.datlag.sekret.gradle.tasks.GenerateSekretBuildScriptTask
import dev.datlag.sekret.gradle.tasks.GenerateSekretTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.maybeCreate
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import java.util.*

open class SekretPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.maybeCreate(GenerateSekretBuildScriptTask.NAME, GenerateSekretBuildScriptTask::class)
        project.tasks.maybeCreate(GenerateSekretTask.NAME, GenerateSekretTask::class)
        project.tasks.maybeCreate(CreateAndCopySekretNativeLibraryTask.NAME, CreateAndCopySekretNativeLibraryTask::class).also {
            it.setupDependingTasks()
        }

        val expose = project.sekretExtension.exposeModule.getOrElse(false)

        when (project.kotlinProjectExtension) {
            is KotlinSingleTargetExtension<*> -> {
                project.dependencies {
                    runCatching {
                        project.findProject("sekret")
                    }.getOrNull()?.let {
                        val exposure = if (expose) {
                            "api"
                        } else {
                            "implementation"
                        }

                        add(exposure, it)
                    }
                }
            }
            is KotlinMultiplatformExtension -> {
                project.dependencies {
                    runCatching {
                        project.findProject("sekret")
                    }.getOrNull()?.let {
                        val exposure = if (expose) {
                            "commonMainApi"
                        } else {
                            "commonMainImplementation"
                        }

                        add(exposure, it)
                    }
                }
            }
        }
    }

    companion object {
        private const val VERSION = "1.0.1"

        internal fun getVersion(): String {
            return runCatching {
                val props = Properties()
                props.load(Companion::class.java.getResourceAsStream("sekret_plugin.properties"))
                props.getProperty("version")
            }.getOrNull()?.ifBlank { null } ?: VERSION
        }
    }
}