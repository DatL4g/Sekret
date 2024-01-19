package dev.datlag.sekret.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.*
import dev.datlag.sekret.gradle.Target
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import java.io.File

object BuildFileGenerator {

    fun generate(
        project: Project,
        forceJS: Boolean = project.sekretExtension.jsSourceSet.getOrElse(false),
        version: String = SekretPlugin.getVersion()
    ) {
        val extension = project.kotlinProjectExtension
        val allNames = extension.targets.map {
            it.name
        }.toMutableSet().apply {
            addAll(extension.sourceSets.map { it.name })
        }
        val defaultTargets = Target.fromSourceSetNames(allNames)
        val requiredTargets = Target.addDependingTargets(defaultTargets)

        generate(
            version = version,
            forceJS = forceJS,
            targets = requiredTargets,
            outputDir = ModuleGenerator.createBase(project)
        )
    }

    fun generate(
        version: String,
        forceJS: Boolean,
        targets: Iterable<Target>,
        outputDir: File
    ) {
        val fileSpec = FileSpec.scriptBuilder("build.gradle")
            .addPlugins(targets)
            .beginControlFlow("kotlin")
            .addSourceSets(
                version = version,
                commonJS = forceJS || targets.any { it.isJS },
                sourceSets = targets.toSet()
            )
            .endControlFlow()

        val spec = fileSpec.build()

        if (outputDir.existsSafely() && outputDir.canWriteSafely()) {
            spec.writeTo(outputDir)
        }
    }

    private fun FileSpec.Builder.addPlugins(sourceSets: Iterable<Target>): FileSpec.Builder {
        val pluginNames = sourceSets.map { it.requiredPlugin }.toMutableSet().apply {
            add("multiplatform")
        }

        var controlFlow = this.beginControlFlow("plugins")
        pluginNames.forEach { plugin ->
            controlFlow = if (plugin == "multiplatform") {
                controlFlow.addStatement("kotlin(%S)", plugin)
            } else {
                controlFlow.addStatement("id(%S)", plugin)
            }
        }
        return controlFlow.endControlFlow()
    }

    private fun FileSpec.Builder.addSourceSets(
        version: String,
        commonJS: Boolean,
        sourceSets: Set<Target>
    ): FileSpec.Builder {
        var spec = this

        sourceSets.forEach { target ->
            spec = if (target.isNative) {
                spec.beginControlFlow(target.name)
                    .beginControlFlow("binaries")
                    .addStatement("sharedLib()")
                    .endControlFlow()
                    .endControlFlow()
            } else {
                if (target is Target.JS.Default) {
                    spec.addStatement("${target.name}(IR)")
                } else {
                    spec.addStatement("${target.name}()")
                }
            }
        }

        spec = spec.addStatement("applyDefaultHierarchyTemplate()")
        spec = spec.beginControlFlow("sourceSets")

        spec = spec.beginControlFlow("commonMain.dependencies")
        spec = spec.addStatement("api(%S)", "dev.datlag.sekret:sekret:$version")
        spec = spec.endControlFlow()

        spec = spec.beginControlFlow("val jniNativeMain by creating")
        spec = spec.addStatement("nativeMain.orNull?.let { dependsOn(it) } ?: dependsOn(commonMain.get())")
        spec = spec.addStatement("androidNativeMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("linuxMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("mingwMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("macosMain.orNull?.dependsOn(this)")
        spec = spec.endControlFlow()

        spec = spec.beginControlFlow("val jniMain by creating")
        spec = spec.addStatement("dependsOn(commonMain.get())")
        spec = spec.addStatement("androidMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("jvmMain.orNull?.dependsOn(this)")
        spec = spec.endControlFlow()

        if (commonJS) {
            spec = spec.addBodyComment("Can be used for JS, WASM and WASI")
            spec = spec.addBodyComment("Add your required targets accordingly")
            spec = spec.beginControlFlow("val jsCommonMain by creating")
            spec = spec.addStatement("dependsOn(commonMain.get())")
            spec = spec.addStatement("jsMain.orNull?.dependsOn(this)")
            spec = spec.endControlFlow()
        }

        spec.endControlFlow()

        return spec
    }
}