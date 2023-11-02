package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.datlag.sekret.gradle.builder.SekretFile
import java.security.MessageDigest
import java.util.Properties
import kotlin.experimental.xor

object Encoder {

    fun encodeProperties(properties: Properties, packageName: String): Pair<FileSpec, FileSpec> {
        val fileSpecBuilder = FileSpec.builder(packageName, "sekret")
        fileSpecBuilder.addKotlinDefaultImports(includeJvm = false, includeJs = false)

        val classSpecBuilder = FileSpec.builder(packageName, "Sekret")
        classSpecBuilder.addKotlinDefaultImports(includeJvm = false, includeJs = false)

        val classTypeBuilder = TypeSpec.classBuilder(ClassName(packageName, "Sekret"))

        properties.entries.forEach { entry ->
            val keyName = entry.key as String
            val secretValue = encode(entry.value as String, packageName)

            SekretFile.addMethod(fileSpecBuilder, classTypeBuilder, keyName, secretValue, packageName)
        }

        classSpecBuilder.addType(
            classTypeBuilder.build()
        )

        return fileSpecBuilder.build() to classSpecBuilder.build()
    }

    private fun encode(value: String, packageName: String): String {
        val obfuscator = sha256(packageName)
        val obfuscatorBytes = obfuscator.encodeToByteArray()
        val obfuscatedSecretBytes = arrayListOf<Byte>()
        var i = 0

        value.encodeToByteArray().forEach { secretByte ->
            val obfuscatorByte = obfuscatorBytes[i % obfuscatorBytes.size]
            val obfuscatedByte = secretByte.xor(obfuscatorByte)
            obfuscatedSecretBytes.add(obfuscatedByte)
            i++
        }

        var encoded = ""
        val iterator = obfuscatedSecretBytes.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            encoded += "0x" + Integer.toHexString(item.toInt() and 0xff)

            if (iterator.hasNext()) {
                encoded += ", "
            }
        }
        return encoded
    }

    private fun sha256(value: String): String {
        val bytes = value.encodeToByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}