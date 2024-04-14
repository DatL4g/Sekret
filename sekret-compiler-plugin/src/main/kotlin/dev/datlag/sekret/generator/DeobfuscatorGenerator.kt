package dev.datlag.sekret.generator

import dev.datlag.sekret.Logger
import dev.datlag.sekret.SekretHelper
import dev.datlag.sekret.common.declareObjectConstructor
import dev.datlag.sekret.common.declareThisReceiver
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.wasm.ir2wasm.allFields
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.*
import org.jetbrains.kotlin.types.Variance
import kotlin.properties.Delegates

object DeobfuscatorGenerator {

    internal var irClass: IrClass? = null
        private set

    internal var getFunction: IrSimpleFunction? = null
        private set

    internal var valuesField: IrProperty? = null
        private set

    val exists: Boolean
        get() = irClass != null

    private fun listOfVararg(pluginContext: IrPluginContext): IrSimpleFunctionSymbol {
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
                if (params.first().isVararg) {
                    function
                } else {
                    null
                }
            } else {
                null
            }
        }

        return varargListOf
    }

    private var seed by Delegates.notNull<Long>()
    private val builder = StringBuilder()

    fun createIrClass(pluginContext: IrPluginContext, logger: Logger) {
        irClass = pluginContext.referenceClass(ClassId.fromString("dev/datlag/sekret/DeobfuscatorHelper"))?.owner

        logger.warn("Class found: ${irClass != null}")

        irClass?.fields?.firstNotNullOf {
            logger.warn(it.name.asString())
        }

        val props = irClass?.properties?.toList() ?: emptyList()
        valuesField = props.firstOrNull {
            it.name == Name.identifier("values")
        }

        irClass?.addFunction {
            name = Name.identifier("get")
            isOperator = true
            returnType = pluginContext.irBuiltIns.stringType
        }?.also { function ->
            val id = function.addValueParameter {
                name = Name.identifier("id")
                type = pluginContext.irBuiltIns.longType
            }

            function.body = DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
                +irReturn(irString("Generated"))
            }
        }
    }

    fun registerString(value: String): Long {
        var mask = 0L
        var state = SekretHelper.Random.seed(seed)
        state = SekretHelper.Random.next(state)
        mask = mask or (state and 0xffff_0000_0000L)
        state = SekretHelper.Random.next(state)
        mask = mask or ((state and 0xffff_0000_0000L) shl 16)

        val index = builder.length
        val id = seed or ((index.toLong() shl 32) xor mask)

        state = SekretHelper.Random.next(state)
        builder.append((((state ushr 32) and 0xffffL) xor value.length.toLong()).toInt().toChar())

        for (char in value) {
            state = SekretHelper.Random.next(state)
            builder.append((((state ushr 32) and 0xffffL) xor char.code.toLong()).toInt().toChar())
        }

        return id
    }

    fun getAllChunks(): List<String> {
        return builder.toString().chunked(0x1fff)
    }

    fun generateList(pluginContext: IrPluginContext, logger: Logger) {
        val varargListOf = listOfVararg(pluginContext)
        val applyParameter = DeclarationIrBuilder(pluginContext, varargListOf).irCall(varargListOf)

        val irStrings = getAllChunks().map {
            DeclarationIrBuilder(pluginContext, pluginContext.symbols.string).irString(it)
        }
        val varargValue = DeclarationIrBuilder(pluginContext, varargListOf).irVararg(pluginContext.irBuiltIns.stringType, irStrings)
        applyParameter.putValueArgument(0, varargValue)


        valuesField?.backingField?.let {
            it.initializer = DeclarationIrBuilder(pluginContext, it.symbol).irExprBody(applyParameter)
        }
    }

    fun setSeed(value: Int) {
        seed = value.toLong() and 0xffff_ffffL
    }
}