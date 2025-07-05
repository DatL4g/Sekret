import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.osdetector)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

kotlin {
    androidNativeX86()
    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()

    linuxX64()
    linuxArm64()
    mingwX64()

    if (getHost() == Host.MAC) {
        macosX64()
        macosArm64()

        iosX64()
        iosArm64()
        iosSimulatorArm64()

        tvosX64()
        tvosArm64()
        tvosSimulatorArm64()

        watchosX64()
        watchosArm32()
        watchosArm64()
        watchosSimulatorArm64()
        watchosDeviceArm64()
    }

    androidTarget {
        publishAllLibraryVariants()
    }
    jvm()

    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()
}

android {
    compileSdk = Configuration.compileSdk
    namespace = artifact

    defaultConfig {
        minSdk = Configuration.minSdk
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    val publishId = "annotations"

    coordinates(
        groupId = artifact,
        artifactId = publishId,
        version = libVersion
    )

    pom {
        name.set(publishId)
        description.set("Sekret Annotations")
        url.set("https://github.com/DatL4g/Sekret")
        inceptionYear.set("2024")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        scm {
            url.set("https://github.com/DatL4g/Sekret")
            connection.set("scm:git:git://github.com/DATL4G/Sekret.git")
            developerConnection.set("scm:git:git://github.com/DATL4G/Sekret.git")
        }

        developers {
            developer {
                id.set("DatL4g")
                name.set("Jeff Retz")
                url.set("https://github.com/DatL4g")
            }
        }
    }
}

fun getHost(): Host {
    return when (osdetector.os) {
        "linux" -> Host.Linux
        "osx" -> Host.MAC
        "windows" -> Host.Windows
        else -> {
            val hostOs = System.getProperty("os.name")
            val isMingwX64 = hostOs.startsWith("Windows")

            when {
                hostOs == "Linux" -> Host.Linux
                hostOs == "Mac OS X" -> Host.MAC
                isMingwX64 -> Host.Windows
                else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
            }
        }
    }
}

enum class Host(val label: String) {
    Linux("linux"),
    Windows("win"),
    MAC("mac");
}