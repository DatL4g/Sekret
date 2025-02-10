package dev.datlag.sekret

import android.content.Context
import android.os.Build
import com.getkeepsafe.relinker.ReLinker
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
    @RequestedApi(23)
    fun loadLibrary(name: String, path: File? = null): Boolean {
        val ending = ".so"

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

    /**
     * Load native library by name (and path)
     *
     * @param context the context in which the load process is handled
     * @param name the name of the native library
     * @param path the path where the library [name] is located
     *
     * @return true if the library was found and loaded
     */
    @OptIn(RequestedApi::class)
    @JvmStatic
    @JvmOverloads
    fun loadLibrary(context: Context, name: String, path: File? = null): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return loadLibrary(name, path)
        }

        val relinkerLoaded = runCatching {
            ReLinker.recursively().loadLibrary(context, name)
        }.recoverCatching {
            ReLinker.loadLibrary(context, name)
        }.isSuccess

        return loadLibrary(name, path) || relinkerLoaded
    }
}