package dev.datlag.sekret.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CopyNativeLibraryTask : DefaultTask() {

    init {
        group = "sekret"
    }

    @TaskAction
    fun copy() {

    }
}