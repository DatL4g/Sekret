package dev.datlag.sekret.gradle.generator

import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.native.NativeGenerator
import dev.datlag.sekret.gradle.generator.native.NativeJNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JNIGenerator
import dev.datlag.sekret.gradle.generator.nonNative.JSGenerator
import dev.datlag.sekret.gradle.generator.nonNative.CommonGenerator
import java.io.File

object SekretGenerator {

    internal const val JNI_PACKAGE_NAME = "dev.datlag.sekret"

    fun createCommon(packageName: String, outputDir: File): CommonGenerator {
        return CommonGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            ),
            outputDir
        )
    }

    fun createNative(packageName: String, outputDir: File): NativeGenerator {
        return NativeGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            ),
            outputDir
        )
    }

    fun createNativeJNI(packageName: String, outputDir: File): NativeJNIGenerator {
        return NativeJNIGenerator(
            Settings(
                packageName = packageName,
                className = "sekret"
            ),
            outputDir
        )
    }

    fun createJNI(packageName: String, outputDir: File): JNIGenerator {
        return JNIGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            ),
            outputDir
        )
    }

    fun createJS(packageName: String, outputDir: File): JSGenerator {
        return JSGenerator(
            Settings(
                packageName = packageName,
                className = "Sekret"
            ),
            outputDir
        )
    }

    fun createAllForTargets(packageName: String, structure: ModuleGenerator.SourceStructure): Set<Generator> {
        val generators = mutableSetOf<Generator>()

        generators.add(createCommon(packageName, structure.commonMain))
        if (structure.hasNative) {
            generators.add(createNative(packageName, structure.nativeMain))
        }
        if (structure.hasJNI) {
            generators.add(createNativeJNI(packageName, structure.jniNativeMain))
            generators.add(createJNI(packageName, structure.jniMain))
        }
        if (structure.hasJS) {
            generators.add(createJS(packageName, structure.webMain))
        }

        return generators
    }

    fun generate(
        encodedProperties: Collection<EncodedProperty>,
        generators: Iterable<Generator>,
        actualModifier: Boolean = generators.any { it is CommonGenerator }
    ) {
        if (encodedProperties.isNotEmpty()) {
            generators.forEach { it.generate(encodedProperties, actualModifier) }
        }
    }

    interface Generator {
        fun generate(encodedProperties: Iterable<EncodedProperty>, actualModifier: Boolean)
    }

    data class Settings(
        val packageName: String,
        val className: String
    )
}