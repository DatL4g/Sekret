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
        nativeDirectory: File,
        targetDirectory: File,
        properties: Properties,
        packageName: String
    ) {
        val (nativeFileSpec, targetFileSpec) = Encoder.encodeProperties(properties, packageName)

        if (nativeDirectory.existsSafely() && nativeDirectory.canWriteSafely()) {
            nativeFileSpec.writeTo(nativeDirectory)
        } else {
            nativeFileSpec.writeTo(System.out)
        }

        if (targetDirectory.existsSafely() && targetDirectory.canWriteSafely()) {
            targetFileSpec.writeTo(targetDirectory)
        } else {
            targetFileSpec.writeTo(System.out)
        }
    }

    fun addMethod(
        nativeFileSpec: FileSpec.Builder,
        kotlinClassSpec: TypeSpec.Builder,
        key: String,
        secret: String,
        packageName: String
    ) {
        nativeFileSpec.addFunction(
            FunSpec.builder(key)
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
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
                .addStatement("%L", secret)
                .addStatement(")")
                .addStatement("return getOriginalKey(obfuscatedSecret, key, env)")
                .build()
        )

        kotlinClassSpec.addFunction(
            FunSpec.builder(key)
                .addModifiers(KModifier.EXTERNAL)
                .addParameter("key", String::class)
                .returns(String::class.asClassName().copy(nullable = true))
                .build()
        )
    }
}