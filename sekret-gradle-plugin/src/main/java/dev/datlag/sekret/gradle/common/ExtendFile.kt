package dev.datlag.sekret.gradle.common

import java.io.File
import java.nio.file.Files

internal fun File?.existsSafely(): Boolean {
    if (this == null) {
        return false
    }

    return runCatching {
        Files.exists(this.toPath())
    }.getOrNull() ?: runCatching {
        this.exists()
    }.getOrNull() ?: false
}

internal fun File.mkdirsSafely(): Boolean {
    return runCatching {
        Files.createDirectories(this.toPath())
    }.getOrNull()?.toFile()?.existsSafely() ?: runCatching {
        this.mkdirs()
    }.getOrNull() ?: false
}

internal fun File.createEmpty(delete: Boolean): Boolean {
    fun create(): Boolean {
        if (this.existsSafely()) {
            this.deleteSafely()
        }

        return runCatching {
            Files.createFile(this.toPath())
        }.getOrNull()?.toFile()?.existsSafely() ?: runCatching {
            this.createNewFile()
        }.getOrNull() ?: false
    }

    return if (delete) {
        create()
    } else {
        if (!this.existsSafely()) {
            create()
        } else {
            false
        }
    }
}

internal fun File.deleteSafely(): Boolean {
    return runCatching {
        Files.delete(this.toPath())
    }.isSuccess || runCatching {
        this.delete()
    }.getOrNull() ?: false
}

internal fun File.canReadSafely(): Boolean {
    return runCatching {
        Files.isReadable(this.toPath())
    }.getOrNull() ?: runCatching {
        this.canRead()
    }.getOrNull() ?: false
}

internal fun File.canWriteSafely(): Boolean {
    return runCatching {
        Files.isWritable(this.toPath())
    }.getOrNull() ?: runCatching {
        this.canWrite()
    }.getOrNull() ?: false
}

internal fun File.isDirectorySafely(): Boolean {
    return runCatching {
        Files.isDirectory(this.toPath())
    }.getOrNull() ?: runCatching {
        this.isDirectory
    }.getOrNull() ?: false
}