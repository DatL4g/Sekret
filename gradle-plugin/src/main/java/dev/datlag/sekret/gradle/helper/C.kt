package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object C {

    private const val INTEROP_PACKAGE = "kotlinx.cinterop"
    private const val POINTER = "CPointer"
    private const val EXPERIMENTAL_FOREIGN_API = "ExperimentalForeignApi"
    private const val MEM_SCOPED = "memScoped"
    private const val BYTE_VAR = "ByteVar"
    private const val K_STRING_FROM_UTF_8 = "toKStringFromUtf8"
    private const val K_STRING = "toKString"
    private const val C_NAME = "CName"
    private const val WCSTR = "wcstr"
    private const val POINTED = "pointed"
    private const val INVOKE = "invoke"

    val pointer = ClassName(INTEROP_PACKAGE, POINTER)
    val experimentalForeignApi = ClassName(INTEROP_PACKAGE, EXPERIMENTAL_FOREIGN_API)
    val memScoped = MemberName(INTEROP_PACKAGE, MEM_SCOPED)
    val byteVar = ClassName(INTEROP_PACKAGE, BYTE_VAR)
    val kStringFromUtf8 = MemberName(INTEROP_PACKAGE, K_STRING_FROM_UTF_8)
    val kString = MemberName(INTEROP_PACKAGE, K_STRING)
    val cname = ClassName(INTEROP_PACKAGE, C_NAME)
    val wcstr = MemberName(INTEROP_PACKAGE, WCSTR)
    val pointed = MemberName(INTEROP_PACKAGE, POINTED)
    val invoke = MemberName(INTEROP_PACKAGE, INVOKE)

}