package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPlugin
import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.*
import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.common.targetsMapped
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import javax.inject.Inject

open class GenerateSekretBuildScriptTask : DefaultTask() {

    @get:Input
    open val enabled: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    open val packageName: Property<String> = project.objects.property(String::class.java)

    @get:Input
    open val targets: SetProperty<Target> = project.objects.setProperty(Target::class.java)

    @get:Input
    open val hasVersionCatalogs: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    open val usesVersionCatalogForPlugin: Property<Boolean> = project.objects.property(Boolean::class.java)

    @get:Input
    open val versionCatalogName: Property<String> = project.objects.property(String::class.java)

    @get:Input
    open val versionCatalogLibraryAlias: Property<String> = project.objects.property(String::class.java)

    @get:OutputDirectory
    open val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    open val projectLayout: ProjectLayout = project.layout

    private val outputDir: File
        get() = outputDirectory.asFile.orNull
            ?: projectLayout.projectDirectory.dir("sekret").asFile

    init {
        group = "sekret"
        description = "Generates buildscript for the sekret module, depending on the targets used in the source project."
    }

    @TaskAction
    fun generate() {
        if (!enabled.getOrElse(false)) {
            return
        }

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

        val hasVersionCatalogs = hasVersionCatalogs.getOrElse(false) ?: false
        val usesVersionCatalogForPlugin = usesVersionCatalogForPlugin.getOrElse(false) ?: false

        if (hasVersionCatalogs && !usesVersionCatalogForPlugin) {
            logger.log(LogLevel.WARN, "Seems like you use version catalogs but not for the sekret plugin.")
        }

        BuildFileGenerator.generate(
            targets = requiredTargets,
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            versionCatalogSekretDependency = versionCatalogLibraryAlias.orNull?.ifBlank { null }?.let { lib ->
                versionCatalogName.orNull?.ifBlank { null }?.let { catalog ->
                    "${catalog}.${lib}"
                }
            },
            outputDir = ModuleGenerator.createBase(outputDir),
            overwrite = true
        )
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        packageName.set(extension.properties.packageName)

        // Provider values are resolved lazily
        targets.set(project.provider {
            project.targetsMapped
        })
        hasVersionCatalogs.set(project.provider {
            project.hasVersionCatalogs
        })
        usesVersionCatalogForPlugin.set(project.provider {
            project.hasSekretVersionCatalog
        })
        versionCatalogName.set(project.provider {
            project.sekretVersionCatalog?.name
        })
        versionCatalogLibraryAlias.set(project.provider {
            project.sekretVersionCatalog?.sekretLibraryAlias
        })

        outputDirectory.set(project.findProject("sekret")?.projectDir ?: File(project.projectDir, "sekret"))
    }

    companion object {
        internal const val NAME = "generateSekretBuildScript"
    }
}