package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.generator.SekretGenerator.JNI_PACKAGE_NAME
import dev.datlag.sekret.gradle.helper.JNI

class JSGenerator(private val settings: SekretGenerator.Settings) : SekretGenerator.Generator {
    override fun generate(encodedProperties: Iterable<EncodedProperty>): FileSpec {
        val spec = FileSpec.builder(settings.packageName, settings.className)
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        val typeSpec = TypeSpec.classBuilder(settings.className)

        encodedProperties.forEach { (key, secret) ->
            typeSpec.addFunction(
                FunSpec.builder(key)
                    .addParameter("key", String::class)
                    .returns(String::class)
                    .addStatement("val obfuscatedSecret = intArrayOf(%L)", secret)
                    .addStatement("return %M(obfuscatedSecret, key)", JNI.getOriginalValue(JNI_PACKAGE_NAME))
                    .build()
            )
        }

        return spec.addType(typeSpec.build()).build()
    }
}