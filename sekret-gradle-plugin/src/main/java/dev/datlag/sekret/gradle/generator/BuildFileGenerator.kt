package dev.datlag.sekret.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.*
import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.canWriteSafely
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.common.targetsMapped
import org.gradle.api.Project
import java.io.File

object BuildFileGenerator {

    fun generate(
        project: Project,
        version: String = SekretPlugin.getVersion(),
        overwrite: Boolean = false
    ) {
        val defaultTargets = project.targetsMapped
        val requiredTargets = Target.addDependingTargets(defaultTargets)

        generate(
            version = version,
            targets = requiredTargets,
            outputDir = ModuleGenerator.createBase(project),
            overwrite = overwrite
        )
    }

    fun generate(
        version: String,
        targets: Iterable<Target>,
        outputDir: File,
        overwrite: Boolean = false
    ) {
        val fileSpec = FileSpec.scriptBuilder("build.gradle")
            .addPlugins(targets)
            .beginControlFlow("kotlin")
            .addSourceSets(
                version = version,
                commonJS = targets.any { it.isJS },
                sourceSets = targets.toSet()
            )
            .endControlFlow()

        val spec = fileSpec.build()

        if (outputDir.existsSafely() && outputDir.canWriteSafely()) {
            if (!overwrite) {
                if (!File(outputDir, spec.relativePath).existsSafely()) {
                    spec.writeTo(outputDir)
                }
            } else {
                spec.writeTo(outputDir)
            }
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
        return controlFlow.endControlFlow().addStatement("")
    }

    private fun FileSpec.Builder.addSourceSets(
        version: String,
        commonJS: Boolean,
        sourceSets: Set<Target>
    ): FileSpec.Builder {
        var spec = this
        val containsAndroidNative = sourceSets.any { it.isAndroidNative }
        val containsAndroidJvm = sourceSets.any { it.isAndroidJvm }

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

        spec = spec.addStatement("")
        spec = spec.addStatement("applyDefaultHierarchyTemplate()")
        spec = spec.addStatement("")
        spec = spec.beginControlFlow("sourceSets")

        spec = spec.beginControlFlow("commonMain.dependencies")
        spec = spec.addStatement("api(%S)", "dev.datlag.sekret:sekret:$version")
        spec = spec.endControlFlow()
        spec = spec.addStatement("")

        spec = spec.beginControlFlow("val jniNativeMain by creating")
        spec = spec.addStatement("nativeMain.orNull?.let { dependsOn(it) } ?: dependsOn(commonMain.get())")
        spec = if (containsAndroidNative) {
            spec.addStatement("androidNativeMain.orNull?.dependsOn(this)")
        } else {
            spec.addBodyComment("androidNativeMain.orNull?.dependsOn(this)")
        }
        spec = spec.addStatement("linuxMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("mingwMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("macosMain.orNull?.dependsOn(this)")
        spec = spec.endControlFlow()
        spec = spec.addStatement("")

        spec = spec.beginControlFlow("val jniMain by creating")
        spec = spec.addStatement("dependsOn(commonMain.get())")
        spec = if (containsAndroidJvm) {
            spec.addStatement("androidMain.orNull?.dependsOn(this)")
        } else {
            spec.addBodyComment("androidMain.orNull?.dependsOn(this)")
        }
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