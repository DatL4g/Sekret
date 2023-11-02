package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName.Companion.member

object KHash {

    private const val SHA256_PACKAGE = "org.komputing.khash.sha256"
    private const val SHA256_CLASS = "Sha256"

    val sha256 = ClassName(SHA256_PACKAGE, SHA256_CLASS)

    object SHA256 {
        val digest = sha256.member("digest")
    }
}