package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.common.canReadSafely
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.common.isDirectorySafely
import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.helper.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CreateSekretValueTask : DefaultTask() {

    @get:Input
    open val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:OutputFile
    open val propertiesFile: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    open val key: Property<String> = project.objects.property(String::class.java)

    @get:Input
    open val value: Property<String> = project.objects.property(String::class.java)

    init {
        group = "sekret"
        description = "Adds a key-value based pair to your properties file."
    }

    @TaskAction
    fun edit() {
        if (!enabled.getOrElse(false)) {
            return
        }

        val name = key.orNull ?: throw IllegalArgumentException("Missing property 'key'")
        val data = value.orNull ?: throw IllegalArgumentException("Missing property 'value'")
        val propFile = propertiesFile.orNull?.asFile ?: throw IllegalStateException("No sekret properties file found.")
        val properties = Utils.propertiesFromFile(propFile)

        properties[name] = data

        Utils.saveProperties(properties, propFile)
    }

    private fun propertiesFile(
        project: Project,
        config: PropertiesExtension
    ): File? {
        val defaultName = PropertiesExtension.sekretFileName

        fun resolveFile(file: File): File? {
            if (file.existsSafely() && file.canReadSafely()) {
                val sekretFile = if (file.isDirectorySafely()) {
                    File(file, defaultName)
                } else {
                    file
                }

                if (sekretFile.existsSafely() && sekretFile.canReadSafely()) {
                    return sekretFile
                }
            }
            return null
        }

        return resolveFile(config.propertiesFile.asFile.getOrElse(project.projectDir))
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        propertiesFile.set(
            propertiesFile(
                project,
                extension.properties
            )
        )
        key.set(project.findProperty("key")?.toString()?.ifBlank { null })
        value.set(project.findProperty("value")?.toString()?.ifBlank { null })
    }

    companion object {
        internal const val NAME = "createSekretValue"
    }
}