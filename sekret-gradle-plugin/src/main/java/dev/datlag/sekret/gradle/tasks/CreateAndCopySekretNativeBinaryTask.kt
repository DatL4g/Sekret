package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class CreateAndCopySekretNativeBinaryTask : DefaultTask() {

    init {
        group = "sekret"
        description = "Combines compile and copy task ['${CreateSekretNativeBinaryTask.NAME}', '${CopySekretNativeBinaryTask.NAME}']"

        // No inputs or outputs as this task is only used to order other tasks.
        outputs.upToDateWhen { true }
    }

    @TaskAction
    fun run() {
        // No actions needed here
    }

    fun setupDependingTasks(project: Project) {
        project.afterEvaluate {
            val assembleTask = project.findProject("sekret")?.findMatchingTask("assemble")
            val generateTask = project.findMatchingTaskWithType<GenerateSekretTask>(GenerateSekretTask.NAME)
            val copyTask = project.findMatchingTaskWithType<CopySekretNativeBinaryTask>(CopySekretNativeBinaryTask.NAME)

            generateTask?.let {
                assembleTask?.dependsOn(it) ?: mustRunAfter(it)
            }
            assembleTask?.let { dependsOn(it) }
            copyTask?.let { finalizedBy(it) }
        }
    }

    companion object {
        internal const val NAME = "createAndCopySekretNativeBinary"
    }
}