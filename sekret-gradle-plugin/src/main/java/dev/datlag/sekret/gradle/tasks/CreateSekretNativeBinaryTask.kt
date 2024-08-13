package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.findMatchingTask
import dev.datlag.sekret.gradle.common.findMatchingTaskWithType
import org.gradle.api.DefaultTask
import org.gradle.api.Project

open class CreateSekretNativeBinaryTask : DefaultTask() {

    init {
        group = "sekret"
    }

    fun setupDependingTasks(project: Project) {
        val assembleTask = project.findProject("sekret")?.findMatchingTask("assemble")
        val generateTask = project.findMatchingTaskWithType<GenerateSekretTask>(GenerateSekretTask.NAME)

        if (assembleTask != null && generateTask != null) {
            dependsOn(generateTask, assembleTask)
        } else if (assembleTask != null) {
            dependsOn(assembleTask)
        } else if (generateTask != null) {
            dependsOn(generateTask)
        }
    }

    companion object {
        internal const val NAME = "createSekretNativeBinary"
    }
}