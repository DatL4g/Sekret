package dev.datlag.sekret.gradle

data class EncodedProperty(
    val key: String,
    val secret: String,
    val targetType: TargetType = TargetType.Common
) {
    /**
     * For YAML specific targets
     */
    sealed interface TargetType {
        object Common : TargetType
        object JNI : TargetType
        object Web : TargetType
        object Native : TargetType
    }
}

val Iterable<EncodedProperty>.keys
    get() = this.map { it.key }