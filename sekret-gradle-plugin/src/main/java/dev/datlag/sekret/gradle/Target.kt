package dev.datlag.sekret.gradle

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import kotlinx.serialization.Serializable
import org.gradle.api.Named
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrSubTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import java.io.Serializable as JvmSerializable

@Serializable
sealed class Target(open val title: String, open val sourceSet: String = title) : JvmSerializable, Named {

    override fun getName(): String = title

    sealed class Android(
        override val title: String,
        override val sourceSet: String = title
    ) : Target(title, sourceSet), JvmSerializable, Named {

        object NATIVE_32 : Android("androidNativeX86"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_64 : Android("androidNativeX64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_32 : Android("androidNativeArm32"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_64 : Android("androidNativeArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object JVM : Android("androidTarget", "android"), JvmSerializable, Named {
            override val requiredPlugin: String = "com.android.library"
        }
    }

    @Serializable
    sealed interface Apple : JvmSerializable, Named

    sealed class Desktop(
        override val title: String
    ) : Target(title), JvmSerializable, Named {

        sealed class Linux(override val title: String) : Desktop(title), JvmSerializable, Named {

            object NATIVE_64 : Linux("linuxX64"), JvmSerializable, Named {
                override val isNative: Boolean = true
            }

            object NATIVE_ARM_64 : Linux("linuxArm64"), JvmSerializable, Named {
                override val isNative: Boolean = true
            }
        }

        sealed class Mac(override val title: String) : Desktop(title), Apple, JvmSerializable, Named {

            object NATIVE_64 : Mac("macosX64"), JvmSerializable, Named {
                override val isNative: Boolean = true
            }

            object NATIVE_ARM_64 : Mac("macosArm64"), JvmSerializable, Named {
                override val isNative: Boolean = true
            }
        }

        object Windows : Desktop("mingwX64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object JVM : Desktop("jvm"), JvmSerializable, Named
    }

    sealed class JS(override val title: String) : Target(title), JvmSerializable, Named {

        object Default : JS("js"), JvmSerializable, Named

        object WASM : JS("wasmJs"), JvmSerializable, Named

        object WASI : JS("wasmWasi"), JvmSerializable, Named
    }

    sealed class IOS(override val title: String) : Target(title), Apple, JvmSerializable, Named {

        object NATIVE_64 : IOS("iosX64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_64 : IOS("iosArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_SIMULATOR_ARM_64 : IOS("iosSimulatorArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }
    }

    sealed class TVOS(override val title: String) : Target(title), Apple, JvmSerializable, Named {

        object NATIVE_64 : TVOS("tvosX64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_64 : TVOS("tvosArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_SIMULATOR_ARM_64 : TVOS("tvosSimulatorArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }
    }

    sealed class WATCHOS(override val title: String) : Target(title), Apple, JvmSerializable, Named {

        object NATIVE_64 : WATCHOS("watchosX64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_32 : WATCHOS("watchosArm32"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_ARM_64 : WATCHOS("watchosArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }

        object NATIVE_SIMULATOR_ARM_64 : WATCHOS("watchosSimulatorArm64"), JvmSerializable, Named {
            override val isNative: Boolean = true
        }
    }

    open val requiredPlugin: String = "multiplatform"
    open val isNative: Boolean = false

    val isAndroid: Boolean
        get() = this is Android

    val isAndroidJvm: Boolean
        get() = this is Android.JVM

    val isAndroidNative: Boolean
        get() = isAndroid && isNative

    val isDesktop: Boolean
        get() = this is Desktop

    val isDesktopJvm: Boolean
        get() = this is Desktop.JVM

    val isJvm: Boolean
        get() = isAndroidJvm || isDesktopJvm

    val isLinux: Boolean
        get() = this is Desktop.Linux

    val isMac: Boolean
        get() = this is Desktop.Mac

    val isJS: Boolean
        get() = this is JS

    val isApple: Boolean
        get() = this is Apple

    val isIOS: Boolean
        get() = this is IOS

    val isTVOS: Boolean
        get() = this is TVOS

    val isWatchOS: Boolean
        get() = this is WATCHOS

    fun matchesName(name: String): Boolean {
        return this.title.equals(name, true) || this.sourceSet.equals(name, true)
    }

    companion object {
        private const val ENDING_MAIN = "Main"
        private const val ENDING_TEST = "Test"
        private const val ENDING_DEBUG = "Debug"
        private const val ENDING_RELEASE = "Release"
        private const val ENDING_STAGING = "Staging"
        private const val ENDING_PRODUCTION = "Production"

        fun fromKotlinTargets(targets: Iterable<KotlinTarget>): Set<Target> {
            val sourceTargetMapped = targets.mapNotNull { target ->
                when (target) {
                    is KotlinJvmTarget -> Desktop.JVM
                    is KotlinAndroidTarget -> Android.JVM
                    is KotlinJsIrTarget, is KotlinJsIrSubTarget -> JS.Default
                    else -> null
                }
            }

            val platformTypeMapped = targets.mapNotNull { target ->
                when (target.platformType) {
                    KotlinPlatformType.jvm -> Desktop.JVM
                    KotlinPlatformType.androidJvm -> Android.JVM
                    KotlinPlatformType.js -> JS.Default
                    KotlinPlatformType.wasm -> JS.WASM
                    else -> null
                }
            }

            val namesMapped = fromSourceSetNames(
                targets.map {
                    listOf(
                        it.name,
                        it.targetName
                    )
                }.flatten()
            )

            return setOf(
                sourceTargetMapped,
                platformTypeMapped,
                namesMapped
            ).flatten().toSet()
        }

        fun fromSourceSetNames(names: Iterable<String>): Set<Target> {
            val flatNames = names.map { name ->
                if (name.endsWith(ENDING_MAIN)) {
                    name.substringBeforeLast(ENDING_MAIN)
                } else if (name.endsWith(ENDING_TEST)) {
                    name.substringBeforeLast(ENDING_TEST)
                } else if (name.endsWith(ENDING_DEBUG)) {
                    name.substringBeforeLast(ENDING_DEBUG)
                } else if (name.endsWith(ENDING_RELEASE)) {
                    name.substringBeforeLast(ENDING_RELEASE)
                } else if (name.endsWith(ENDING_STAGING)) {
                    name.substringBeforeLast(ENDING_STAGING)
                } else if (name.endsWith(ENDING_PRODUCTION)) {
                    name.substringBeforeLast(ENDING_PRODUCTION)
                } else {
                    name
                }
            }

            return flatNames.mapNotNull { name ->
                when {
                    Android.NATIVE_32.matchesName(name) -> Android.NATIVE_32
                    Android.NATIVE_64.matchesName(name) -> Android.NATIVE_64
                    Android.NATIVE_ARM_32.matchesName(name) -> Android.NATIVE_ARM_32
                    Android.NATIVE_ARM_64.matchesName(name) -> Android.NATIVE_ARM_64
                    Android.JVM.matchesName(name) -> Android.JVM

                    Desktop.Linux.NATIVE_64.matchesName(name) -> Desktop.Linux.NATIVE_64
                    Desktop.Linux.NATIVE_ARM_64.matchesName(name) -> Desktop.Linux.NATIVE_ARM_64

                    Desktop.Mac.NATIVE_64.matchesName(name) -> Desktop.Mac.NATIVE_64
                    Desktop.Mac.NATIVE_ARM_64.matchesName(name) -> Desktop.Mac.NATIVE_ARM_64

                    Desktop.Windows.matchesName(name) -> Desktop.Windows
                    Desktop.JVM.matchesName(name) -> Desktop.JVM

                    JS.Default.matchesName(name) -> JS.Default
                    JS.WASM.matchesName(name) -> JS.WASM
                    JS.WASI.matchesName(name) -> JS.WASI

                    IOS.NATIVE_64.matchesName(name) -> IOS.NATIVE_64
                    IOS.NATIVE_ARM_64.matchesName(name) -> IOS.NATIVE_ARM_64
                    IOS.NATIVE_SIMULATOR_ARM_64.matchesName(name) -> IOS.NATIVE_SIMULATOR_ARM_64

                    TVOS.NATIVE_64.matchesName(name) -> TVOS.NATIVE_64
                    TVOS.NATIVE_ARM_64.matchesName(name) -> TVOS.NATIVE_ARM_64
                    TVOS.NATIVE_SIMULATOR_ARM_64.matchesName(name) -> TVOS.NATIVE_SIMULATOR_ARM_64

                    WATCHOS.NATIVE_64.matchesName(name) -> WATCHOS.NATIVE_64
                    WATCHOS.NATIVE_ARM_32.matchesName(name) -> WATCHOS.NATIVE_ARM_32
                    WATCHOS.NATIVE_ARM_64.matchesName(name) -> WATCHOS.NATIVE_ARM_64
                    WATCHOS.NATIVE_SIMULATOR_ARM_64.matchesName(name) -> WATCHOS.NATIVE_SIMULATOR_ARM_64

                    else -> null
                }
            }.toSet()
        }

        fun addDependingTargets(default: Iterable<Target>): Set<Target> {
            val list = mutableSetOf<Target>()
            list.addAll(default)

            if (default.any { it.isAndroidJvm }) {
                list.add(Android.NATIVE_32)
                list.add(Android.NATIVE_64)
                list.add(Android.NATIVE_ARM_32)
                list.add(Android.NATIVE_ARM_64)
            }

            if (default.any { it.isDesktopJvm }) {
                list.add(Desktop.Linux.NATIVE_64)
                list.add(Desktop.Linux.NATIVE_ARM_64)
                list.add(Desktop.Mac.NATIVE_64)
                list.add(Desktop.Mac.NATIVE_ARM_64)
                list.add(Desktop.Windows)
            }

            return list
        }
    }
}