package dev.datlag.sekret.gradle

open class SekretGradleConfiguration {

    /**
     * Specify the package name, the secrets are available under
     */
    var packageName: String = "dev.datlag.sekret"

    /**
     * Specify the key, the secrets will be encrypted with
     */
    var password: String? = null

    /**
     * The properties file (location), to get the key - value pairs from
     */
    var propertiesFile: String = "sekret.properties"
}