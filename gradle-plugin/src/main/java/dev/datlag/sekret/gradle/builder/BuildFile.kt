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
            .beginControlFlow("sourceSets")
            .beginControlFlow("val commonMain by getting")
            .beginControlFlow("dependencies")
            .addStatement("api(%S)", "dev.datlag.sekret:library:$version")
            .endControlFlow()
            .endControlFlow()
            .addStatement("val androidNativeX86Main by getting")
            .addStatement("val androidNativeX64Main by getting")
            .addStatement("val androidNativeArm32Main by getting")
            .addStatement("val androidNativeArm64Main by getting")
            .addStatement("val linuxX64Main by getting")
            .addStatement("val linuxArm64Main by getting")
            .addStatement("val mingwX64Main by getting")
            .beginControlFlow("val jniNativeMain by creating")
            .addStatement("androidNativeX86Main.dependsOn(this)")
            .addStatement("androidNativeX64Main.dependsOn(this)")
            .addStatement("androidNativeArm32Main.dependsOn(this)")
            .addStatement("androidNativeArm64Main.dependsOn(this)")
            .addStatement("linuxX64Main.dependsOn(this)")
            .addStatement("linuxArm64Main.dependsOn(this)")
            .addStatement("mingwX64Main.dependsOn(this)")
            .endControlFlow()
            .beginControlFlow("val androidMain by getting")
            .addStatement("dependsOn(commonMain)")
            .endControlFlow()
            .beginControlFlow("val jvmMain by getting")
            .addStatement("dependsOn(commonMain)")
            .endControlFlow()
            .addBodyComment("Java (Kotlin) Sekret class")
            .beginControlFlow("val jniMain by creating")
            .addStatement("androidMain.dependsOn(this)")
            .addStatement("jvmMain.dependsOn(this)")
            .endControlFlow()
            .beginControlFlow("val jsMain by getting")
            .addStatement("dependsOn(commonMain)")
            .endControlFlow()
            .beginControlFlow("val nativeMain by creating")
            .addStatement("dependsOn(commonMain)")
            .addStatement("jniNativeMain.dependsOn(this)")
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