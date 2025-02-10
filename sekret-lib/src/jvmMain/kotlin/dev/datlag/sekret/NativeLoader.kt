package dev.datlag.sekret

import dev.datlag.sekret.common.systemLoad
import dev.datlag.sekret.common.systemLoadLibrary
import java.io.File

data object NativeLoader {

    /**
     * Load native library by name (and path)
     *
     * @param name the name of the native library
     * @param path the path where the library [name] is located
     *
     * @return true if the library was found and loaded
     */
    @JvmStatic
    @JvmOverloads
    fun loadLibrary(name: String, path: File? = null): Boolean {
        val currentOs = Platform.os
        val ending = when {
            currentOs?.isWindows == true -> ".dll"
            currentOs?.isLinux == true -> ".so"
            currentOs?.isMacOSX == true -> ".dylib"
            else -> ""
        }

        var pathLoaded = true
        systemLoad(File(path, name)) {
            systemLoad(File(path, name + ending)) {
                systemLoad(File(path, "lib$name")) {
                    systemLoad(File(path, "lib$name$ending")) {
                        pathLoaded = false
                    }
                }
            }
        }

        if (pathLoaded) {
            return true
        }

        var pathLibraryLoaded = true
        systemLoadLibrary(File(path, name)) {
            systemLoadLibrary(File(path, name + ending)) {
                systemLoadLibrary(File(path, "lib$name")) {
                    systemLoadLibrary(File(path, "lib$name$ending")) {
                        pathLibraryLoaded = false
                    }
                }
            }
        }

        if (pathLibraryLoaded) {
            return true
        }

        var libraryLoaded = true
        systemLoad(name) {
            systemLoad(name + ending) {
                systemLoad("lib$name") {
                    systemLoad("lib$name$ending") {
                        systemLoadLibrary(name) {
                            systemLoadLibrary(name + ending) {
                                systemLoadLibrary("lib$name") {
                                    systemLoadLibrary("lib$name$ending") {
                                        libraryLoaded = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return libraryLoaded
    }
}