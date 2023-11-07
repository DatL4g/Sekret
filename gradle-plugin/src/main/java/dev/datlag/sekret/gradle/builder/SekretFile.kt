package dev.datlag.sekret.gradle.builder

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.existsSafely
import dev.datlag.sekret.gradle.helper.C
import dev.datlag.sekret.gradle.helper.Encoder
import dev.datlag.sekret.gradle.helper.JNI
import dev.datlag.sekret.gradle.helper.Utils
import java.io.File
import java.util.Properties

object SekretFile {

    fun create(
        sourceInfo: ModuleStructure.SourceInfo,
        properties: Properties,
        packageName: String,
        password: String
    ) {
        val nativeFileSpec = FileSpec.builder(packageName, "sekret")
        val nativeJniFileSpec = FileSpec.builder(packageName, "sekret")
        val jniFileSpec = FileSpec.builder(packageName, "Sekret")
        val jniTypeSpec = TypeSpec.classBuilder("Sekret")

        nativeFileSpec.addKotlinDefaultImports(includeJvm = false, includeJs = false)
        nativeJniFileSpec.addKotlinDefaultImports(includeJvm = false, includeJs = false)
        jniFileSpec.addKotlinDefaultImports(includeJvm = false, includeJs = false)

        Encoder.encodeProperties(properties, password) { name, secret ->
            addMethod(
                nativeFileSpec,
                nativeJniFileSpec,
                jniTypeSpec,
                name,
                secret,
                packageName
            )
        }

        jniFileSpec.addType(jniTypeSpec.build())
        nativeFileSpec.build().writeTo(sourceInfo.nativeMain)
        nativeJniFileSpec.build().writeTo(sourceInfo.jniNativeMain)
        jniFileSpec.build().writeTo(sourceInfo.jniMain)
    }

    fun addMethod(
        nativeFileSpec: FileSpec.Builder,
        nativeJniFileSpec: FileSpec.Builder,
        jniClassSpec: TypeSpec.Builder,
        key: String,
        secret: String,
        packageName: String
    ) {
        nativeFileSpec.addFunction(
            FunSpec.builder(key)
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi, C.experimentalNativeApi))
                .addParameter("key", String::class)
                .returns(String::class.asClassName().copy(nullable = true))
                .addStatement("val obfuscatedSecret = intArrayOf(")
                .addStatement("\t%L", secret)
                .addStatement(")")
                .addStatement("return getOriginalKey(key)")
                .build()
        )

        nativeJniFileSpec.addFunction(
            FunSpec.builder(key)
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi, C.experimentalNativeApi))
                .addAnnotation(
                    AnnotationSpec.builder(C.cname)
                        .addMember("%S", "Java_${Utils.packageNameCSave(packageName)}_Sekret_$key")
                        .build()
                )
                .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .addParameter("clazz", JNI.jClass(packageName).copy(nullable = true))
                .addParameter("key", JNI.jString(packageName))
                .returns(JNI.jString(packageName).copy(nullable = true))
                .addStatement("val obfuscatedSecret = intArrayOf(")
                .addStatement("\t%L", secret)
                .addStatement(")")
                .addStatement("return getOriginalKey(obfuscatedSecret, key, env)")
                .build()
        )

        jniClassSpec.addFunction(
            FunSpec.builder(key)
                .addModifiers(KModifier.EXTERNAL)
                .addParameter("key", String::class)
                .returns(String::class.asClassName().copy(nullable = true))
                .build()
        )
    }
}