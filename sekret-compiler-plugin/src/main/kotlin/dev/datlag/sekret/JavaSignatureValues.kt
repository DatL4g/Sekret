package dev.datlag.sekret

import org.jetbrains.kotlin.ir.types.getPublicSignature

object JavaSignatureValues {
    @JvmField val string = getPublicSignature(JavaStandardNames.LANG_PACKAGE_FQ_NAME, "String")
    @JvmField val charSequence = getPublicSignature(JavaStandardNames.LANG_PACKAGE_FQ_NAME, "CharSequence")
    @JvmField val stringBuilder = getPublicSignature(JavaStandardNames.LANG_PACKAGE_FQ_NAME, "StringBuilder")
    @JvmField val appendable = getPublicSignature(JavaStandardNames.LANG_PACKAGE_FQ_NAME, "Appendable")
}