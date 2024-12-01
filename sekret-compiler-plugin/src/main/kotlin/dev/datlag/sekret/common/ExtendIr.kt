package dev.datlag.sekret.common

import dev.datlag.sekret.model.JavaSignatureValues
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.builders.declarations.UNDEFINED_PARAMETER_INDEX
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrProperty.hasMatchingAnnotation(
    name: FqName,
    parent: IrClass?,
    checkGetter: Boolean = false,
    checkSetter: Boolean = false
): Boolean {
    return this.hasAnnotation(name) || this.originalProperty.hasAnnotation(name) || run {
        if (checkGetter && parent != null) {
            runCatching {
                (runCatching {
                    parent.getPropertyGetter(this.name.asString())
                }.getOrNull() ?: runCatching {
                    parent.getPropertyGetter(this.name.asStringStripSpecialMarkers())
                }.getOrNull())?.owner?.hasAnnotation(name)
            }.getOrNull() == true
        } else {
            false
        }
    } || run {
        if (checkSetter && parent != null) {
            runCatching {
                (runCatching {
                    parent.getPropertySetter(this.name.asString())
                }.getOrNull() ?: runCatching {
                    parent.getPropertySetter(this.name.asStringStripSpecialMarkers())
                }.getOrNull())?.owner?.hasAnnotation(name)
            }.getOrNull() == true
        } else {
            false
        }
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrField.hasMatchingAnnotation(
    name: FqName,
    parent: IrClass?,
    checkGetter: Boolean = false,
    checkSetter: Boolean = false,
): Boolean {
    return this.hasAnnotation(name)
            || this.type.hasAnnotation(name)
            || runCatching {
        this.correspondingPropertySymbol?.owner?.hasMatchingAnnotation(name, parent, checkGetter, checkSetter)
    }.getOrNull() ?: false
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrField.matchesProperty(property: IrProperty): Boolean {
    return if (this.isPropertyField) {
        runCatching {
            this.correspondingPropertySymbol?.owner == property
        }.getOrNull() ?: false || this.name == property.name
    } else {
        false
    }
}

fun IrField.matchesAnyProperty(properties: Iterable<IrProperty>): Boolean {
    return properties.any { this.matchesProperty(it) }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrType.matches(signature: IdSignature.CommonSignature, nullable: Boolean? = null): Boolean {
    if (this !is IrSimpleType) return false
    if (nullable != null && this.isMarkedNullable() != nullable) return false
    return signature == classifier.signature || runCatching {
        classifier.owner.let { it is IrClass && it.signatureMatchesFqName(signature) }
    }.getOrNull() ?: false
}

fun IrClass.signatureMatchesFqName(signature: IdSignature.CommonSignature): Boolean =
    name.asString() == signature.shortName &&
            hasEqualFqName(FqName("${signature.packageFqName}.${signature.declarationFqName}"))

fun IrType.matchesPossibleNull(signature: IdSignature.CommonSignature, nullable: Boolean = false): Boolean {
    return matches(signature, false) || (nullable && matches(signature, true))
}

fun IrType.isAnyString(nullable: Boolean = false): Boolean {
    return this.matchesPossibleNull(IdSignatureValues.string, nullable = nullable)
            || this.matchesPossibleNull(JavaSignatureValues.string, nullable = nullable)
}

fun IrType.isAnyCharSequence(nullable: Boolean = false): Boolean {
    return this.matchesPossibleNull(IdSignatureValues.charSequence, nullable = nullable)
            || this.matchesPossibleNull(JavaSignatureValues.charSequence, nullable = nullable)
}

fun IrType.isAnyStringBuilder(nullable: Boolean = false): Boolean {
    return this.matchesPossibleNull(
        getPublicSignature(StandardNames.TEXT_PACKAGE_FQ_NAME, "StringBuilder"),
        nullable = nullable
    ) || this.matchesPossibleNull(JavaSignatureValues.stringBuilder, nullable = nullable)
}

fun IrType.isAnyAppendable(nullable: Boolean = false): Boolean {
    return this.matchesPossibleNull(
        getPublicSignature(StandardNames.TEXT_PACKAGE_FQ_NAME, "Appendable"),
        nullable = nullable
    ) || this.matchesPossibleNull(JavaSignatureValues.appendable, nullable = nullable)
}

fun IrType.isStringBuffer(nullable: Boolean = false): Boolean {
    return this.matchesPossibleNull(JavaSignatureValues.stringBuffer, nullable = nullable)
}

fun IrClass.declareThisReceiver(
    irFactory: IrFactory,
    thisType: IrType,
    thisOrigin: IrDeclarationOrigin,
    startOffset: Int = this.startOffset,
    endOffset: Int = this.endOffset,
    name: Name = SpecialNames.THIS
) {
    thisReceiver = irFactory.createValueParameter(
        startOffset = startOffset,
        endOffset = endOffset,
        origin = thisOrigin,
        name = name,
        type = thisType,
        isAssignable = false,
        symbol = IrValueParameterSymbolImpl(),
        index = UNDEFINED_PARAMETER_INDEX,
        varargElementType = null,
        isCrossinline = false,
        isNoinline = false,
        isHidden = false
    ).apply {
        this.parent = this@declareThisReceiver
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrClass.declareObjectConstructor(
    unitType: IrType,
    irFactory: IrFactory,
    irConstructorSymbol: IrConstructorSymbol
) {
    this.addConstructor {
        isPrimary = true
        returnType = this@declareObjectConstructor.typeWith()
    }.apply {
        body = irFactory.createBlockBody(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET
        ).apply {
            statements += IrDelegatingConstructorCallImpl.fromSymbolOwner(
                startOffset = SYNTHETIC_OFFSET,
                endOffset = SYNTHETIC_OFFSET,
                type = this@declareObjectConstructor.typeWith(),
                symbol = irConstructorSymbol
            )

            statements += IrInstanceInitializerCallImpl(
                startOffset = SYNTHETIC_OFFSET,
                endOffset = SYNTHETIC_OFFSET,
                classSymbol = this@declareObjectConstructor.symbol,
                type = unitType
            )
        }
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
fun IrClass.declareObjectConstructor(
    pluginContext: IrPluginContext
) = this.declareObjectConstructor(
    unitType = pluginContext.irBuiltIns.unitType,
    irFactory = pluginContext.irFactory,
    irConstructorSymbol = pluginContext.irBuiltIns.anyClass.constructors.first()
)