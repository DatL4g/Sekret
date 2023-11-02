package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.ClassName

object JNI {

    private const val JNI_ENV_VAR = "JNIEnvVar"
    private const val J_STRING = "jstring"
    private const val J_CHAR_VAR = "jcharVar"
    private const val J_CLASS = "jclass"

    fun jniEnvVar(packageName: String) = ClassName(packageName, JNI_ENV_VAR)
    fun jString(packageName: String) = ClassName(packageName, J_STRING)
    fun jCharVar(packageName: String) = ClassName(packageName, J_CHAR_VAR)
    fun jClass(packageName: String) = ClassName(packageName, J_CLASS)

    sealed class Error(private val msg: String) {
        object NewString : Error("Could not find NewString method in JNI")
        object GetStringUTFChars : Error("Could not find GetStringUTFChars method in JNI")
        object JString : Error("Unable to create a String from the given jstring")

        override fun toString(): String {
            return msg
        }
    }

}