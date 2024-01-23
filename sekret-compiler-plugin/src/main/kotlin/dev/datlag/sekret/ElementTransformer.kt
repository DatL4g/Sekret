package dev.datlag.sekret

import dev.datlag.sekret.common.*
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName

class ElementTransformer(
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    override fun visitClassNew(declaration: IrClass): IrStatement {
        val hasObfuscate = declaration.hasAnnotation(FqName.fromSegments(listOf("dev.datlag.sekret", "Obfuscate")))
        val secretAnnotation = FqName.fromSegments(listOf("dev.datlag.sekret", "Secret"))

        val secretProperties = declaration.properties.filter { it.hasMatchingAnnotation(secretAnnotation, declaration) }
        if (secretProperties.count() > 0) {
            declaration.getSimpleFunction("toString")?.owner?.transformChildren(
                ToStringTransformer(secretProperties.toList(), config, logger, pluginContext),
                null
            )
        }

        return super.visitClassNew(declaration)
    }
}

class ToStringTransformer(
    private val secretProperties: Collection<IrProperty>,
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    override fun visitGetField(expression: IrGetField): IrExpression {
        val string = expression.type.isAnyString(config.secretMaskNull)
        val charSequence = expression.type.isAnyCharSequence(config.secretMaskNull)
        val stringBuilder = expression.type.isAnyStringBuilder(config.secretMaskNull)
        val appendable = expression.type.isAnyAppendable(config.secretMaskNull)

        if (string || charSequence || stringBuilder || appendable) {
            if (expression.symbol.owner.matchesAnyProperty(secretProperties)) {
                return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
            }
        }
        return super.visitGetField(expression)
    }
}