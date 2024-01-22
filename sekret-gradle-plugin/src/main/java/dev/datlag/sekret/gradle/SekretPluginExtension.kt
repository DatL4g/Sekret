package dev.datlag.sekret.gradle

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * Configure the sekret gradle plugin to match your needs.
 */
abstract class SekretPluginExtension {
    /**
     * Change the package name, the Sekret files will be available under.
     * Default is your specified group where the plugin is applied and falls back to "dev.datlag.sekret" if none found.
     */
    abstract val packageName: Property<String>

    /**
     * Change the key which will be used to encrypt your secrets.
     * Default is your provided [packageName].
     */
    abstract val encryptionKey: Property<String>

    /**
     * Change where the properties file for your secrets is located.
     * Default is "sekret.properties" in the module the plugin is applied.
     */
    abstract val propertiesFile: RegularFileProperty

    /**
     * Change whether the generated "sekret" module should be available in top projects.
     * * if true: the module is added with "api"
     * * if false: the module is added with "implementation"
     */
    abstract val exposeModule: Property<Boolean>

    /**
     * Set up the path where the compiled native library for android should be copied to.
     * * If not set the binary won't be copied.
     *
     * Default jni location for android is **src/main/jniLibs** or **src/androidMain/jniLibs**, depending on your setup.
     */
    abstract val androidJNIFolder: DirectoryProperty

    /**
     * Set up the path where the compiled native library for desktop compose applications should be copied to.
     * * If not set the binary won't be copied.
     *
     * Default location you should use is resources (**NOT** src/main/resources).
     *
     * Take a look on how to configure here: [Adding files to packaged application](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution#adding-files-to-packaged-application)
     */
    abstract val desktopComposeResourcesFolder: DirectoryProperty

    companion object {
        internal const val sekretFileName = "sekret.properties"
        internal const val sekretPackageName = "dev.datlag.sekret"
    }
}

internal fun SekretPluginExtension.setupConvention(project: Project) {
    packageName.convention(project.provider {
        project.group.toString().ifBlank { SekretPluginExtension.sekretPackageName }
    })
    encryptionKey.convention(packageName)
    propertiesFile.convention(project.layout.projectDirectory.file(SekretPluginExtension.sekretFileName))
    exposeModule.convention(false)
}