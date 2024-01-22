import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.publish)
}

val artifact = VersionCatalog.artifactName()
group = artifact
version = libVersion

dependencies {
    compileOnly(libs.auto.service)
    kapt(libs.auto.service)

    compileOnly(libs.kotlin.compiler.embeddable)
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.S01, automaticRelease = true)
    signAllPublications()

    val publishId = "compiler-plugin"

    coordinates(
        groupId = artifact,
        artifactId = publishId,
        version = libVersion
    )

    pom {
        name.set(publishId)
        description.set("Compiler plugin for sekret annotations")
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