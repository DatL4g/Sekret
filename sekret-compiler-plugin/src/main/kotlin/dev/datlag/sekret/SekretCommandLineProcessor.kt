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
    override val pluginId: String = PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = KEY_SECRET_MASK.toString(), valueDescription = "string",
            description = "Mask for Strings annotated with @Secret"
        ),
        CliOption(
            optionName = KEY_SECRET_MASK_NULL.toString(), valueDescription = "<true|false>",
            description = "Apply mask to nullable values or not"
        ),
        CliOption(
            optionName = KEY_OBFUSCATE_SEED.toString(), valueDescription = "int",
            description = "Change seed for Obfuscation random",
            required = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        super.processOption(option, value, configuration)

        when (option.optionName) {
            KEY_SECRET_MASK.toString() -> configuration.put(KEY_SECRET_MASK, value)
            KEY_SECRET_MASK_NULL.toString() -> configuration.put(KEY_SECRET_MASK_NULL, value.toBoolean())
            KEY_OBFUSCATE_SEED.toString() -> configuration.put(KEY_OBFUSCATE_SEED, value.toInt())
        }
    }

    companion object {
        internal const val PLUGIN_ID = "sekretPlugin"
    }
}

val KEY_SECRET_MASK = CompilerConfigurationKey<String>("secretMask")
val KEY_SECRET_MASK_NULL = CompilerConfigurationKey<Boolean>("secretMaskNull")
val KEY_OBFUSCATE_SEED = CompilerConfigurationKey<Int>("obfuscateSeed")