package dev.datlag.sekret

import dev.datlag.sekret.common.hasMatchingAnnotation
import dev.datlag.sekret.common.matchesAnyProperty
import dev.datlag.sekret.common.matchesProperty
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.wasm.ir2wasm.allFields
import org.jetbrains.kotlin.backend.wasm.utils.hasWasmAutoboxedAnnotation
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.isNullableString
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.typeConstructorParameters
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.annotations.findSynchronizedAnnotation
import org.jetbrains.kotlin.resolve.source.getPsi

class ElementTransformer(
    private val config: Config,
    private val logger: Logger,
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {
    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitClassNew(declaration: IrClass): IrStatement {
        val hasObfuscate = declaration.hasAnnotation(FqName.fromSegments(listOf("dev.datlag.sekret", "Obfuscate")))
        val secretAnnotation = FqName.fromSegments(listOf("dev.datlag.sekret", "Secret"))

        val secretProperties = declaration.properties.filter { it.hasMatchingAnnotation(secretAnnotation, declaration) }
        if (secretProperties.count() > 0) {
            declaration.getSimpleFunction("toString")?.owner?.transform(
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
        val stringField = expression.type.isString() || (config.secretMaskNull && expression.type.isNullableString())
        if (stringField) {
            if (expression.symbol.owner.matchesAnyProperty(secretProperties)) {
                return DeclarationIrBuilder(pluginContext, expression.symbol).irString(config.secretMask)
            }
        }
        return super.visitGetField(expression)
    }
}