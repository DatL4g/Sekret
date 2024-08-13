package dev.datlag.sekret.gradle.tasks

import dev.datlag.sekret.gradle.SekretPlugin
import dev.datlag.sekret.gradle.SekretPluginExtension
import dev.datlag.sekret.gradle.Target
import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.common.targetsMapped
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.generator.BuildFileGenerator
import dev.datlag.sekret.gradle.generator.ModuleGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class GenerateSekretBuildScriptTask : DefaultTask() {

    @get:Input
    abstract val enabled: Property<Boolean>

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val targets: SetProperty<Target>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val projectLayout: ProjectLayout

    private val outputDir: File
        get() = outputDirectory.asFile.orNull
            ?: projectLayout.projectDirectory.dir("sekret").asFile

    init {
        group = "sekret"
    }

    @TaskAction
    fun generate() {
        if (!enabled.getOrElse(false)) {
            return
        }
        BuildFileGenerator.generate(
            targets = Target.addDependingTargets(targets.get()),
            packageName = packageName.getOrElse(PropertiesExtension.sekretPackageName),
            outputDir = ModuleGenerator.createBase(outputDir),
            overwrite = true
        )
    }

    fun apply(project: Project, extension: SekretPluginExtension = project.sekretExtension) {
        enabled.set(extension.properties.enabled)
        packageName.set(extension.properties.packageName)
        targets.set(project.targetsMapped)
        outputDirectory.set(project.findProject("sekret")?.projectDir ?: File(project.projectDir, "sekret"))
    }

    companion object {
        internal const val NAME = "generateSekretBuildScript"
    }
}