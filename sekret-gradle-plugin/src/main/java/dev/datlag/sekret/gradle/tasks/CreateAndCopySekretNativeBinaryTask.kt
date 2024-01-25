package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class CreateAndCopySekretNativeBinaryTask : DefaultTask() {

    init {
        group = "sekret"
    }

    private val sekretProject: Project?
        get() = runCatching {
            project.findProject("sekret")
        }.getOrNull()

    fun setupDependingTasks() {
        val assembleTask = sekretProject?.findMatchingTask("assemble")
        val generateTask = project.findMatchingTaskWithType<GenerateSekretTask>(GenerateSekretTask.NAME)
        val copyTask = project.findMatchingTaskWithType<CopySekretNativeBinaryTask>(CopySekretNativeBinaryTask.NAME)

        if (assembleTask != null && generateTask != null) {
            dependsOn(generateTask, assembleTask, copyTask)
        } else if (assembleTask != null) {
            dependsOn(assembleTask, copyTask)
        } else if (generateTask != null) {
            dependsOn(generateTask, copyTask)
        }
    }

    companion object {
        internal const val NAME = "createAndCopySekretNativeBinary"
    }
}