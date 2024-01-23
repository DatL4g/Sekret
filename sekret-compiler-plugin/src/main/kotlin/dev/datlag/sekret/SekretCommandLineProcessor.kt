package dev.datlag.sekret

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class SekretCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "sekretPlugin"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = KEY_SECRET_MASK.toString(), valueDescription = "<fqname>",
            description = "Mask for Strings annotated with @Secret"
        ),
        CliOption(
            optionName = KEY_SECRET_MASK_NULL.toString(), valueDescription = "<true|false>",
            description = "Apply mask to nullable values or not"
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (option.optionName) {
            KEY_SECRET_MASK.toString() -> configuration.put(KEY_SECRET_MASK, value)
            KEY_SECRET_MASK_NULL.toString() -> configuration.put(KEY_SECRET_MASK_NULL, value.toBoolean())
        }
    }
}

val KEY_SECRET_MASK = CompilerConfigurationKey<String>("secretMask")
val KEY_SECRET_MASK_NULL = CompilerConfigurationKey<Boolean>("secretMaskNull")