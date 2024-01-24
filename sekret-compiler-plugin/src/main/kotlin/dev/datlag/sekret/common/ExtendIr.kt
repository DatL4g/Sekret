package dev.datlag.sekret.common

import dev.datlag.sekret.model.JavaSignatureValues
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.builders.declarations.UNDEFINED_PARAMETER_INDEX
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

fun IrProperty.hasMatchingAnnotation(
    name: FqName,
    parent: IrClass?,
    checkGetter: Boolean = false,
    checkSetter: Boolean = false
): Boolean {
    return this.hasAnnotation(name) || this.originalProperty.hasAnnotation(name) || run {
        if (checkGetter && parent != null) {
            (runCatching {
                parent.getPropertyGetter(this.name.asString())
            }.getOrNull() ?: runCatching {
                parent.getPropertyGetter(this.name.asStringStripSpecialMarkers())
            }.getOrNull())?.owner?.hasAnnotation(name) == true
        } else {
            false
        }
    } || run {
        if (checkSetter && parent != null) {
            (runCatching {
                parent.getPropertySetter(this.name.asString())
            }.getOrNull() ?: runCatching {
                parent.getPropertySetter(this.name.asStringStripSpecialMarkers())
            }.getOrNull())?.owner?.hasAnnotation(name) == true
        } else {
            false
        }
    }
}

fun IrField.hasMatchingAnnotation(
    name: FqName,
    parent: IrClass?,
    checkGetter: Boolean = false,
    checkSetter: Boolean = false,
): Boolean {
    return this.hasAnnotation(name)
            || this.type.hasAnnotation(name)
            || this.correspondingPropertySymbol?.owner?.hasMatchingAnnotation(name, parent, checkGetter, checkSetter) ?: false
}

fun IrField.matchesProperty(property: IrProperty): Boolean {
    return if (this.isPropertyField) {
        this.correspondingPropertySymbol?.owner == property || this.name == property.name
    } else {
        false
    }
}

fun IrField.matchesAnyProperty(properties: Iterable<IrProperty>): Boolean {
    return properties.any { this.matchesProperty(it) }
}

fun IrType.matches(signature: IdSignature.CommonSignature, nullable: Boolean? = null): Boolean {
    if (this !is IrSimpleType) return false
    if (nullable != null && this.isMarkedNullable() != nullable) return false
    return signature == classifier.signature ||
            classifier.owner.let { it is IrClass && it.signatureMatchesFqName(signature) }
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

fun IrClass.declareObjectConstructor(
    unitType: IrType,
    irFactory: IrFactory,
) {
    this.addConstructor {
        isPrimary = true
        returnType = this@declareObjectConstructor.typeWith()
    }.apply {
        body = irFactory.createBlockBody(
            startOffset = SYNTHETIC_OFFSET,
            endOffset = SYNTHETIC_OFFSET
        ).apply {
            statements += IrInstanceInitializerCallImpl(
                startOffset = SYNTHETIC_OFFSET,
                endOffset = SYNTHETIC_OFFSET,
                classSymbol = this@declareObjectConstructor.symbol,
                type = unitType
            )
        }
    }
}