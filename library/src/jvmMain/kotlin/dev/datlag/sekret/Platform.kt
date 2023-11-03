package dev.datlag.sekret

import dev.datlag.sekret.common.systemProperty
import java.util.*

data object Platform {

    private const val PROPERTY_OS_NAME = "os.name"

    val os: OS? by lazy(LazyThreadSafetyMode.NONE) {
        val osName = systemProperty(PROPERTY_OS_NAME)

        osName?.let { OS.matching(it) }
    }

    sealed class OS(open val name: String, private vararg val values: String) {
        data class MACOSX(override val name: String) : OS(name, "mac", "darwin", "osx")
        data class LINUX(override val name: String) : OS(name, "linux", "unix")
        data class WINDOWS(override val name: String) : OS(name, "win", "windows")

        val isMacOSX: Boolean
            get() = this is MACOSX

        val isLinux: Boolean
            get() = this is LINUX

        val isWindows: Boolean
            get() = this is WINDOWS

        internal fun matches(osName: String): Boolean {
            return this.values.contains(osName.lowercase(Locale.ENGLISH)) || this.values.any {
                osName.startsWith(it, true)
            }
        }

        companion object {
            internal fun matching(osName: String): OS? = when {
                MACOSX(osName).matches(osName) -> MACOSX(osName)
                LINUX(osName).matches(osName) -> LINUX(osName)
                WINDOWS(osName).matches(osName) -> WINDOWS(osName)
                else -> null
            }
        }
    }

}