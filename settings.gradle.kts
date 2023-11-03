rootProject.name = "sekret"

include(":gradle-plugin")
include(":library")

include(":sample")
include(":sample:sekret")

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