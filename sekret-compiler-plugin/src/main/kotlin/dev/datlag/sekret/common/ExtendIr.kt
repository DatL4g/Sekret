package dev.datlag.sekret.common

import dev.datlag.sekret.model.JavaSignatureValues
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName

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