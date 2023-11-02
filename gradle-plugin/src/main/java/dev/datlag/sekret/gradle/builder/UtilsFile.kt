package dev.datlag.sekret.gradle.builder

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.datlag.sekret.gradle.canWriteSafely
import dev.datlag.sekret.gradle.existsSafely
import dev.datlag.sekret.gradle.helper.C
import dev.datlag.sekret.gradle.helper.JNI
import dev.datlag.sekret.gradle.helper.Utils
import java.io.File

object UtilsFile {

    fun create(
        directory: File,
        packageName: String
    ) {
        val fileSpecBuilder = FileSpec.builder(packageName, "utils")
        fileSpecBuilder.addKotlinDefaultImports(includeJvm = false, includeJs = false)

        createJNIEnvVarToJStringExtension(fileSpecBuilder, packageName)
        createStringToJStringExtension(fileSpecBuilder, packageName)
        createJStringToCPointerByteVarExtension(fileSpecBuilder, packageName)
        createJStringToStringExtension(fileSpecBuilder, packageName)

        val fileSpec = fileSpecBuilder.build()
        if (directory.existsSafely() && directory.canWriteSafely()) {
            fileSpec.writeTo(directory)
        } else {
            fileSpec.writeTo(System.out)
        }
    }

    private fun createJNIEnvVarToJStringExtension(
        spec: FileSpec.Builder,
        packageName: String
    ) {
        spec.addFunction(
            FunSpec.builder("newString")
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
                .addModifiers(KModifier.INTERNAL)
                .receiver(C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .addParameter("chars", C.pointer.parameterizedBy(JNI.jCharVar(packageName)))
                .addParameter("length", Int::class)
                .returns(JNI.jString(packageName).copy(nullable = true))
                .addStatement("val method = %M?.pointed?.NewString ?: error(%S)", C.pointed, JNI.Error.NewString.toString())
                .addStatement("return method.%M(this, chars, length)", C.invoke)
                .build()
        )
    }

    private fun createStringToJStringExtension(
        spec: FileSpec.Builder,
        packageName: String
    ) {
        spec.addFunction(
            FunSpec.builder("toJString")
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
                .addModifiers(KModifier.INTERNAL)
                .receiver(String::class)
                .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .returns(JNI.jString(packageName).copy(nullable = true))
                .beginControlFlow("return %M", C.memScoped)
                .addStatement("env.newString(%M.ptr, length)", C.wcstr)
                .endControlFlow()
                .build()
        )
    }

    private fun createJStringToCPointerByteVarExtension(
        spec: FileSpec.Builder,
        packageName: String
    ) {
        spec.addFunction(
            FunSpec.builder("getStringUTFChars")
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
                .addModifiers(KModifier.INTERNAL)
                .receiver(JNI.jString(packageName))
                .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .returns(C.pointer.parameterizedBy(C.byteVar).copy(nullable = true))
                .addStatement("val method = env.%M.pointed?.GetStringUTFChars ?: error(%S)", C.pointed, JNI.Error.GetStringUTFChars.toString())
                .addStatement("return method.%M(env, this, null)", C.invoke)
                .build()
        )
    }

    private fun createJStringToStringExtension(
        spec: FileSpec.Builder,
        packageName: String
    ) {
        spec.addFunction(
            FunSpec.builder("getString")
                .addAnnotation(Utils.optInAnnotation(C.experimentalForeignApi))
                .addModifiers(KModifier.INTERNAL)
                .receiver(JNI.jString(packageName))
                .addParameter("env", C.pointer.parameterizedBy(JNI.jniEnvVar(packageName)))
                .returns(String::class)
                .addStatement("val chars = getStringUTFChars(env)")
                .addStatement("return chars?.%M() ?: error(%S)", C.kStringFromUtf8, JNI.Error.JString.toString())
                .build()
        )
    }
}