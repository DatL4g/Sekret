package dev.datlag.sekret

import com.google.auto.service.AutoService
import dev.datlag.sekret.model.Config
import dev.datlag.sekret.transformer.ElementTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.security.SecureRandom

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class SekretComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override val pluginId: String = SekretCommandLineProcessor.PLUGIN_ID

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY, configuration.messageCollector)
        val logger = Logger(true, messageCollector)

        val config = Config(
            secretMask = configuration[KEY_SECRET_MASK, "***"],
            secretMaskNull = configuration[KEY_SECRET_MASK_NULL, true],
            obfuscateSeed = configuration[KEY_OBFUSCATE_SEED, SecureRandom().nextInt()]
        )

        IrGenerationExtension.registerExtension(object : IrGenerationExtension {
            override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
                moduleFragment.transform(
                    transformer = ElementTransformer(config, logger, pluginContext),
                    data = null
                )
            }
        })
    }
}