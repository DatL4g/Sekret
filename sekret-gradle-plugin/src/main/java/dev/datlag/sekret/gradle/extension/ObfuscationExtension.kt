package dev.datlag.sekret.gradle.extension

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.internal.Actions
import org.gradle.util.internal.ConfigureUtil

/**
 * Used for changing obfuscation configurations.
 */
open class ObfuscationExtension(objectFactory: ObjectFactory) {

    /**
     * Set whether obfuscation (secure logging) is enabled or not.
     * Default: true
     */
    open val enabled: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * Configuration for how *Secret* annotations are handled.
     */
    lateinit var secretAnnotation: SecretAnnotationExtension
        private set

    /**
     * Configuration for how *Obfuscate* annotations are handled.
     */
    lateinit var obfuscateAnnotation: ObfuscateAnnotationExtension
        private set

    /**
     * Change how *Secret* annotations are handled.
     */
    fun secretAnnotation(closure: Closure<in SecretAnnotationExtension>): SecretAnnotationExtension {
        return secretAnnotation(ConfigureUtil.configureUsing(closure))
    }

    /**
     * Change how *Secret* annotations are handled.
     */
    fun secretAnnotation(action: Action<in SecretAnnotationExtension>): SecretAnnotationExtension {
        return Actions.with(secretAnnotation, action)
    }

    /**
     * Change how *Obfuscate* annotations are handled.
     */
    fun obfuscateAnnotation(closure: Closure<in ObfuscateAnnotationExtension>): ObfuscateAnnotationExtension {
        return obfuscateAnnotation(ConfigureUtil.configureUsing(closure))
    }

    /**
     * Change how *Obfuscate* annotations are handled.
     */
    fun obfuscateAnnotation(action: Action<in ObfuscateAnnotationExtension>): ObfuscateAnnotationExtension {
        return Actions.with(obfuscateAnnotation, action)
    }

    internal fun setupConvention(project: Project) {
        enabled.convention(true)
        secretAnnotation = SecretAnnotationExtension(project.objects).also {
            it.setupConvention(project)
        }
        obfuscateAnnotation = ObfuscateAnnotationExtension(project.objects).also {
            it.setupConvention(project)
        }
    }

    companion object {
        internal const val DEFAULT_SECRET_MASK = "***"
        internal const val DEFAULT_SECRET_MAK_NULL = true
    }

    open class SecretAnnotationExtension(objectFactory: ObjectFactory) {

        /**
         * Change mask of @Secret annotated properties.
         * Default: ***
         */
        open val mask: Property<String> = objectFactory.property(String::class.java)

        /**
         * Set whether nullable Strings should be masked or not.
         * Default: true
         */
        open val maskNull: Property<Boolean> = objectFactory.property(Boolean::class.java)

        internal fun setupConvention(project: Project) {
            mask.convention(DEFAULT_SECRET_MASK)
            maskNull.convention(DEFAULT_SECRET_MAK_NULL)
        }
    }

    open class ObfuscateAnnotationExtension(objectFactory: ObjectFactory) {

        /**
         * Change the seed used for obfuscation.
         * Default: SecureRandom value
         */
        open val seed: Property<Int> = objectFactory.property(Int::class.java)

        internal fun setupConvention(project: Project) {

        }
    }
}