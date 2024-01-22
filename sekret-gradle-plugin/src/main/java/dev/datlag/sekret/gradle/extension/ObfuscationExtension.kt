package dev.datlag.sekret.gradle.extension

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

/**
 * Used for changing obfuscation configurations.
 */
open class ObfuscationExtension(objectFactory: ObjectFactory) {

    /**
     * Change mask of @Secret annotated properties.
     * Default: ***
     */
    open val secretMask: Property<String> = objectFactory.property(String::class.java)

    /**
     * Set whether nullable Strings should be masked or not.
     * Default: true
     */
    open val secretMaskNull: Property<Boolean> = objectFactory.property(Boolean::class.java)

    internal fun setupConvention(project: Project) {
        secretMask.convention(DEFAULT_SECRET_MASK)
        secretMaskNull.convention(DEFAULT_SECRET_MAK_NULL)
    }

    companion object {
        internal const val DEFAULT_SECRET_MASK = "***"
        internal const val DEFAULT_SECRET_MAK_NULL = true
    }
}