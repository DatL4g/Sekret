package dev.datlag.sekret.gradle.builder

import dev.datlag.sekret.gradle.createEmpty
import java.io.File

object HeaderFile {

    fun create(
        file: File,
        deletePrevious: Boolean
    ) {
        if (file.createEmpty(deletePrevious)) {
            file.writeText("headers = jni.h")
        }
    }
}