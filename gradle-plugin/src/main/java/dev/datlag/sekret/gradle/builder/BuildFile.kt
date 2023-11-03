package dev.datlag.sekret.gradle.builder

import dev.datlag.sekret.gradle.createEmpty
import java.io.File

object BuildFile {

    fun create(
        file: File,
        version: String,
        deletePrevious: Boolean
    ) {
        if (file.createEmpty(deletePrevious)) {
            file.writeText("""
                plugins {
                    kotlin("multiplatform")
                    id("com.android.library")
                }
                
                repositories {
                    mavenCentral()
                }
                
                kotlin {
                    // native targets, for building native library
                    listOf(
                        androidNativeX86(),
                        androidNativeX64(),
                        androidNativeArm32(),
                        androidNativeArm64(),
                        linuxX64(),
                        linuxArm64(),
                        mingwX64()
                    ).forEach { target ->
                        target.binaries {
                            sharedLib()
                        }
                        target.compilations["main"].cinterops.create("sekret") {
                            val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")
                            
                            includeDirs(
                                Callable { File(javaHome, "include") },
                                Callable { File(javaHome, "include/darwin") },
                                Callable { File(javaHome, "include/linux") },
                                Callable { File(javaHome, "include/win32") },
                            )
                        }
                    }
                    
                    // other targets, for Sekret class
                    androidTarget()
                    jvm()
                    
                    sourceSets {
                        val commonMain by getting {
                            dependencies {
                                api("dev.datlag.sekret:library:$version")
                            }
                        }
                    }
                }
                
                android {
                    compileSdk = 21
                    namespace = "dev.datlag.sekret"
                }
            """.trimIndent())
        }
    }

}