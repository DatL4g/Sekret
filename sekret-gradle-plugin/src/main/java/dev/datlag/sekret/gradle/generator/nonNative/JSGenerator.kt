package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.*
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.generator.SekretGenerator.JNI_PACKAGE_NAME
import dev.datlag.sekret.gradle.helper.JNI
import java.io.File

class JSGenerator(
    private val settings: SekretGenerator.Settings,
    private val outputDir: File
) : SekretGenerator.Generator {
    override fun generate(encodedProperties: Iterable<EncodedProperty>) {
        val spec = FileSpec.builder(settings.packageName, "${settings.className}.js")
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        var typeSpec = TypeSpec.objectBuilder(settings.className).addModifiers(KModifier.ACTUAL)

        encodedProperties.forEach { (key, secret) ->
            typeSpec = typeSpec.addFunction(
                FunSpec.builder(key)
                    .addModifiers(KModifier.ACTUAL)
                    .addParameter("key", String::class)
                    .returns(String::class.asClassName().copy(nullable = true))
                    .addStatement("val obfuscatedSecret = intArrayOf(%L)", secret)
                    .addStatement("return %M(obfuscatedSecret, key)", JNI.getNativeValue(JNI_PACKAGE_NAME))
                    .build()
            )
        }

        spec.addType(typeSpec.build()).build().writeTo(outputDir)
    }
}