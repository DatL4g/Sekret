package dev.datlag.sekret.gradle.model

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File

@Serializable
data class YamlConfig(
    @SerialName("common") val common: Collection<Target> = emptySet(),
    @SerialName("jni") val jni: Collection<Target> = emptySet(),
    @SerialName("web") val web: Collection<Target> = emptySet(),
    @SerialName("native") val native: Collection<Target> = emptySet(),
) {

    @Serializable
    data class Target(
        @SerialName("name") val name: String,
        @SerialName("value") val value: String
    )

    companion object {
        fun from(file: File, logger: Logger): YamlConfig? {
            return file.inputStream().use { inputStream ->
                runCatching {
                    Yaml.default.decodeFromStream<YamlConfig>(inputStream)
                }.onFailure {
                    logger.log(LogLevel.ERROR, "Seems like your YAML is malformed, could not parse.", it)
                }.getOrNull()
            }
        }
    }
}