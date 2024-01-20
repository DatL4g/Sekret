plugins {
    kotlin("jvm")
    id("dev.datlag.sekret") version "0.5.4-SNAPSHOT"
    id("org.jetbrains.compose") version "1.5.11"
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
}

sekret {
    packageName.set("dev.datlag.sekret.sample")
    encryptionKey.set("password12345")
    propertiesFile.set(project.layout.projectDirectory.file("sekret.properties"))
    desktopComposeResourcesFolder.set(project.layout.projectDirectory.dir("resources"))
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