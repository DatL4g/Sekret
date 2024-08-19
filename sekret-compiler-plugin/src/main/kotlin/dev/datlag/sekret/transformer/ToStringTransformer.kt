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
        val string = expression.type.isAnyString(nullable = true)
        val charSequence = expression.type.isAnyCharSequence(nullable = true)
        val stringBuilder = expression.type.isAnyStringBuilder(nullable = true)
        val appendable = expression.type.isAnyAppendable(nullable = true)
        val stringBuffer = expression.type.isStringBuffer(nullable = true)

        if (string || charSequence || stringBuilder || appendable || stringBuffer) {
            val matches = runCatching {
                expression.symbol.owner.matchesAnyProperty(secretProperties)
            }.getOrNull() ?: false

            if (matches) {
                if (config.secretMaskNull || !isNull(expression)) {
                    return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
                }
            }
        }
        return super.visitGetField(expression)
    }

    private fun isNull(expression: IrExpression): Boolean {
        return when (expression) {
            is IrConst<*> -> expression.value == null
            else -> false
        }
    }
}