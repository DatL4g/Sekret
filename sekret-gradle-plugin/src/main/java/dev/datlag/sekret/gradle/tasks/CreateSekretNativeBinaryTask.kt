package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.common.findMatchingTask
import dev.datlag.sekret.gradle.common.findMatchingTaskWithType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

open class CreateSekretNativeBinaryTask : DefaultTask() {

    init {
        group = "sekret"
        description = "Compiles native binaries if required for the JVM (including Android)"

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

            generateTask?.let {
                assembleTask?.dependsOn(it) ?: mustRunAfter(it)
            }
            assembleTask?.let { dependsOn(assembleTask) }
        }
    }

    companion object {
        internal const val NAME = "createSekretNativeBinary"
    }
}