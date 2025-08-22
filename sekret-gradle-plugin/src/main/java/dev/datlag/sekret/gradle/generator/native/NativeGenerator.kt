package dev.datlag.sekret.gradle.generator.native

import com.squareup.kotlinpoet.*
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
        val spec = FileSpec.builder(settings.packageName, "${settings.className}.native")
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        var typeSpec = TypeSpec.objectBuilder(settings.className).addModifiers(KModifier.ACTUAL)

        encodedProperties.filter { it.targetType is EncodedProperty.TargetType.Common || it.targetType is EncodedProperty.TargetType.Native }.forEach { (key, secret, target) ->
            typeSpec = typeSpec.addFunction(
                FunSpec.builder(key)
                    .apply {
                        if (target is EncodedProperty.TargetType.Common) {
                            addModifiers(KModifier.ACTUAL)
                        }
                    }
                    .addParameter("key", String::class)
                    .addParameter(
                        ParameterSpec.builder(
                            name = "config",
                            LambdaTypeName.get(
                                receiver = Utils.sekretConfig.nestedClass("Builder"),
                                returnType = Unit::class.asTypeName()
                            )
                        ).build()
                    )
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .addStatement("return $key(key, %T.Builder().apply(config).build())", Utils.sekretConfig)
                    .build()
            ).addFunction(
                FunSpec.builder(key)
                    .apply {
                        if (target is EncodedProperty.TargetType.Common) {
                            addModifiers(KModifier.ACTUAL)
                        }
                    }
                    .addParameter("key", String::class)
                    .addParameter(
                        ParameterSpec.builder(
                            name = "config",
                            Utils.sekretConfig
                        ).build()
                    )
                    .returns(String::class.asClassName().copy(nullable = true))
                    .addStatement("val obfuscatedSecret = intArrayOf(%L)", secret)
                    .addStatement("return %M(obfuscatedSecret, key)", JNI.getNativeValue(JNI_PACKAGE_NAME))
                    .build()
            )
        }

        spec.addType(typeSpec.build()).build().writeTo(outputDir)
    }
}