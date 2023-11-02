plugins {
    `kotlin-dsl`
    kotlin("jvm")
    id("java-gradle-plugin")
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.kotlin.gradle.plugin.api)
    implementation(libs.kotlin.poet)
}

gradlePlugin {
    plugins {
        create("sekretPlugin") {
            id = artifact
            implementationClass = "dev.datlag.sekret.gradle.SekretGradlePlugin"
            displayName = "Sekret Gradle Plugin"
            description = "Gradle Plugin for Sekret"
        }
    }
}