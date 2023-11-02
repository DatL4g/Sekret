package dev.datlag.sekret.gradle.builder

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.existsSafely
import dev.datlag.sekret.gradle.helper.C
import dev.datlag.sekret.gradle.helper.JNI
import dev.datlag.sekret.gradle.helper.KHash
import dev.datlag.sekret.gradle.helper.Utils
import java.io.File

object ObfuscationFile {

    fun create(
        directory: File,
        packageName: String
    ) {
        val fileSpecBuilder = FileSpec.builder(packageName, "obfuscation")
        fileSpecBuilder.addKotlinDefaultImports(includeJvm = false, includeJs = false)

        createGetOriginalKeyFunction(fileSpecBuilder, packageName)

        val fileSpec = fileSpecBuilder.build()
        if (directory.existsSafely() && directory.canWriteSafely()) {
            fileSpec.writeTo(directory)
        } else {
            fileSpec.writeTo(System.out)
        }
    }

    private fun createGetOriginalKeyFunction(
        spec: FileSpec.Builder,
        packageName: String
    ) {
        spec.addFunction(
            FunSpec.builder("getOriginalKey")
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
                .addModifiers(KModifier.INTERNAL)
                .addParameter("secret", IntArray::class)
                .addParameter("obfuscatingString", JNI.jString(packageName))
                .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .returns(JNI.jString(packageName).copy(nullable = true))
                .addStatement("val obfuscator = obfuscatingString.getString(env)")
                .addStatement("val obfuscatorBytes = %T.digest(obfuscator.encodeToByteArray())", KHash.sha256)
                .beginControlFlow("val hex = obfuscatorBytes.fold(%S) { str, it ->", String())
                .addStatement("val value = it.toUByte().toString(16)")
                .beginControlFlow("str + if (value.length == 1)")
                .addStatement("%S + value", "0")
                .nextControlFlow("else")
                .addStatement("value")
                .endControlFlow()
                .endControlFlow()
                .addStatement("val hexBytes = hex.encodeToByteArray()")
                .beginControlFlow("val out = secret.mapIndexed { index, it ->")
                .addStatement("val obfuscatorByte = hexBytes[index %L hexBytes.size]", "%")
                .addStatement("it.toByte().%M(obfuscatorByte)", MemberName("kotlin.experimental", "xor"))
                .endControlFlow()
                .addStatement("return out.toByteArray().toKString().toJString(env)")
                .build()
        )
    }
}