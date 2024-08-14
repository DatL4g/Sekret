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
import dev.datlag.sekret.gradle.helper.Encoder
import dev.datlag.sekret.gradle.helper.Utils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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

    @get:InputFile
    open val propertiesFile: RegularFileProperty = project.objects.fileProperty()

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
        val requiredTargets = Target.addDependingTargets(targets.get())
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

        val propFile = propertiesFile.asFile.orNull ?: throw IllegalStateException("No sekret properties file found.")
        val properties = Utils.propertiesFromFile(propFile)

        val generator = SekretGenerator.createAllForTargets(
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            structure = structure
        )

        val encodedProperties = Encoder.encodeProperties(properties, encryptionKey.get())
        SekretGenerator.generate(encodedProperties, *generator.toTypedArray())
    }

    private fun propertiesFile(project: Project, config: PropertiesExtension): File? {
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

        return resolveFile(config.propertiesFile.asFile.getOrElse(project.file(PropertiesExtension.sekretFileName)))
            ?: resolveFile(project.projectDir)
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        packageName.set(extension.properties.packageName)
        targets.set(project.targetsMapped)
        encryptionKey.set(extension.properties.encryptionKey)
        outputDirectory.set(project.findProject("sekret")?.projectDir ?: File(project.projectDir, "sekret"))
        propertiesFile.set(propertiesFile(project, extension.properties))
    }

    companion object {
        internal const val NAME = "generateSekret"
    }
}