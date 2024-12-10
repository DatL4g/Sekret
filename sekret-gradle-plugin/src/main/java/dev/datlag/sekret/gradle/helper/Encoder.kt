package dev.datlag.sekret.gradle.helper

import dev.datlag.sekret.gradle.EncodedProperty
import dev.datlag.sekret.gradle.model.GoogleServices
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.universalWordSplitter
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.xor

object Encoder {

    fun encodeProperties(
        properties: Properties?,
        googleServices: GoogleServices?,
        password: String
    ): Iterable<EncodedProperty> {
        val props = mutableSetOf<EncodedProperty>()

        properties?.entries?.forEach { entry ->
            val keyName = (entry.key as String).toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim()
            val secretValue = encode(entry.value as String, password)

            props.add(EncodedProperty(keyName, secretValue))
        }
        googleServices?.let { json ->
            props.add(EncodedProperty(
                key = GoogleServices.PROJECT_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                secret = encode(json.project.id, password)
            ))
            json.project.number?.let {
                props.add(EncodedProperty(
                    key = GoogleServices.PROJECT_NUMBER_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                    secret = encode(it, password)
                ))
            }
            json.project.firebaseUrl?.let {
                props.add(EncodedProperty(
                    key = GoogleServices.PROJECT_FIREBASE_URL_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                    secret = encode(it, password)
                ))
            }
            json.project.storageBucket?.let {
                props.add(EncodedProperty(
                    key = GoogleServices.PROJECT_STORAGE_BUCKET_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                    secret = encode(it, password)
                ))
            }
            json.firebase?.let { firebase ->
                firebase.info?.appId?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_APP_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
                firebase.currentApiKey?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_API_KEY_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
                firebase.androidClient?.clientId?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_ANDROID_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
                firebase.iOSClient?.clientId?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_IOS_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
                firebase.webClientOrFirebaseAuth?.clientId?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_WEB_OR_AUTH_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
                firebase.adminClient?.clientId?.let {
                    props.add(EncodedProperty(
                        key = GoogleServices.FIREBASE_ADMIN_ID_KEY.toCamelCase(universalWordSplitter(treatDigitsAsUppercase = false)).trim(),
                        secret = encode(it, password)
                    ))
                }
            }
        }

        return props
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