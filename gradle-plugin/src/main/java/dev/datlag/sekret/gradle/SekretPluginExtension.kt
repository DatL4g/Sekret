package dev.datlag.sekret.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class SekretPluginExtension {
    abstract val packageName: Property<String>
    abstract val encryptionKey: Property<String>
    abstract val propertiesFile: Property<String>
    abstract val jsSourceSet: Property<Boolean>
    abstract val exposeModule: Property<Boolean>
    abstract val androidJNIFolder: Property<String?>
    abstract val desktopComposeResourceFolder: Property<String?>

    companion object {
        internal const val sekretFileName = "sekret.properties"
        internal const val sekretPackageName = "dev.datlag.sekret"
    }
}

internal fun SekretPluginExtension.setupConvention(project: Project) {
    packageName.convention(project.provider { project.group.toString() })
    encryptionKey.convention(packageName)
    propertiesFile.convention(SekretPluginExtension.sekretFileName)
    jsSourceSet.convention(false)
    exposeModule.convention(false)
    androidJNIFolder.convention(null)
    desktopComposeResourceFolder.convention(null)
}