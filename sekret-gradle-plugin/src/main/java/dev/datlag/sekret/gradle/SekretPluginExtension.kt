package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.extension.ObfuscationExtension
import dev.datlag.sekret.gradle.extension.PropertiesExtension
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.internal.Actions
import org.gradle.util.internal.ConfigureUtil

/**
 * Configure the sekret gradle plugin to match your needs.
 */
open class SekretPluginExtension {

    /**
     * Configuration for obfuscation.
     */
    lateinit var obfuscation: ObfuscationExtension
        internal set

    /**
     * Configuration to generate secrets from properties.
     */
    lateinit var properties: PropertiesExtension
        internal set

    fun obfuscation(closure: Closure<in ObfuscationExtension>): ObfuscationExtension {
        return obfuscation(ConfigureUtil.configureUsing(closure))
    }

    fun obfuscation(action: Action<in ObfuscationExtension>): ObfuscationExtension {
        return Actions.with(obfuscation, action)
    }

    fun properties(closure: Closure<in PropertiesExtension>): PropertiesExtension {
        return properties(ConfigureUtil.configureUsing(closure))
    }

    fun properties(action: Action<in PropertiesExtension>): PropertiesExtension {
        return Actions.with(properties, action)
    }

    internal fun setupConvention(project: Project) {
        obfuscation = ObfuscationExtension(project.objects).also {
            it.setupConvention(project)
        }
        properties = PropertiesExtension(project.objects).also {
            it.setupConvention(project)
        }
    }
}