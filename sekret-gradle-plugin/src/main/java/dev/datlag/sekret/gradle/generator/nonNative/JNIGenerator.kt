package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.*
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.keys
import java.io.File

class JNIGenerator(
    private val settings: SekretGenerator.Settings,
    private val outputDir: File
) : SekretGenerator.Generator {
    override fun generate(encodedProperties: Iterable<EncodedProperty>) {
        val spec = FileSpec.builder(settings.packageName, "${settings.className}.jni")
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        var typeSpec = TypeSpec.objectBuilder(settings.className).addModifiers(KModifier.ACTUAL)

        encodedProperties.keys.forEach { key ->
            typeSpec = typeSpec.addFunction(
                FunSpec.builder(key)
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.ACTUAL, KModifier.EXTERNAL)
                    .addParameter("key", String::class)
                    .returns(String::class.asClassName().copy(nullable = true))
                    .build()
            )
        }
        spec.addType(typeSpec.build()).build().writeTo(outputDir)
    }
}