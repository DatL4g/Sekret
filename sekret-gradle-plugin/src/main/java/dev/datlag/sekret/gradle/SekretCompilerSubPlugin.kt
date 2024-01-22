package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.common.sekretExtension
import dev.datlag.sekret.gradle.extension.ObfuscationExtension
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class SekretCompilerSubPlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val config = kotlinCompilation.target.project.sekretExtension.obfuscation

        return kotlinCompilation.target.project.provider {
            listOf(
                SubpluginOption("secretMask", config.secretMask.getOrElse(ObfuscationExtension.DEFAULT_SECRET_MASK)),
                SubpluginOption("secretMaskNull", config.secretMaskNull.getOrElse(ObfuscationExtension.DEFAULT_SECRET_MAK_NULL).toString())
            )
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

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    companion object {
        private const val GROUP_NAME = "dev.datlag.sekret"
        private const val ARTIFACT = "compiler-plugin"
        private const val PLUGIN_ID = "sekretPlugin"
    }
}