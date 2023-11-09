package dev.datlag.sekret.gradle.builder

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.createEmpty
import dev.datlag.sekret.gradle.existsSafely
import java.io.File

object BuildFile {
    fun create(
        directory: File,
        packageName: String, // can be used for the android plugin
        version: String,
        sourceSets: Set<Target>
    ) {
        val nativeSourceSets = sourceSets.toMutableSet()
        if (sourceSets.contains(Target.Desktop.JVM)) {
            nativeSourceSets.add(Target.Desktop.Linux.NATIVE_64)
            nativeSourceSets.add(Target.Desktop.Linux.NATIVE_ARM_64)
            nativeSourceSets.add(Target.Desktop.Windows)
        }
        if (sourceSets.contains(Target.Android.JVM)) {
            nativeSourceSets.add(Target.Android.NATIVE_32)
            nativeSourceSets.add(Target.Android.NATIVE_64)
            nativeSourceSets.add(Target.Android.NATIVE_ARM_32)
            nativeSourceSets.add(Target.Android.NATIVE_ARM_64)
        }

        val fileSpecBuilder = FileSpec.scriptBuilder("build.gradle")
            .addPlugins(nativeSourceSets)
            .beginControlFlow("kotlin")
            .addSourceSets(version, nativeSourceSets)
            .endControlFlow()

        val fileSpec = fileSpecBuilder.build()
        if (directory.existsSafely() && directory.canWriteSafely()) {
            fileSpec.writeTo(directory)
        } else {
            fileSpec.writeTo(System.out)
        }
    }

    private fun FileSpec.Builder.addSourceSets(version: String, sourceSets: Set<Target>): FileSpec.Builder {
        var spec = this

        sourceSets.forEach { target ->
            spec = if (target.native) {
                spec.beginControlFlow(target.name)
                    .beginControlFlow("binaries")
                    .addStatement("sharedLib()")
                    .endControlFlow()
                    .endControlFlow()
            } else {
                spec.addStatement("${target.name}()")
            }
        }

        spec = spec.addStatement("applyDefaultHierarchyTemplate()")
        spec = spec.beginControlFlow("sourceSets")

        spec = spec.beginControlFlow("val commonMain by getting")
        spec = spec.beginControlFlow("dependencies")
        spec = spec.addStatement("api(%S)", "dev.datlag.sekret:sekret:$version")
        spec = spec.endControlFlow()
        spec = spec.endControlFlow()

        spec = spec.beginControlFlow("val jniNativeMain by creating")
        spec = spec.addStatement("nativeMain.orNull?.let { dependsOn(it) }")
        spec = spec.addStatement("androidNativeMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("linuxMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("mingwMain.orNull?.dependsOn(this)")
        spec = spec.endControlFlow()

        spec = spec.beginControlFlow("val jniMain by creating")
        spec = spec.addStatement("androidMain.orNull?.dependsOn(this)")
        spec = spec.addStatement("jvmMain.orNull?.dependsOn(this)")
        spec = spec.endControlFlow()

        spec = spec.endControlFlow()

        return spec
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

    sealed class Target(
        open val name: String,
        open val sourceSet: String = name,
        open val native: Boolean = false,
        open val jniNative: Boolean = false,
        open val jni: Boolean = false
    ) {
        sealed class Android(
            override val name: String,
            override val sourceSet: String = name,
            override val native: Boolean = true,
            override val jniNative: Boolean = true,
            override val jni: Boolean = false
        ) : Target(name, sourceSet, native, jniNative, jni) {
            object NATIVE_32 : Android("androidNativeX86")
            object NATIVE_64 : Android("androidNativeX64")
            object NATIVE_ARM_32 : Android("androidNativeArm32")
            object NATIVE_ARM_64 : Android("androidNativeArm64")
            object JVM : Android("androidTarget", "android", native = false, jniNative = false, jni = true) {
                override val requiredPlugin: String = "com.android.library"
            }
        }

        sealed class Desktop(
            override val name: String,
            override val sourceSet: String = name,
            override val native: Boolean = true,
            override val jniNative: Boolean = true,
            override val jni: Boolean = false
        ) : Target(name, sourceSet, native, jniNative, jni) {
            sealed class Linux(override val name: String) : Desktop(name) {
                object NATIVE_64 : Linux("linuxX64")
                object NATIVE_ARM_64 : Linux("linuxArm64")
            }
            object Windows : Desktop("mingwX64")
            object JVM : Desktop("jvm", native = false, jniNative = false, jni = true)
        }

        open val requiredPlugin: String = "multiplatform"

        fun matchesName(name: String): Boolean {
            return this.name == name || this.sourceSet == name
        }

        companion object {
            private const val ENDING_MAIN = "Main"
            private const val ENDING_TEST = "Test"

            fun fromSourceSetNames(names: Collection<String>): Set<Target> {
                val flatNames = names.map { name ->
                    if (name.endsWith(ENDING_MAIN)) {
                        name.substringBeforeLast(ENDING_MAIN)
                    } else if (name.endsWith(ENDING_TEST)) {
                        name.substringBeforeLast(ENDING_TEST)
                    } else {
                        name
                    }
                }

                return flatNames.mapNotNull { name ->
                    when {
                        Android.NATIVE_32.matchesName(name) -> Android.NATIVE_32
                        Android.NATIVE_64.matchesName(name) -> Android.NATIVE_64
                        Android.NATIVE_ARM_32.matchesName(name) -> Android.NATIVE_ARM_32
                        Android.NATIVE_ARM_64.matchesName(name) -> Android.NATIVE_ARM_64
                        Android.JVM.matchesName(name) -> Android.JVM

                        Desktop.Linux.NATIVE_64.matchesName(name) -> Desktop.Linux.NATIVE_64
                        Desktop.Linux.NATIVE_ARM_64.matchesName(name) -> Desktop.Linux.NATIVE_ARM_64
                        Desktop.Windows.matchesName(name) -> Desktop.Windows
                        Desktop.JVM.matchesName(name) -> Desktop.JVM
                        else -> null
                    }
                }.toSet()
            }
        }
    }

}