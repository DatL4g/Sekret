package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.datlag.sekret.gradle.builder.SekretFile
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.universalWordSplitter
import java.security.MessageDigest
import java.util.Properties
import kotlin.experimental.xor

object Encoder {

    fun encodeProperties(
        properties: Properties,
        password: String,
        onNewMethod: (name: String, secret: String) -> Unit
    ) {
        properties.entries.forEach { entry ->
            val keyName = (entry.key as String).toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false))
            val secretValue = encode(entry.value as String, password)

            onNewMethod(keyName, secretValue)
        }
    }

    private fun encode(value: String, password: String): String {
        val obfuscator = sha256(password)
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