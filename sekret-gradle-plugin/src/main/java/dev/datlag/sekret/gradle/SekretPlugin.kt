package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.common.createSekretExtension
import dev.datlag.sekret.gradle.common.isSingleTarget
import dev.datlag.sekret.gradle.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.maybeCreate
import java.util.*

open class SekretPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.createSekretExtension()

        project.tasks.maybeCreate(GenerateSekretBuildScriptTask.NAME, GenerateSekretBuildScriptTask::class).also { task ->
            task.apply(project, extension)
        }
        project.tasks.maybeCreate(GenerateSekretTask.NAME, GenerateSekretTask::class).also { task ->
            task.apply(project, extension)
        }
        project.tasks.maybeCreate(CopySekretNativeBinaryTask.NAME, CopySekretNativeBinaryTask::class).also { task ->
            task.apply(project, extension)
        }
        project.tasks.maybeCreate(CreateSekretValueTask.NAME, CreateSekretValueTask::class).also { task ->
            task.apply(project, extension)
        }
        project.tasks.maybeCreate(CreateSekretNativeBinaryTask.NAME, CreateSekretNativeBinaryTask::class).also { task ->
            task.setupDependingTasks(project)
        }
        project.tasks.maybeCreate(CreateAndCopySekretNativeBinaryTask.NAME, CreateAndCopySekretNativeBinaryTask::class).also { task ->
            task.setupDependingTasks(project)
        }

        if (project.isSingleTarget) {
            project.dependencies {
                runCatching {
                    project.findProject("sekret")
                }.getOrNull()?.let {
                    add("implementation", it)
                }

                add("implementation", "dev.datlag.sekret:sekret:${getVersion()}")
            }
        } else {
            project.dependencies {
                runCatching {
                    project.findProject("sekret")
                }.getOrNull()?.let {
                    add("commonMainImplementation", it)
                }

                add("commonMainImplementation", "dev.datlag.sekret:sekret:${getVersion()}")
            }
        }

        project.pluginManager.apply(SekretCompilerSubPlugin::class.java)
    }

    companion object {
        private const val VERSION = "2.0.0-alpha-08-SNAPSHOT"

        internal fun getVersion(): String {
            return runCatching {
                val props = Properties()
                props.load(Companion::class.java.getResourceAsStream("sekret_plugin.properties"))
                props.getProperty("version")
            }.getOrNull()?.ifBlank { null } ?: VERSION
        }
    }
}