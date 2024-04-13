package dev.datlag.sekret.gradle.generator.native

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.generator.SekretGenerator
import dev.datlag.sekret.gradle.generator.SekretGenerator.JNI_PACKAGE_NAME
import dev.datlag.sekret.gradle.helper.C
import dev.datlag.sekret.gradle.helper.JNI
import dev.datlag.sekret.gradle.helper.Utils
import java.io.File

class NativeJNIGenerator(
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
                    .addAnnotation(
                        AnnotationSpec.builder(C.cname)
                            .addMember("%S", "Java_${Utils.packageNameCSave(settings.packageName)}_Sekret_$key")
                            .build()
                    )
                    .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(JNI_PACKAGE_NAME)))
                    .addParameter("clazz", JNI.libraryJObject(JNI_PACKAGE_NAME).copy(nullable = true))
                    .addParameter("key", JNI.libraryJString(JNI_PACKAGE_NAME))
                    .returns(JNI.libraryJString(JNI_PACKAGE_NAME).copy(nullable = true))
                    .addStatement("val obfuscatedSecret = intArrayOf(%L)", secret)
                    .addStatement(
                        "return %T.%M(obfuscatedSecret, key, env)",
                        JNI.sekretHelper(JNI_PACKAGE_NAME),
                        JNI.getExtensionNativeValue(JNI_PACKAGE_NAME)
                    )
                    .build()
            )
        }

        spec.build().writeTo(outputDir)
    }
}