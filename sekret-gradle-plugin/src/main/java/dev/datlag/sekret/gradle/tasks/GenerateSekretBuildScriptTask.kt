package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPlugin
import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.allTargets
import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.common.sourceSets
import dev.datlag.sekret.gradle.common.targetsMapped
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
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
    open val sourceSets: SetProperty<String> = project.objects.setProperty(String::class.java)

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

        val allTargets = listOf(
            targets.get(),
            Target.fromSourceSetNames(sourceSets.get())
        ).flatten().filterNotNull()

        BuildFileGenerator.generate(
            targets = Target.addDependingTargets(allTargets),
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            outputDir = ModuleGenerator.createBase(outputDir),
            overwrite = true
        )
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        packageName.set(extension.properties.packageName)
        targets.set(project.provider {
            project.targetsMapped
        })
        sourceSets.set(project.provider {
            project.sourceSets.map { it.name }
        })
        outputDirectory.set(project.findProject("sekret")?.projectDir ?: File(project.projectDir, "sekret"))
    }

    companion object {
        internal const val NAME = "generateSekretBuildScript"
    }
}