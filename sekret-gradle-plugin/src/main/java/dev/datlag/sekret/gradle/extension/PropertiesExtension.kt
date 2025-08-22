package dev.datlag.sekret.gradle.extension

import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.common.isDirectorySafely
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.internal.Actions
import org.gradle.util.internal.ConfigureUtil

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
     * Change where the google-services.json file for your secrets is located.
     * Default is "google-services.json" in the **androidMain** target of the module the plugin is applied.
     */
    open val googleServicesFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Change where the YAML file for your secrets is located.
     * Default is "sekret.yaml" in the module the plugin is applied.
     */
    open val yamlFile: RegularFileProperty = objectFactory.fileProperty()

    /**
     * Configuration for handling copy process of native binaries.
     */
    lateinit var nativeCopy: NativeCopyExtension
        private set

    /**
     * Change how the copy process of native binaries are handled.
     */
    fun nativeCopy(closure: Closure<in NativeCopyExtension>): NativeCopyExtension {
        return nativeCopy(ConfigureUtil.configureUsing(closure))
    }

    /**
     * Change how the copy process of native binaries are handled.
     */
    fun nativeCopy(action: Action<in NativeCopyExtension>): NativeCopyExtension {
        return Actions.with(nativeCopy, action)
    }

    internal fun setupConvention(project: Project) {
        enabled.convention(false)
        packageName.convention(project.provider {
            project.group.toString().ifBlank { sekretPackageName }
        })
        encryptionKey.convention(packageName)
        propertiesFile.convention(project.provider {
            resolveFile(project.layout.projectDirectory.file(sekretFileName))
        })
        googleServicesFile.convention(project.provider {
            resolveFile(project.layout.projectDirectory.file("src/androidMain/$googleServicesFileName"))
                ?: resolveFile(project.layout.projectDirectory.file("src/main/$googleServicesFileName"))
                ?: resolveFile(project.layout.projectDirectory.file(googleServicesFileName))
        })
        yamlFile.convention(project.provider {
            resolveFile(project.layout.projectDirectory.file(yamlFileName))
                ?: resolveFile(project.layout.projectDirectory.file(yamlAlternativeFileName))
        })

        nativeCopy = NativeCopyExtension(project.objects).also {
            it.setupConvention(project)
        }
    }

    open class NativeCopyExtension(objectFactory: ObjectFactory) {

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
            androidJNIFolder.convention(project.provider {
                resolveFolder(project.layout.projectDirectory.dir("src/androidMain/jniLibs"))
                    ?: resolveFolder(project.layout.projectDirectory.dir("src/main/jniLibs"))
            })
            desktopComposeResourcesFolder.convention(project.provider {
                resolveFolder(project.layout.projectDirectory.dir("src/jvmMain/resources"))
                    ?: resolveFolder(project.layout.projectDirectory.dir("src/desktopMain/resources"))
            })
        }
    }

    companion object {
        internal const val sekretFileName = "sekret.properties"
        internal const val sekretPackageName = "dev.datlag.sekret"
        internal const val googleServicesFileName = "google-services.json"
        internal const val yamlFileName = "sekret.yaml"
        internal const val yamlAlternativeFileName = "sekret.yml"

        private fun resolveFolder(dir: Directory): Directory? {
            return if (dir.asFile.existsSafely() && dir.asFile.isDirectorySafely()) {
                dir
            } else {
                null
            }
        }

        private fun resolveFile(file: RegularFile): RegularFile? {
            return if (file.asFile.existsSafely()) {
                file
            } else {
                null
            }
        }
    }
}