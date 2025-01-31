rootProject.name = "sekret"

include(":sekret-gradle-plugin")
include(":sekret-lib")
include(":sekret-annotations")
include(":sekret-compiler-plugin")
include(":sekret-ksp")
// include(":sample", ":sample:sekret")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}