package dev.datlag.sekret.gradle.builder

import dev.datlag.sekret.gradle.createEmpty
import java.io.File

object BuildFile {

    fun create(
        file: File,
        deletePrevious: Boolean
    ) {
        if (file.createEmpty(deletePrevious)) {
            file.writeText("""
                plugins {
                    kotlin("multiplatform")
                }
                
                repositories {
                    mavenCentral()
                    maven { url = uri("https://jitpack.io") }
                }
                
                kotlin {
                    val nativeTarget = when (getHost()) {
                        Host.LINUX -> linuxX64("native")
                        Host.MAC -> macosX64("native")
                        Host.WINDOWS -> mingwX64("native")
                    }
                    
                    nativeTarget.apply {
                        binaries {
                            sharedLib()
                        }
                        compilations["main"].cinterops.create("sekret") {
                            val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
                
                            includeDirs(
                                Callable { File(javaHome, "include") },
                                Callable { File(javaHome, "include/darwin") },
                                Callable { File(javaHome, "include/linux") },
                                Callable { File(javaHome, "include/win32") }
                            )
                        }
                    }
                    
                    jvm()
                    
                    sourceSets {
                        val commonMain by getting {
                            
                        }
                        val nativeMain by getting {
                            dependencies {
                                implementation("com.github.komputing.khash:sha256:1.1.3")
                            }
                        }
                        val jvmMain by getting {
                            dependsOn(commonMain)
                        }
                    }
                }
                
                fun getHost(): Host {
                    val hostOs = System.getProperty("os.name")
                    
                    return when {
                        hostOs.equals("Linux", true) -> Host.LINUX
                        hostOs.equals("Mac OS X", true) -> Host.MAC
                        hostOs.startsWith("Windows", true) -> Host.WINDOWS
                        else -> throw IllegalStateException("Unknown OS: " + hostOs)
                    }
                }
                
                enum class Host {
                    LINUX,
                    WINDOWS,
                    MAC;
                }
            """.trimIndent())
        }
    }

}