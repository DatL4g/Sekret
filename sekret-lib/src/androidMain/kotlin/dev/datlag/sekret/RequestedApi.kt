package dev.datlag.sekret

/**
 * Denotes that the annotated element should be called on the given API level or higher.
 *
 * This is similar in purpose to the `@RequiresApi` annotation, but only expresses that this should be called with the
 * specified api as minSdk, however it's not required.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RequestedApi(val api: Int)
