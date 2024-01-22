package dev.datlag.sekret.gradle.generator.native

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.generator.SekretGenerator.JNI_PACKAGE_NAME
import dev.datlag.sekret.gradle.helper.C
import dev.datlag.sekret.gradle.helper.JNI
import dev.datlag.sekret.gradle.helper.Utils
import java.io.File

class NativeGenerator(
    private val settings: SekretGenerator.Settings,
    private val outputDir: File
) : SekretGenerator.Generator {
    override fun generate(encodedProperties: Iterable<EncodedProperty>) {
        val spec = FileSpec.builder(settings.packageName, settings.className)
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        encodedProperties.forEach { (key, secret) ->
            spec.addFunction(
                FunSpec.builder(key)
                    .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi, C.experimentalNativeApi))
                    .addParameter("key", String::class)
                    .returns(String::class)
                    .addStatement("val obfuscatedSecret = intArrayOf(%L)", secret)
                    .addStatement("return %M(obfuscatedSecret, key)", JNI.getOriginalValue(JNI_PACKAGE_NAME))
                    .build()
            )
        }

        spec.build().writeTo(outputDir)
    }
}