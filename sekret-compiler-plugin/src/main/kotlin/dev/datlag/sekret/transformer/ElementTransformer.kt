package dev.datlag.sekret.transformer

import dev.datlag.sekret.model.Config
import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.*
import dev.datlag.sekret.generator.DeobfuscatorGenerator
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.lower.isInitFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*

class ElementTransformer(
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val obfuscateAnnotation = FqName.fromSegments(listOf("dev.datlag.sekret", "Obfuscate"))

    override fun visitClassNew(declaration: IrClass): IrStatement {
        val hasObfuscate = declaration.hasAnnotation(obfuscateAnnotation)
        val secretAnnotation = FqName.fromSegments(listOf("dev.datlag.sekret", "Secret"))

        val secretProperties = declaration.properties.filter { it.hasMatchingAnnotation(secretAnnotation, declaration) }
        if (secretProperties.count() > 0) {
            declaration.getSimpleFunction("toString")?.owner?.transformChildren(
                ToStringTransformer(secretProperties.toList(), config, logger, pluginContext),
                null
            )
        }

        if (hasObfuscate && DeobfuscatorGenerator.exists) {
            declaration.transformChildren(DeobfuscatorTransformer(logger, pluginContext), null)
            /**
             * Working example:
             * toString.owner.body = DeclarationIrBuilder(pluginContext, toString).irBlockBody {
             *                     +irReturn(irCall(getString))
             *                 }
             */
        }

        return super.visitClassNew(declaration)
    }

    override fun visitFileNew(declaration: IrFile): IrFile {
        if (declaration.hasAnnotation(obfuscateAnnotation) && DeobfuscatorGenerator.exists) {
            declaration.transformChildren(DeobfuscatorTransformer(logger, pluginContext), null)
        }

        return super.visitFileNew(declaration)
    }

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        if (declaration.hasAnnotation(obfuscateAnnotation) && DeobfuscatorGenerator.exists) {
            declaration.transformChildren(DeobfuscatorTransformer(logger, pluginContext), null)
        }

        return super.visitConstructor(declaration)
    }
}