package dev.datlag.sekret

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object JavaStandardNames {

    @JvmField
    val BUILT_INS_PACKAGE_NAME = Name.identifier("java")

    @JvmField
    val BUILT_INS_PACKAGE_FQ_NAME = FqName.topLevel(BUILT_INS_PACKAGE_NAME)

    @JvmField
    val LANG_PACKAGE_FQ_NAME = BUILT_INS_PACKAGE_FQ_NAME.child(Name.identifier("lang"))
}