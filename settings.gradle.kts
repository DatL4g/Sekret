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
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}