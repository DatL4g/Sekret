package dev.datlag.sekret.gradle

data class EncodedProperty(
    val key: String,
    val secret: String
)

val Iterable<EncodedProperty>.keys
    get() = this.map { it.key }