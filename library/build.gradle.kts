import com.vanniktech.maven.publish.SonatypeHost

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
        target.compilations["main"].cinterops.create("sekret") {
            val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
            packageName = "dev.datlag.sekret"

            includeDirs(
                Callable { File(javaHome, "include") },
                Callable { File(javaHome, "include/darwin") },
                Callable { File(javaHome, "include/linux") },
                Callable { File(javaHome, "include/win32") }
            )
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
    androidTarget()
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting

        val androidNativeX86Main by getting
        val androidNativeX64Main by getting
        val androidNativeArm32Main by getting
        val androidNativeArm64Main by getting

        val androidNativeMain by creating {
            androidNativeX86Main.dependsOn(this)
            androidNativeX64Main.dependsOn(this)
            androidNativeArm32Main.dependsOn(this)
            androidNativeArm64Main.dependsOn(this)
        }

        val linuxX64Main by getting
        val linuxArm64Main by getting
        val mingwX64Main by getting

        val macosX64Main = findByName("macosX64Main")
        val macosArm64Main = findByName("macosArm64Main")

        val jniNativeMain by creating {
            linuxX64Main.dependsOn(this)
            linuxArm64Main.dependsOn(this)
            mingwX64Main.dependsOn(this)

            macosX64Main?.dependsOn(this)
            macosArm64Main?.dependsOn(this)
        }

        val jniNativeCommonMain by creating {
            androidNativeMain.dependsOn(this)
            jniNativeMain.dependsOn(this)
        }

        val iosX64Main = findByName("iosX64Main")
        val iosArm64Main = findByName("iosArm64Main")
        val iosSimulatorArm64Main = findByName("iosSimulatorArm64Main")

        val tvosX64Main = findByName("tvosX64Main")
        val tvosArm64Main = findByName("tvosArm64Main")
        val tvosSimulatorArm64Main = findByName("tvosSimulatorArm64Main")

        val watchosX64Main = findByName("watchosX64Main")
        val watchosArm32Main = findByName("watchosArm32Main")
        val watchosArm64Main = findByName("watchosArm64Main")
        val watchosSimulatorArm64Main = findByName("watchosSimulatorArm64Main")
        val watchosDeviceArm64Main = findByName("watchosDeviceArm64Main")

        val nativeMain by creating {
            dependsOn(commonMain)

            jniNativeCommonMain.dependsOn(this)

            iosX64Main?.dependsOn(this)
            iosArm64Main?.dependsOn(this)
            iosSimulatorArm64Main?.dependsOn(this)

            tvosX64Main?.dependsOn(this)
            tvosArm64Main?.dependsOn(this)
            tvosSimulatorArm64Main?.dependsOn(this)

            watchosX64Main?.dependsOn(this)
            watchosArm32Main?.dependsOn(this)
            watchosArm64Main?.dependsOn(this)
            watchosSimulatorArm64Main?.dependsOn(this)
            watchosDeviceArm64Main?.dependsOn(this)
        }

        // non-native sourceSets
        val androidMain by getting {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependsOn(commonMain)
        }
    }
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
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()

    val publishId = "library"

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

enum class Host(val label: String) {
    Linux("linux"),
    Windows("win"),
    MAC("mac");
}