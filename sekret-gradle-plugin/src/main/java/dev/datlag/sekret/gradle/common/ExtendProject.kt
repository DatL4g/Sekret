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
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

private val Project.kotlinProjectExtension: KotlinProjectExtension?
    get() = this.extensions.findByType<KotlinProjectExtension>()
        ?: (this.extensions.findByName("kotlin") as? KotlinProjectExtension)

private val Project.kotlinBaseExtension: KotlinBaseExtension?
    get() = this.extensions.findByType<KotlinBaseExtension>()
        ?: (this.extensions.findByName("kotlin") as? KotlinBaseExtension)

private val Project.kotlinMultiPlatformExtension: KotlinMultiplatformExtension?
    get() = this.extensions.findByType<KotlinMultiplatformExtension>()
        ?: (this.extensions.findByName("kotlin") as? KotlinMultiplatformExtension)

private val Project.kotlinJvmExtension: KotlinJvmExtension?
    get() = this.extensions.findByType<KotlinJvmExtension>()
        ?: this.extensions.findByName("kotlin") as? KotlinJvmExtension

private val Project.kotlinJvmProjectExtension: KotlinJvmProjectExtension?
    get() = this.extensions.findByType<KotlinJvmProjectExtension>()
        ?: this.extensions.findByName("kotlin") as? KotlinJvmProjectExtension

private val Project.kotlinAndroidExtension: KotlinAndroidExtension?
    get() = this.extensions.findByType<KotlinAndroidExtension>()
        ?: this.extensions.findByName("kotlin") as? KotlinAndroidExtension

private val Project.kotlinAndroidProjectExtension: KotlinAndroidProjectExtension?
    get() = this.extensions.findByType<KotlinAndroidProjectExtension>()
        ?: this.extensions.findByName("kotlin") as? KotlinAndroidProjectExtension

private val Project.kotlinJsExtension: KotlinJsProjectExtension?
    get() = this.extensions.findByType<KotlinJsProjectExtension>()
        ?: this.extensions.findByName("kotlin") as? KotlinJsProjectExtension

private val KotlinProjectExtension.allTargets: List<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOfNotNull(this.target)
        is KotlinMultiplatformExtension -> this.targets.toList()
        else -> emptyList()
    }

internal val Project.allTargets: Iterable<KotlinTarget>
    get() {
        val projectTargets = kotlinProjectExtension?.allTargets
        val multiTargets = listOfNotNull(
            kotlinMultiPlatformExtension?.allTargets,
            kotlinMultiPlatformExtension?.targets
        ).flatten()
        val jvmTargets = listOfNotNull(
            kotlinJvmExtension?.target?.let(::listOfNotNull),
            kotlinJvmProjectExtension?.allTargets,
            kotlinJvmProjectExtension?.target?.let(::listOfNotNull)
        ).flatten()
        val androidTargets = listOfNotNull(
            kotlinAndroidExtension?.target?.let(::listOfNotNull),
            kotlinAndroidProjectExtension?.allTargets,
            kotlinAndroidProjectExtension?.target?.let(::listOfNotNull)
        ).flatten()
        val jsTargets = listOfNotNull(
            kotlinJsExtension?.allTargets
        ).flatten()

        return setOfNotNull(
            projectTargets,
            multiTargets,
            jvmTargets,
            androidTargets,
            jsTargets
        ).flatten()
    }

internal val Project.sourceSets: Iterable<KotlinSourceSet>
    get() {
        return setOfNotNull(
            kotlinProjectExtension?.sourceSets,
            kotlinBaseExtension?.sourceSets,
            kotlinMultiPlatformExtension?.sourceSets,
            kotlinJvmExtension?.sourceSets,
            kotlinJvmProjectExtension?.sourceSets,
            kotlinAndroidExtension?.sourceSets,
            kotlinAndroidProjectExtension?.sourceSets,
            kotlinJsExtension?.sourceSets
        ).flatten()
    }

internal val Project.targetsMapped: Set<Target>
    get() {
        return setOf(
            Target.fromKotlinTargets(allTargets),
            Target.fromSourceSetNames(sourceSets.map { it.name })
        ).flatten().toSet()
    }

internal val Project.isSingleTarget: Boolean
    get() {
        if (kotlinProjectExtension is KotlinMultiplatformExtension || kotlinMultiPlatformExtension != null) {
            return false
        }

        val usingJvm = kotlinJvmExtension != null || kotlinJvmProjectExtension != null
        val usingAndroid = kotlinAndroidExtension != null || kotlinAndroidProjectExtension != null
        val usingJava = usingJvm || usingAndroid
        val usingJs = kotlinJsExtension != null

        return usingJava xor usingJs
    }

fun Project.findMatchingTask(name: String): Task? {
    return this.tasks.findByName(name) ?: this.getTasksByName(
        name,
        false
    ).firstOrNull { t -> t.name == name }
}

inline fun <reified T : Task> Project.findMatchingTaskWithType(name: String): Task? {
    return this.tasks.withType<T>().firstOrNull() ?: findMatchingTask(name)
}

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