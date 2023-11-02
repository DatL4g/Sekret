package dev.datlag.sekret.gradle

open class SekretGradleConfiguration {

    /**
     * Specify the package name, the secrets are available under
     */
    var packageName: String = "dev.datlag.sekret"

    /**
     * The version must be either available through mavenCentral or jitpack.io
     *
     * [KHash GitHub Repository](https://github.com/komputing/KHash)
     */
    var khashVersion: String = "1.1.3"

    /**
     * The generated file will be deleted and created again, meaning all edits by users will be lost
     */
    var recreateGeneratedFiles: Boolean = true

    /**
     * The properties file (location), to get the key - value pairs from
     */
    var propertiesFile: String = "sekret.properties"
}