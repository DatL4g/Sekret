package dev.datlag.sekret.transformer

import dev.datlag.sekret.Logger
import dev.datlag.sekret.generator.DeobfuscatorGenerator
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.addChild

class DeobfuscatorTransformer(
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitPackageFragment(declaration: IrPackageFragment): IrPackageFragment {
        val result = super.visitPackageFragment(declaration)

        if (!DeobfuscatorGenerator.exists) {
            result.addChild(DeobfuscatorGenerator.createIrClass(pluginContext))
        }

        return result
    }
}