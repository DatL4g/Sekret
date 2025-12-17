import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.osdetector)
    alias(libs.plugins.serialization)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

kotlin {
    androidNativeX86()
    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()

    val jniTargets = mutableListOf(
        linuxX64(),
        linuxArm64(),
        mingwX64()
    )

    if (getHost() == Host.MAC) {
        jniTargets.add(
            macosX64()
        )
        jniTargets.add(
            macosArm64()
        )
    }

    jniTargets.forEach { target ->
        target.compilations.getByName("main") {
            cinterops {
                create("sekret") {
                    val javaDefaultHome = System.getProperty("java.home")
                    val javaEnvHome = getSystemJavaHome()
                    val javaSdkMan = getSdkManJava()
                    val javaDefaultRuntime = getDefaultRuntimeJava()

                    packageName("dev.datlag.sekret")

                    includeDirs.allHeaders(
                        // Gradle or IDE specified Java Home
                        File(javaDefaultHome, "include"),
                        File(javaDefaultHome, "include/darwin"),
                        File(javaDefaultHome, "include/linux"),
                        File(javaDefaultHome, "include/win32"),

                        // System set Java Home
                        File(javaEnvHome, "include"),
                        File(javaEnvHome, "include/darwin"),
                        File(javaEnvHome, "include/linux"),
                        File(javaEnvHome, "include/win32"),

                        // SDK Man Java
                        File(javaSdkMan, "include"),
                        File(javaSdkMan, "include/darwin"),
                        File(javaSdkMan, "include/linux"),
                        File(javaSdkMan, "include/win32"),

                        // Default Runtime Java
                        File(javaDefaultRuntime, "include"),
                        File(javaDefaultRuntime, "include/darwin"),
                        File(javaDefaultRuntime, "include/linux"),
                        File(javaDefaultRuntime, "include/win32"),
                    )
                }
            }
        }
    }

    if (getHost() == Host.MAC) {
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

    // non-native targets
    androidLibrary {
        compileSdk = Configuration.compileSdk
        minSdk = Configuration.minSdk
        namespace = artifact
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

    sourceSets {
        commonMain.dependencies {
            api(project(":sekret-annotations"))
            implementation(libs.serialization)
        }

        val jniNativeMain by creating {
            linuxMain.get().dependsOn(this)
            mingwMain.get().dependsOn(this)

            macosMain.orNull?.dependsOn(this)
        }

        val jniNativeCommonMain by creating {
            dependsOn(nativeMain.get())

            androidNativeMain.get().dependsOn(this)
            jniNativeMain.dependsOn(this)
        }

        androidMain.dependencies {
            implementation(libs.relinker)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    val publishId = "sekret"

    coordinates(
        groupId = artifact,
        artifactId = publishId,
        version = libVersion
    )

    pom {
        name.set(publishId)
        description.set("Deeply hide secrets with Kotlin Multiplatform")
        url.set("https://github.com/DatL4g/Sekret")
        inceptionYear.set("2023")

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

fun getSystemJavaHome(): String? {
    return System.getenv("JAVA_HOME")?.ifBlank { null }
}

fun getSdkManJava(): String? {
    return System.getProperty("user.home")?.ifBlank { null }?.let {
        "$it/.sdkman/candidates/java/current"
    }
}

fun getDefaultRuntimeJava(): String? {
    return "/usr/lib/jvm/default-runtime"
}

enum class Host(val label: String) {
    Linux("linux"),
    Windows("win"),
    MAC("mac");
}