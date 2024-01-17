package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.*
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.keys

class JNIGenerator(private val settings: SekretGenerator.Settings) : SekretGenerator.Generator {
    override fun generate(encodedProperties: Iterable<EncodedProperty>): FileSpec {
        val spec = FileSpec.builder(settings.packageName, settings.className)
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        val typeSpec = TypeSpec.objectBuilder(settings.className)

        encodedProperties.keys.forEach { key ->
            typeSpec.addFunction(
                FunSpec.builder(key)
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.EXTERNAL)
                    .addParameter("key", String::class)
                    .returns(String::class.asClassName().copy(nullable = true))
                    .build()
            )
        }
        return spec.addType(typeSpec.build()).build()
    }
}