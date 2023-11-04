package dev.datlag.sekret.gradle.builder

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.createEmpty
import dev.datlag.sekret.gradle.existsSafely
import java.io.File

object BuildFile {
    fun create(
        directory: File,
        packageName: String,
        version: String
    ) {
        val fileSpecBuilder = FileSpec.scriptBuilder("build.gradle")
            .beginControlFlow("plugins")
            .addStatement("kotlin(%S)", "multiplatform")
            .addStatement("id(%S)", "com.android.library")
            .endControlFlow()
            .beginControlFlow("kotlin")
            .addBodyComment("native targets")
            .addStatement("androidNativeX86()")
            .addStatement("androidNativeX64()")
            .addStatement("androidNativeArm32()")
            .addStatement("androidNativeArm64()")
            .addStatement("linuxX64()")
            .addStatement("linuxArm64()")
            .addStatement("mingwX64()")
            .addBodyComment("non-native targets")
            .addStatement("androidTarget()")
            .addStatement("jvm()")
            .beginControlFlow("js(IR)")
            .addStatement("browser()")
            .addStatement("nodejs()")
            .endControlFlow()
            .addStatement("applyDefaultHierarchyTemplate()")
            .beginControlFlow("sourceSets")
            .beginControlFlow("val commonMain by getting")
            .beginControlFlow("dependencies")
            .addStatement("api(%S)", "dev.datlag.sekret:library:$version")
            .endControlFlow()
            .endControlFlow()
            .beginControlFlow("val jniNativeMain by creating")
            .addStatement("dependsOn(nativeMain.get())")
            .addStatement("androidNativeMain.get().dependsOn(this)")
            .addStatement("linuxMain.get().dependsOn(this)")
            .addStatement("mingwMain.get().dependsOn(this)")
            .endControlFlow()
            .addBodyComment("Java (Kotlin) Sekret class")
            .beginControlFlow("val jniMain by creating")
            .addStatement("androidMain.get().dependsOn(this)")
            .addStatement("jvmMain.get().dependsOn(this)")
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .beginControlFlow("android")
            .addStatement("compileSdk = 34")
            .addStatement("namespace = %S", packageName)
            .endControlFlow()

        val fileSpec = fileSpecBuilder.build()
        if (directory.existsSafely() && directory.canWriteSafely()) {
            fileSpec.writeTo(directory)
        } else {
            fileSpec.writeTo(System.out)
        }
    }

}