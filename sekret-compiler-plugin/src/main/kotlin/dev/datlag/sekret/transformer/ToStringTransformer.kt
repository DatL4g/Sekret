package dev.datlag.sekret.transformer

import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.*
import dev.datlag.sekret.model.Config
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.constantValue
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.isNullConst

class ToStringTransformer(
    private val secretProperties: Collection<IrProperty>,
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val maskField = mutableMapOf<IrField, Boolean>()

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitSetField(expression: IrSetField): IrExpression {
        if (config.secretMaskNull) {
            return super.visitSetField(expression)
        }

        val matches = runCatching {
            expression.symbol.owner.matchesAnyProperty(secretProperties)
        }.getOrNull() ?: false

        if (matches) {
            runCatching {
                expression.symbol.owner
            }.getOrNull()?.let {
                maskField[it] = !isNull(expression)
            }
        }

        return super.visitSetField(expression)
    }

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
                val mask = if (maskField.isEmpty()) {
                    config.secretMaskNull || !isNull(expression)
                } else {
                    runCatching {
                        maskField[expression.symbol.owner]
                    }.getOrNull() ?: config.secretMaskNull || !isNull(expression)
                }


                if (mask) {
                    return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
                }
            }
        }
        return super.visitGetField(expression)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private fun isNull(expression: IrExpression): Boolean {
        return when (expression) {
            is IrConst<*> -> {
                expression.kind == IrConstKind.Null || expression.value == null
            }
            is IrSetField -> {
                expression.value.isNullConst() || runCatching {
                    expression.symbol.owner.constantValue()?.let {
                        isNull(it)
                    }
                }.getOrNull() ?: false
            }
            is IrGetValue -> expression.isNullConst()
            else -> false
        }
    }
}