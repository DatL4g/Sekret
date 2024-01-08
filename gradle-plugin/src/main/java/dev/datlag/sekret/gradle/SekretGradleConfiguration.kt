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

    /**
     * Specify if the JS target should be added or not, default tries to read your source module.
     */
    var generateJsSourceSet: Boolean? = null

    /**
     * Specify if the generated sekret module should be added using *implementation* or *api*.
     *
     * * true = api
     * * false = implementation
     */
    var exposeModule: Boolean = false

    /**
     * Specify the android JNI root folder where the built native library will be copied to.
     */
    var androidJniFolder: String? = null

    /**
     * Specify the desktop compose resource root folder where the built native library will be copied to.
     */
    var desktopComposeResourceFolder: String? = null
}