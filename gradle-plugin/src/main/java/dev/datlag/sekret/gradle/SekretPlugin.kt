package dev.datlag.sekret.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

open class SekretPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val sekretExtension: SekretPluginExtension = project.extensions.create(
            name = "sekret",
            type = SekretPluginExtension::class
        ).apply { setupConvention(project) }

        project.plugins.withType(KotlinMultiplatformPluginWrapper::class) {
            val kmpExtension: KotlinMultiplatformExtension = project.extensions.getByType()

        }
    }

    private fun registerGenerate(project: Project) {
        project.tasks.register("generateSekret") {
            group = "sekret"
            dependsOn(project.tasks.withType())
        }
    }
}