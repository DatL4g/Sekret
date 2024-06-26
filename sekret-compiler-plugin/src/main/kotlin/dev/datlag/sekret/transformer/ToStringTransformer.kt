package dev.datlag.sekret.transformer

import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.*
import dev.datlag.sekret.model.Config
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

class ToStringTransformer(
    private val secretProperties: Collection<IrProperty>,
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitGetField(expression: IrGetField): IrExpression {
        val string = expression.type.isAnyString(true)
        val charSequence = expression.type.isAnyCharSequence(true)
        val stringBuilder = expression.type.isAnyStringBuilder(true)
        val appendable = expression.type.isAnyAppendable(true)
        val stringBuffer = expression.type.isStringBuffer(true)

        if (string || charSequence || stringBuilder || appendable || stringBuffer) {
            val matches = runCatching {
                expression.symbol.owner.matchesAnyProperty(secretProperties)
            }.getOrNull() ?: false

            if (matches) {
                return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
            }
        }
        return super.visitGetField(expression)
    }
}