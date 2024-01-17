package dev.datlag.sekret.gradle.generator

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

object ModuleGenerator {

    fun getRequiredSourceSets(container: KotlinSourceSetContainer) {
        container.sourceSets.names
    }
}