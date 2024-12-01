package dev.datlag.sekret.gradle.common

import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.Target
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

internal val Project.kotlinProjectExtension: KotlinProjectExtension
    get() = this.extensions.findByType<KotlinProjectExtension>()
        ?: (this.extensions.findByName("kotlin") as KotlinProjectExtension)

internal val Project.sekretExtension: SekretPluginExtension
    get() = this.extensions.findByType<SekretPluginExtension>()
        ?: runCatching { this@sekretExtension.createSekretExtension() }.getOrNull()
        ?: this.extensions.getByType<SekretPluginExtension>()

internal fun Project.createSekretExtension(): SekretPluginExtension {
    return this@createSekretExtension.extensions.create(
        name = "sekret",
        type = SekretPluginExtension::class
    ).apply { setupConvention(this@createSekretExtension) }
}

internal val KotlinProjectExtension.allTargets: Iterable<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOfNotNull(this.target)
        is KotlinMultiplatformExtension -> this.targets
        else -> emptyList()
    }

internal val KotlinProjectExtension.targetsMapped: Set<Target>
    get() {
        val usedTargets = this.allTargets
        val allFlatten = listOf(
            usedTargets.map { it.targetName },
            usedTargets.map { it.name },
            this.sourceSets.map { it.name }.also {
                println("Used sourceSets: $it")
            },
            when (this) {
                is KotlinJvmProjectExtension -> listOf("jvm")
                is KotlinAndroidProjectExtension -> listOf("android")
                is KotlinJsProjectExtension, is Kotlin2JsProjectExtension -> listOf("js")
                else -> emptyList()
            }
        ).flatten()

        return setOf(
            Target.fromKotlinTargets(usedTargets),
            Target.fromSourceSetNames(allFlatten)
        ).flatten().toSet()
    }

internal val Project.targetsMapped: Set<Target>
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