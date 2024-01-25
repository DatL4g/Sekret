import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

plugins {
    kotlin("jvm")
    id("dev.datlag.sekret") version "1.2.0-SNAPSHOT"
    id("org.jetbrains.compose") version "1.5.11"
    alias(libs.plugins.ksp)
}

group = "dev.datlag"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(compose.runtime)
    implementation(compose.desktop.currentOs)

    implementation(project(":sekret-annotations"))
    implementation(project("sekret"))

    PLUGIN_CLASSPATH_CONFIGURATION_NAME(project(":sekret-compiler-plugin"))
}

sekret {
    obfuscation {
        secretMask.set("###")
        secretMaskNull.set(true)
    }
    properties {
        enabled.set(true)
        packageName.set("dev.datlag.sekret.sample")
        encryptionKey.set("password12345")
        propertiesFile.set(project.layout.projectDirectory.file("sekret.properties"))

        nativeCopy {
            desktopComposeResourcesFolder.set(project.layout.projectDirectory.dir("resources"))
        }
    }
}

compose {
    kotlinCompilerPlugin.set(dependencies.compiler.forKotlin("1.9.20"))
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.9.22")
    desktop {
        application {
            mainClass = "MainKt"

            nativeDistributions {
                appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            }
        }
    }
}