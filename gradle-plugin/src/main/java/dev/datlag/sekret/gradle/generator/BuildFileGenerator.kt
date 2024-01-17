package dev.datlag.sekret.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.Target
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

object BuildFileGenerator {

    fun generate(
        version: String,
        forceJS: Boolean,
        container: KotlinSourceSetContainer
    ) {
        val allNames = container.sourceSets.names
        val defaultTargets = Target.fromSourceSetNames(allNames)
        val requiredTargets = Target.addDependingTargets(defaultTargets)

        val fileSpec = FileSpec.scriptBuilder("build.gradle")
            .addPlugins(requiredTargets)
            .beginControlFlow("kotlin")
            .addSourceSets(
                version = version,
                commonJS = forceJS || requiredTargets.any { it.isJS },
                sourceSets = requiredTargets
            )
            .endControlFlow()
    }

    private fun FileSpec.Builder.addPlugins(sourceSets: Set<Target>): FileSpec.Builder {
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