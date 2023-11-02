package dev.datlag.sekret.gradle.helper

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import java.io.File
import java.util.Properties

object Utils {

    fun packageNameToFolderStructure(packageName: String): String {
        return packageName.replace(".", File.pathSeparator?.ifBlank { null } ?: "/")
    }

    fun packageNameCSave(packageName: String): String {
        return packageName
            .replace("_", "_1")
            .replace(";", "_2")
            .replace("[", "_3")
            .replace(".", "_")
    }

    fun optInAnnotation(type: TypeName): AnnotationSpec {
        return AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
            .addMember("%T::class", type)
            .build()
    }

    fun propertiesFromFile(propFile: File): Properties {
        return Properties().apply {
            propFile.inputStream().use {
                load(it)
            }
        }
    }

}