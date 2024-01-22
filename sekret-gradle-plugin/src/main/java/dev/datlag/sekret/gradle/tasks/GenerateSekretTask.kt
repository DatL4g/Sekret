package dev.datlag.sekret.gradle.tasks

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
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateSekretTask : DefaultTask() {

    init {
        group = "sekret"
    }

    @TaskAction
    fun generate() {
        val config = project.sekretExtension.properties
        if (!config.enabled.getOrElse(false)) {
            return
        }

        val sekretDir = ModuleGenerator.createBase(project)
        BuildFileGenerator.generate(
            project = project,
            overwrite = false
        )

        val defaultTargets = project.targetsMapped
        val requiredTargets = Target.addDependingTargets(defaultTargets)

        val structure = ModuleGenerator.createForTargets(
            directory = sekretDir,
            targets = requiredTargets
        )

        val propFile = propertiesFile(config) ?: throw IllegalStateException("No sekret properties file found.")
        val properties = Utils.propertiesFromFile(propFile)

        val generator = SekretGenerator.createAllForTargets(
            packageName = config.packageName.getOrElse(PropertiesExtension.sekretPackageName),
            structure = structure
        )

        val encodedProperties = Encoder.encodeProperties(properties, config.encryptionKey.get())
        SekretGenerator.generate(encodedProperties, *generator.toTypedArray())
    }

    private fun propertiesFile(config: PropertiesExtension): File? {
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

    companion object {
        internal const val NAME = "generateSekret"
    }
}