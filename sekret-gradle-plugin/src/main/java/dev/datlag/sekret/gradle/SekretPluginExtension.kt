package dev.datlag.sekret.gradle

import dev.datlag.sekret.gradle.extension.PropertiesExtension
import dev.datlag.sekret.gradle.extension.setupConvention
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.internal.Actions
import org.gradle.util.internal.ConfigureUtil

/**
 * Configure the sekret gradle plugin to match your needs.
 */
open class SekretPluginExtension {

    /**
     * Configuration to generate secrets from properties.
     */
    lateinit var properties: PropertiesExtension
        internal set

    fun properties(closure: Closure<in PropertiesExtension>): PropertiesExtension {
        return properties(ConfigureUtil.configureUsing(closure))
    }

    fun properties(action: Action<in PropertiesExtension>): PropertiesExtension {
        return Actions.with(properties, action)
    }
}

internal fun SekretPluginExtension.setupConvention(project: Project) {
    properties = PropertiesExtension(project.objects).also {
        it.setupConvention(project)
    }


}