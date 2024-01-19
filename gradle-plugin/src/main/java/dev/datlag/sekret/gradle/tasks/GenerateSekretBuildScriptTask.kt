package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateSekretBuildScriptTask : DefaultTask() {

    init {
        group = "sekret"
    }

    @TaskAction
    fun generate() {
        BuildFileGenerator.generate(project = project)
    }
}