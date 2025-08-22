package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.*
import dev.datlag.sekret.gradle.common.canReadSafely
import dev.datlag.sekret.gradle.common.existsSafely
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.generator.native.NativeGenerator
import dev.datlag.sekret.gradle.generator.native.NativeJNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JSGenerator
import dev.datlag.sekret.gradle.helper.Encoder
import dev.datlag.sekret.gradle.helper.Utils
import dev.datlag.sekret.gradle.model.GoogleServices
import dev.datlag.sekret.gradle.model.YamlConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class GenerateSekretTask : DefaultTask() {

    @get:Input
    open val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    open val packageName: Property<String> = project.objects.property(String::class.java)

    @get:Input
    open val targets: SetProperty<Target> = project.objects.setProperty(Target::class.java)

    @get:Input
    open val encryptionKey: Property<String> = project.objects.property(String::class.java)

    @get:OutputDirectory
    open val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    open val projectLayout: ProjectLayout = project.layout

    @get:Optional
    @get:InputFile
    open val propertiesFile: RegularFileProperty = project.objects.fileProperty()

    @get:Optional
    @get:InputFile
    open val googleServicesFile: RegularFileProperty = project.objects.fileProperty()

    @get:Optional
    @get:InputFile
    open val yamlFile: RegularFileProperty = project.objects.fileProperty()

    private val outputDir: File
        get() = outputDirectory.asFile.orNull
            ?: projectLayout.projectDirectory.dir("sekret").asFile

    init {
        group = "sekret"
        description = "Generates required source files for the sekret module, including the buildscript if not present."
    }

    @TaskAction
    fun generate() {
        if (!enabled.getOrElse(false)) {
            return
        }

        val sekretDir = ModuleGenerator.createBase(outputDir)
        val usedTargets = targets.get()
        val requiredTargets = Target.addDependingTargets(usedTargets)
        val logLevel = if (usedTargets.size <= 1) {
            LogLevel.WARN
        } else {
            LogLevel.INFO
        }

        logger.log(logLevel, "Following targets in use detected: ${usedTargets.joinToString { it.name }}.")
        logger.log(logLevel, "Following targets are used/required depending on your configuration: ${requiredTargets.joinToString { it.name }}.")
        logger.log(logLevel, "Please report if you encounter any missing target.")

        BuildFileGenerator.generate(
            targets = requiredTargets,
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            outputDir = sekretDir,
            overwrite = false
        )

        val structure = ModuleGenerator.createForTargets(
            directory = sekretDir,
            targets = requiredTargets
        )

        val propFile = propertiesFile(propertiesFile.orNull)
        val googleServicesFile = googleServicesFile(googleServicesFile.orNull)
        val yamlFile = yamlFile(yamlFile.orNull)

        if (propFile == null && googleServicesFile == null && yamlFile == null) {
            throw IllegalStateException("No sekret.properties file, google-services.json or sekret.yaml file found.")
        }

        val properties = propFile?.let(Utils::propertiesFromFile)
        val googleServices = googleServicesFile?.let { GoogleServices.from(it, logger) }
        val yamlConfig = yamlFile?.let { YamlConfig.from(it, logger) }

        if (properties == null && googleServices == null && yamlConfig == null) {
            throw IllegalStateException("Neither of your provided properties, google-services or yaml could be parsed.")
        }

        val generator = SekretGenerator.createAllForTargets(
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            structure = structure
        )

        val encodedProperties = Encoder.encodeProperties(properties, googleServices, yamlConfig, encryptionKey.get())
        SekretGenerator.generate(encodedProperties, generator)
    }

    private fun propertiesFile(file: RegularFile?): File? {
        val defaultName = PropertiesExtension.sekretFileName

        return file?.asFile?.let { resolveFile(it, defaultName) }
    }

    private fun googleServicesFile(file: RegularFile?): File? {
        val defaultName = PropertiesExtension.googleServicesFileName

        return file?.asFile?.let { resolveFile(it, defaultName) }
    }

    private fun yamlFile(file: RegularFile?): File? {
        val defaultName = PropertiesExtension.yamlFileName
        val alternativeName = PropertiesExtension.yamlAlternativeFileName

        return file?.asFile?.let { resolveFile(it, defaultName) } ?: file?.asFile?.let { resolveFile(it, alternativeName) }
    }

    private fun resolveFile(file: File, defaultName: String): File? {
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

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        packageName.set(extension.properties.packageName)

        // Provider values are resolved lazily
        targets.set(project.provider {
            project.targetsMapped
        })

        encryptionKey.set(extension.properties.encryptionKey)
        outputDirectory.set(project.findProject("sekret")?.projectDir ?: File(project.projectDir, "sekret"))
        propertiesFile.set(extension.properties.propertiesFile)
        googleServicesFile.set(extension.properties.googleServicesFile)
        yamlFile.set(extension.properties.yamlFile)
    }

    companion object {
        internal const val NAME = "generateSekret"
    }
}