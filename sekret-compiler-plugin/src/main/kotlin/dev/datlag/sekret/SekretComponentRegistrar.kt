package dev.datlag.sekret

import com.google.auto.service.AutoService
import dev.datlag.sekret.generator.DeobfuscatorGenerator
import dev.datlag.sekret.model.Config
import dev.datlag.sekret.transformer.DeobfuscatorTransformer
import dev.datlag.sekret.transformer.ElementTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.irMessageLogger

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class SekretComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        // available for jvm: ClassGeneratorExtension
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val logger = Logger(true, messageCollector, configuration.irMessageLogger)

        val config = Config(
            secretMask = configuration[KEY_SECRET_MASK, "***"],
            secretMaskNull = configuration[KEY_SECRET_MASK_NULL, true]
        )

        IrGenerationExtension.registerExtension(object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
                val generatedDeobfuscatorModule = moduleFragment.transform(
                    transformer = DeobfuscatorTransformer(logger, pluginContext),
                    data = null
                )

                generatedDeobfuscatorModule.transform(
                    transformer = ElementTransformer(config, logger, pluginContext),
                    data = null
                )
            }
        })
    }
}