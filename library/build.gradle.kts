import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

kotlin {
    androidTarget()
    androidNativeX86()
    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()

    jvm()

    linuxX64()
    linuxArm64()

    mingwX64()

    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting
    }
}

android {
    compileSdk = 21
    namespace = artifact
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