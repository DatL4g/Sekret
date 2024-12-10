package dev.datlag.sekret.gradle.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File

@Serializable
data class GoogleServices(
    @SerialName("project_info") val project: Project,
    @SerialName("client") val client: List<Client>
) {
    @Transient
    val firebase: Client? = client.maxByOrNull {
        it.firebaseFactor
    } ?: client.firstOrNull()

    @Serializable
    data class Project(
        @SerialName("project_id") val id: String,
        @SerialName("project_number") private val _number: String,
        @SerialName("firebase_url") private val _firebaseUrl: String? = null,
        @SerialName("storage_bucket") private val _storageBucket: String? = null,
    ) {
        @Transient
        val number = _number.ifBlank { null }

        @Transient
        val firebaseUrl = _firebaseUrl?.ifBlank { null }

        @Transient
        val storageBucket = _storageBucket?.ifBlank { null }
    }

    @Serializable
    data class Client(
        @SerialName("client_info") val info: Info? = null,
        @SerialName("oauth_client") val oauthClient: List<OAuthClient> = emptyList(),
        @SerialName("api_key") val apiKey: List<ApiKey> = emptyList(),
        @SerialName("services") val services: Services? = null
    ) {
        @Transient
        val androidClient: OAuthClient? = clientOfType(1)

        @Transient
        val iOSClient: OAuthClient? = clientOfType(2)

        @Transient
        val webClientOrFirebaseAuth: OAuthClient? = clientOfType(3)

        @Transient
        val adminClient: OAuthClient? = clientOfType(4)

        @Transient
        val currentApiKey: String? = apiKey.firstNotNullOfOrNull { it.current }

        @Transient
        internal val firebaseFactor: Int = listOfNotNull(
            androidClient?.let { 1 },
            iOSClient?.let { 1 },
            webClientOrFirebaseAuth?.let { 2 },
            adminClient?.let { 1 }
        ).sum()

        private fun clientOfType(type: Int): OAuthClient? = oauthClient.firstOrNull {
            it.clientType == type
        } ?: services?.appInvite?.otherPlatformOAuthClient?.firstOrNull {
            it.clientType == type
        }

        @Serializable
        data class Info(
            @SerialName("mobilesdk_app_id") private val _appId: String?,
        ) {
            @Transient
            val appId: String? = _appId?.ifBlank { null }
        }

        @Serializable
        data class OAuthClient(
            @SerialName("client_id") private val _clientId: String?,
            @SerialName("client_type") val clientType: Int = -1,
        ) {
            @Transient
            val clientId: String? = _clientId?.ifBlank { null }
        }

        @Serializable
        data class ApiKey(
            @SerialName("current_key") private val _current: String? = null,
        ) {
            @Transient
            val current: String? = _current?.ifBlank { null }
        }

        @Serializable
        data class Services(
            @SerialName("appinvite_service") val appInvite: AppInvite? = null,
        ) {
            @Serializable
            data class AppInvite(
                @SerialName("other_platform_oauth_client") val otherPlatformOAuthClient: List<OAuthClient> = emptyList(),
            )
        }
    }

    companion object {
        internal const val PROJECT_ID_KEY = "PROJECT_ID"
        internal const val PROJECT_NUMBER_KEY = "PROJECT_NUMBER"
        internal const val PROJECT_FIREBASE_URL_KEY = "PROJECT_FIREBASE_URL"
        internal const val PROJECT_STORAGE_BUCKET_KEY = "PROJECT_STORAGE_BUCKET"

        internal const val FIREBASE_APP_ID_KEY = "FIREBASE_APP_ID"
        internal const val FIREBASE_ANDROID_ID_KEY = "FIREBASE_ANDROID_ID"
        internal const val FIREBASE_IOS_ID_KEY = "FIREBASE_IOS_ID"
        internal const val FIREBASE_WEB_OR_AUTH_ID_KEY = "FIREBASE_WEB_OR_AUTH_ID"
        internal const val FIREBASE_ADMIN_ID_KEY = "FIREBASE_ADMIN_ID"
        internal const val FIREBASE_API_KEY_KEY = "FIREBASE_API_KEY"

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun from(file: File, logger: Logger): GoogleServices? {
            return file.inputStream().use { inputStream ->
                runCatching {
                    json.decodeFromStream<GoogleServices>(inputStream)
                }.onFailure {
                    logger.log(LogLevel.ERROR, "Seems like your google-services.json is malformed, could not parse.", it)
                }.getOrNull()
            }
        }
    }
}
