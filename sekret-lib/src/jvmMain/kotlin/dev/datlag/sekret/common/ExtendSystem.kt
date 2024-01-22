package dev.datlag.sekret.common

import java.io.File

internal fun systemProperty(key: String): String? = runCatching {
    System.getProperty(key).ifEmpty {
        null
    }
}.getOrNull()

@Suppress("UnsafeDynamicallyLoadedCode")
internal fun systemLoadLibrary(value: String, onError: () -> Unit = { }) {
    if (runCatching {
        System.loadLibrary(value)
    }.isFailure) {
        onError()
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
internal fun systemLoadLibrary(value: File, onError: () -> Unit = { }) {
    systemLoadLibrary(value.canonicalPath) {
        systemLoadLibrary(value.path, onError)
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
internal fun systemLoad(value: String, onError: () -> Unit = { }) {
    if (runCatching {
        System.load(value)
    }.isFailure) {
        onError()
    }
}

@Suppress("UnsafeDynamicallyLoadedCode")
internal fun systemLoad(value: File, onError: () -> Unit = { }) {
    systemLoad(value.canonicalPath) {
        systemLoad(value.path, onError)
    }
}