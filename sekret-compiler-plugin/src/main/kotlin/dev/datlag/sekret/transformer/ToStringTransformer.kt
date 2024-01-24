package dev.datlag.sekret.transformer

import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.*
import dev.datlag.sekret.model.Config
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField

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
        val stringBuffer = expression.type.isStringBuffer(config.secretMaskNull)

        if (string || charSequence || stringBuilder || appendable || stringBuffer) {
            if (expression.symbol.owner.matchesAnyProperty(secretProperties)) {
                return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
            }
        }
        return super.visitGetField(expression)
    }
}