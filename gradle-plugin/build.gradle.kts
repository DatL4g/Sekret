import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    id("java-gradle-plugin")
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.kotlin.gradle.plugin.api)
    implementation(libs.kotlin.poet)
    implementation(libs.kase.change)
}

gradlePlugin {
    plugins {
        website.set("https://github.com/DatL4g/Sekret")
        vcsUrl.set("https://github.com/DatL4g/Sekret")

        create("sekretPlugin") {
            id = artifact
            implementationClass = "dev.datlag.sekret.gradle.SekretGradlePlugin"
            displayName = "Sekret Gradle Plugin"
            description = "Gradle Plugin for Sekret"
            tags.set(listOf("kotlin", "secret", "hidden"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()

    val publishId = "sekret-gradle-plugin"

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