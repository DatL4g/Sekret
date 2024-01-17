package dev.datlag.sekret.gradle.generator

import com.squareup.kotlinpoet.FileSpec
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.native.NativeGenerator
import dev.datlag.sekret.gradle.generator.native.NativeJNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JSGenerator

object SekretGenerator {

    internal const val JNI_PACKAGE_NAME = "dev.datlag.sekret"

    fun createNative(packageName: String): NativeGenerator {
        return NativeGenerator(
            Settings(
                packageName = packageName,
                className = "sekret"
            )
        )
    }

    fun createNativeJNI(packageName: String): NativeJNIGenerator {
        return NativeJNIGenerator(
            Settings(
                packageName = packageName,
                className = "sekret"
            )
        )
    }

    fun createJNI(packageName: String): JNIGenerator {
        return JNIGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            )
        )
    }

    fun createJS(packageName: String): JSGenerator {
        return JSGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            )
        )
    }

    fun generate(encodedProperties: Iterable<EncodedProperty>, vararg generator: Generator) {
        generator.forEach { it.generate(encodedProperties) }
    }

    interface Generator {
        fun generate(encodedProperties: Iterable<EncodedProperty>): FileSpec
    }

    data class Settings(
        val packageName: String,
        val className: String
    )
}