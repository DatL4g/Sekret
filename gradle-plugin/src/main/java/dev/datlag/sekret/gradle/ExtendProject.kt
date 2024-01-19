package dev.datlag.sekret.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

val Project.kotlinProjectExtension: KotlinProjectExtension
    get() = this.extensions.findByType<KotlinProjectExtension>()
        ?: (this.extensions.findByName("kotlin") as KotlinProjectExtension)

val Project.sekretExtension: SekretPluginExtension
    get() = this.extensions.create(
        name = "sekret",
        type = SekretPluginExtension::class
    ).apply { setupConvention(project) }

val KotlinProjectExtension.targets: Iterable<KotlinTarget>
    get() = when (this) {
        is KotlinSingleTargetExtension<*> -> listOf(this.target)
        is KotlinMultiplatformExtension -> targets
        else -> emptyList()
    }