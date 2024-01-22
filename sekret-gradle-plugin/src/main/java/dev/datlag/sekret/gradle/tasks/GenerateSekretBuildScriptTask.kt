package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GenerateSekretBuildScriptTask : DefaultTask() {

    init {
        group = "sekret"
    }

    @TaskAction
    fun generate() {
        if (!project.sekretExtension.properties.enabled.getOrElse(false)) {
            return
        }
        BuildFileGenerator.generate(
            project = project,
            overwrite = true
        )
    }

    companion object {
        internal const val NAME = "generateSekretBuildScript"
    }
}