package dev.datlag.sekret.common

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
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