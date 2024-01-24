package dev.datlag.sekret.transformer

import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.isAnyString
import dev.datlag.sekret.generator.DeobfuscatorGenerator
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
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

    override fun visitConst(expression: IrConst<*>): IrExpression {
        val getMethod = DeobfuscatorGenerator.getFunction
        if (getMethod != null) {
            if (expression.kind == IrConstKind.String || expression.type.isAnyString()) {
                val value = expression.value?.toString()
                if (value != null) {
                    val position = DeobfuscatorGenerator.addValue(value)

                }

                return super.visitConst(expression)

                // Working
                val getCall = DeclarationIrBuilder(pluginContext, pluginContext.symbols.string).irCall(getMethod)
                getCall.putValueArgument(0, DeclarationIrBuilder(pluginContext, pluginContext.symbols.int).irInt(1))
                return getCall
            }
        }
        return super.visitConst(expression)
    }
}