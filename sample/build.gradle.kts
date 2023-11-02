plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.gradle.plugin)
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {

        }
    }
}