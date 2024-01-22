package dev.datlag.sekret.gradle.extension

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Used for generating (native binary) secrets from properties file.
 */
open class PropertiesExtension(objectFactory: ObjectFactory) {
    /**
     * Set whether secret generation from properties is enabled or not.
     * Default: false
     */
    open val enabled: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * Change the package name, the Sekret files will be available under.
     * Default is your specified group where the plugin is applied and falls back to "dev.datlag.sekret" if none found.
     */
    open val packageName: Property<String> = objectFactory.property(String::class.java)

    /**
     * Change the key which will be used to encrypt your secrets.
     * Default is your provided [packageName].
     */
    open val encryptionKey: Property<String> = objectFactory.property(String::class.java)

    /**
     * Change where the properties file for your secrets is located.
     * Default is "sekret.properties" in the module the plugin is applied.
     */
    open val propertiesFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Change whether the generated "sekret" module should be available in top projects.
     * * if true: the module is added with "api"
     * * if false: the module is added with "implementation"
     */
    open val exposeModule: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * Set up the path where the compiled native library for android should be copied to.
     * * If not set the binary won't be copied.
     *
     * Default jni location for android is **src/main/jniLibs** or **src/androidMain/jniLibs**, depending on your setup.
     */
    open val androidJNIFolder: DirectoryProperty = objectFactory.directoryProperty()

    /**
     * Set up the path where the compiled native library for desktop compose applications should be copied to.
     * * If not set the binary won't be copied.
     *
     * Default location you should use is resources (**NOT** src/main/resources).
     *
     * Take a look on how to configure here: [Adding files to packaged application](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution#adding-files-to-packaged-application)
     */
    open val desktopComposeResourcesFolder: DirectoryProperty = objectFactory.directoryProperty()

    internal fun setupConvention(project: Project) {
        enabled.convention(false)
        packageName.convention(project.provider {
            project.group.toString().ifBlank { PropertiesExtension.sekretPackageName }
        })
        encryptionKey.convention(packageName)
        propertiesFile.convention(project.layout.projectDirectory.file(PropertiesExtension.sekretFileName))
        exposeModule.convention(false)
    }

    companion object {
        internal const val sekretFileName = "sekret.properties"
        internal const val sekretPackageName = "dev.datlag.sekret"
    }
}