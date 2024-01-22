package dev.datlag.sekret.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

val Project.kotlinProjectExtension: KotlinProjectExtension
    get() = this.extensions.findByType<KotlinProjectExtension>()
        ?: (this.extensions.findByName("kotlin") as KotlinProjectExtension)

val Project.sekretExtension: SekretPluginExtension
    get() = this.extensions.findByType<SekretPluginExtension>() ?: runCatching {
        this@sekretExtension.extensions.create(
            name = "sekret",
            type = SekretPluginExtension::class
        ).apply { setupConvention(this@sekretExtension) }
    }.getOrNull() ?: this.extensions.getByType<SekretPluginExtension>()

val KotlinProjectExtension.allTargets: Iterable<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOf(this.target)
        is KotlinMultiplatformExtension -> targets
        else -> emptyList()
    }

val KotlinProjectExtension.targetsMapped: Set<Target>
    get() {
        val allFlatten = listOf(
            this.allTargets.map {
                it.targetName
            },
            this.allTargets.map {
                it.name
            },
            this.sourceSets.map {
                it.name
            },
            when (this) {
                is KotlinJvmProjectExtension -> listOf("jvm")
                is KotlinAndroidProjectExtension -> listOf("android")
                is KotlinJsProjectExtension, is Kotlin2JsProjectExtension -> listOf("js")
                else -> emptyList()
            }
        ).flatten()

        return Target.fromSourceSetNames(allFlatten)
    }

val Project.targetsMapped: Set<Target>
    get() = kotlinProjectExtension.targetsMapped

fun Project.findMatchingTask(name: String): Task? {
    return this.tasks.findByName(name) ?: this.getTasksByName(
        name,
        false
    ).firstOrNull { t -> t.name == name }
}

inline fun <reified T : Task> Project.findMatchingTaskWithType(name: String): Task? {
    return this.tasks.withType<T>().firstOrNull() ?: findMatchingTask(name)
}