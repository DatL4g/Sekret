plugins {
    `kotlin-dsl` version "5.1.2"
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("https://jitpack.io") }
}