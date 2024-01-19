package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.tasks.GenerateSekretBuildScriptTask
import dev.datlag.sekret.gradle.tasks.GenerateSekretTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import java.util.*

open class SekretPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.maybeCreate("generateBuildScript", GenerateSekretBuildScriptTask::class)
        project.tasks.maybeCreate("generateSekret", GenerateSekretTask::class)

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

    private fun registerGenerate(project: Project) {
        project.tasks.register("generateSekret") {
            group = "sekret"
            dependsOn(project.tasks.withType())
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

    companion object {
        private const val VERSION = "0.5.0-SNAPSHOT"

        internal fun getVersion(): String {
            return runCatching {
                val props = Properties()
                props.load(Companion::class.java.getResourceAsStream("sekret_plugin.properties"))
                props.getProperty("version")
            }.getOrNull()?.ifBlank { null } ?: VERSION
        }
    }
}