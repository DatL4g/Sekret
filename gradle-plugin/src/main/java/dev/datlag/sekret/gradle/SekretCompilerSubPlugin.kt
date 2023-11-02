package dev.datlag.sekret.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

internal class SekretCompilerSubPlugin : KotlinCompilerPluginSupportPlugin {

    private lateinit var project: Project
    private var gradleExtension: SekretGradleConfiguration = SekretGradleConfiguration()

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        gradleExtension = kotlinCompilation.target.project.extensions.findByType(SekretGradleConfiguration::class.java)
            ?: SekretGradleConfiguration()

        return kotlinCompilation.target.project.provider {
            listOf(
                SubpluginOption("packageName", gradleExtension.packageName.toString()),
                SubpluginOption("khashVersion", gradleExtension.khashVersion.toString()),
                SubpluginOption("recreateGeneratedFiles", gradleExtension.recreateGeneratedFiles.toString()),
                SubpluginOption("propertiesFile", gradleExtension.propertiesFile.toString()),
                SubpluginOption("generateCommonSourceFile", gradleExtension.generateCommonSourceFile.toString())
            )
        }
    }

    private fun Project.getSekretConfig() = this.extensions.findByType(SekretGradleConfiguration::class.java)
        ?: SekretGradleConfiguration()

    override fun apply(target: Project) {
        project = target

        super.apply(target)
    }

    override fun getCompilerPluginId(): String = COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = SERIALIZATION_GROUP_NAME,
            artifactId = ARTIFACT_NAME
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    companion object {
        const val SERIALIZATION_GROUP_NAME = "dev.datlag.sekret"
        const val ARTIFACT_NAME = "compiler-plugin"
        const val COMPILER_PLUGIN_ID = "sekretPlugin"
    }
}