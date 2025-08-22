package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.*
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.helper.Utils
import dev.datlag.sekret.gradle.keys
import java.io.File

class CommonGenerator(
    private val settings: SekretGenerator.Settings,
    private val outputDir: File
) : SekretGenerator.Generator {

    override fun generate(encodedProperties: Iterable<EncodedProperty>) {
        val spec = FileSpec.builder(settings.packageName, settings.className)
            .addKotlinDefaultImports(includeJvm = false, includeJs = false)

        var typeSpec = TypeSpec.objectBuilder(settings.className).addModifiers(KModifier.EXPECT)

        encodedProperties.filter { it.targetType is EncodedProperty.TargetType.Common }.keys.forEach { key ->
            typeSpec = typeSpec.addFunction(
                FunSpec.builder(key)
                    .addModifiers(KModifier.EXPECT)
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
                    .build()
            ).addFunction(
                FunSpec.builder(key)
                    .addModifiers(KModifier.EXPECT)
                    .addParameter("key", String::class)
                    .addParameter(
                        ParameterSpec.builder(
                            name = "config",
                            Utils.sekretConfig
                        ).defaultValue(
                            "%T()",
                            Utils.sekretConfig
                        ).build()
                    )
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .build()
            )
        }

        spec.addType(typeSpec.build()).build().writeTo(outputDir)
    }
}