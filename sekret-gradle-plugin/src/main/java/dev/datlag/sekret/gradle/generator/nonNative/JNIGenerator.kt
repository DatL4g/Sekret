package dev.datlag.sekret.gradle.generator.nonNative

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.helper.Utils
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
                    .addModifiers(KModifier.ACTUAL)
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
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.ACTUAL)
                    .addParameter("key", String::class)
                    .addParameter("config", Utils.sekretConfig)
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .beginControlFlow("return if (config.jni.decryptDirectly)")
                        .addStatement("${key}Decrypted(key)")
                    .nextControlFlow("else")
                        .addStatement("${key}Encrypted()?.decrypt(key) ?:")
                        .beginControlFlow("if (config.jni.fallbackToDirectDecryption)")
                            .addStatement("${key}Decrypted(key)")
                        .nextControlFlow("else")
                            .addStatement("null")
                        .endControlFlow()
                    .endControlFlow()
                    .build()
            ).addFunction(
                FunSpec.builder(key)
                    .addAnnotation(JvmStatic::class)
                    .addParameter("key", String::class)
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .addStatement("return $key(key, %T())", Utils.sekretConfig)
                    .build()
            ).addFunction(
                FunSpec.builder("${key}Decrypted")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    .addParameter("key", String::class)
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .build()
            ).addFunction(
                FunSpec.builder("_${key}Encrypted")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.PRIVATE, KModifier.EXTERNAL)
                    .returns(IntArray::class.asTypeName().copy(nullable = true))
                    .build()
            ).addFunction(
                FunSpec.builder("${key}Encrypted")
                    .addAnnotation(JvmStatic::class)
                    .addModifiers(KModifier.PRIVATE)
                    .returns(Utils.encryptedSecret.copy(nullable = true))
                    .addStatement("return _${key}Encrypted()?.let(%L)", Utils.encryptedSecret.member("invoke").reference())
                    .build()
            )
        }
        spec.addType(typeSpec.build()).build().writeTo(outputDir)
    }
}