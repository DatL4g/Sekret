package dev.datlag.sekret.generator

import dev.datlag.sekret.Logger
import dev.datlag.sekret.common.declareObjectConstructor
import dev.datlag.sekret.common.declareThisReceiver
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.addDispatchReceiver
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

object DeobfuscatorGenerator {

    internal var irClass: IrClass? = null
        private set

    internal var getFunction: IrSimpleFunction? = null
        private set

    internal var valuesField: IrField? = null
        private set

    private val valueList: MutableList<String> = mutableListOf()

    val exists: Boolean
        get() = irClass != null

    private fun listGet(pluginContext: IrPluginContext) = pluginContext.irBuiltIns.listClass.owner.functions.single {
        it.name.asString() == "get" && it.valueParameters.size == 1
    }

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

        valuesField = genClass.addField {
            name = Name.identifier("values")
            type = pluginContext.irBuiltIns.listClass.createType(false, listOf(
                makeTypeProjection(
                    type = pluginContext.irBuiltIns.stringType,
                    variance = Variance.INVARIANT
                )
            ))
            isFinal = true
            isStatic = true
            visibility = DescriptorVisibilities.PRIVATE
        }

        getFunction = genClass.addFunction {
            name = Name.identifier("get")
            returnType = pluginContext.irBuiltIns.stringType
            isOperator = true
        }.also { function ->
            val pos = function.addValueParameter {
                name = Name.identifier("pos")
                type = pluginContext.irBuiltIns.intType
            }
            function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
                val field = irTemporary(irGetField(null, valuesField!!))

                +irReturn(
                    irCallOp(
                        callee = listGet(pluginContext).symbol,
                        type = function.returnType,
                        dispatchReceiver = irGet(field),
                        argument = irGet(pos)
                    )
                )
            }
        }

        return genClass.also { irClass = it }
    }

    fun addValue(value: String): Int {
        valueList.add(value)
        return valueList.lastIndex
    }

    fun generateList(pluginContext: IrPluginContext) {
        val listOfFunction = pluginContext.referenceFunctions(
            CallableId(
                packageName = FqName("kotlin.collections"),
                className = null,
                callableName = Name.identifier("listOf")
            )
        )

        val varargListOf = listOfFunction.firstNotNullOf { function ->
            val params = function.owner.valueParameters
            if (params.isNotEmpty()) {
                if (params.single().isVararg) {
                    function
                } else {
                    null
                }
            } else {
                null
            }
        }

        val applyParameter = DeclarationIrBuilder(pluginContext, varargListOf).irCall(varargListOf)

        val irStrings = valueList.map {
            DeclarationIrBuilder(pluginContext, pluginContext.symbols.string).irString(it)
        }
        val varargValue = DeclarationIrBuilder(pluginContext, varargListOf).irVararg(pluginContext.irBuiltIns.stringType, irStrings)
        applyParameter.putValueArgument(0, varargValue)

        valuesField?.let {
            it.initializer = DeclarationIrBuilder(pluginContext, it.symbol).irExprBody(applyParameter)
        }
    }
}