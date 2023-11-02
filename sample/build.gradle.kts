plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.gradle.plugin)
}

sekret {
    packageName = "dev.datlag.sekret.sample"
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
        }
    }
}