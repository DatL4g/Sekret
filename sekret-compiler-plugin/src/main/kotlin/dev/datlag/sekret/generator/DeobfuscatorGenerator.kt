package dev.datlag.sekret.generator

import dev.datlag.sekret.common.declareObjectConstructor
import dev.datlag.sekret.common.declareThisReceiver
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.name.Name

object DeobfuscatorGenerator {

    internal var irClass: IrClass? = null
        private set

    val exists: Boolean
        get() = irClass != null

    fun createIrClass(pluginContext: IrPluginContext): IrClass {
        val genClass = pluginContext.irFactory.buildClass {
            kind = ClassKind.OBJECT
            name = Name.identifier("Deobfuscator")
        }
        genClass.declareThisReceiver(
            irFactory = pluginContext.irFactory,
            thisType = IrSimpleTypeImpl(
                classifier = genClass.symbol,
                hasQuestionMark = false,
                arguments = emptyList(),
                annotations = emptyList()
            ),
            thisOrigin = IrDeclarationOrigin.INSTANCE_RECEIVER
        )
        genClass.declareObjectConstructor(pluginContext)
        genClass.addFunction {
            name = Name.identifier("getString")
            returnType = pluginContext.irBuiltIns.stringType
        }.also { function ->
            function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
                +irReturn(irString("Generated getString method"))
            }
        }

        return genClass.also { irClass = it }
    }
}