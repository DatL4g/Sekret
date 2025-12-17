package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.common.sekretExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class SekretCompilerSubPlugin : KotlinCompilerPluginSupportPlugin {

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            val config = kotlinCompilation.target.project.sekretExtension.obfuscation

            val fullList = mutableListOf(
                SubpluginOption("secretMask", config.secretAnnotation.mask.get()),
                SubpluginOption("secretMaskNull", config.secretAnnotation.maskNull.get().toString()),
            ).apply {
                val seed = config.obfuscateAnnotation.seed.orNull

                if (seed != null) {
                    add(SubpluginOption("obfuscateSeed", seed.toString()))
                }
            }
            fullList
        }
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = GROUP_NAME,
            artifactId = ARTIFACT,
            version = SekretPlugin.getVersion()
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val config = kotlinCompilation.target.project.sekretExtension.obfuscation

        return config.enabled.getOrElse(true)
    }

    companion object {
        private const val GROUP_NAME = "dev.datlag.sekret"
        private const val ARTIFACT = "compiler-plugin"
        private const val PLUGIN_ID = "sekretPlugin"
    }
}