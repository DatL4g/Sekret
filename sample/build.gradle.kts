import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

plugins {
    kotlin("jvm")
    id("dev.datlag.sekret") version "1.2.1-SNAPSHOT"
    id("org.jetbrains.compose") version "1.5.11"
    alias(libs.plugins.ksp)
}

group = "dev.datlag"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

sekret {
    obfuscation {
        secretAnnotation {
            mask.set("###")
            maskNull.set(true)
        }
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

dependencies {
    implementation(compose.runtime)
    implementation(compose.desktop.currentOs)

    implementation(project(":sekret-annotations"))
    implementation(project(":sekret-lib"))
    implementation(project("sekret"))

    ksp(project(":sekret-ksp"))

    PLUGIN_CLASSPATH_CONFIGURATION_NAME(project(":sekret-compiler-plugin"))
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