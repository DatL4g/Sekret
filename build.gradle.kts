import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.osdetector) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = CompileOptions.jvmTarget
    }
}