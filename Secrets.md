# Application Secrets

The Sekret plugin allows any Kotlin developer to deeply hide secrets in your project. This prevents **credentials harvesting** to a certain level.

The following obfuscation techniques are used:

- The secret is obfuscated using a reversible XOR operation, so it never appears as plain text
- Obfuscated secret is stored as a hexadecimal array, so it's hard to decompile
  - On the JVM (Desktop and Android) it's stored in a separate binary to force runtime evaluation
- The obfuscated string is not persisted in the binary to force runtime evaluation

This plugin is a more flexible and **Kotlin Multiplatform** compatible version of [klaxit/hidden-secrets-gradle-plugin](https://github.com/klaxit/hidden-secrets-gradle-plugin)

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in application package is not a smart idea**. But when you absolutely need to, this is the best method to hide it.

## Install

Make sure to apply the Gradle plugin as explained in the [ReadMe](README.md#install)

```gradle
sekret {
    properties {
        enabled.set(true) // REQUIRED!!!
    }
}
```

## Custom Configuration

You can modify the behavior lightly by changing the following properties in the gradle plugin.

```gradle
sekret {
    properties {
        packageName.set("your.package.name") // default: dev.datlag.sekret
        encryptionKey.set("YourCustomEncryptionKey") // default is the specified packageName
        propertiesFile.set(File("custom/path/to/file.properties")) // default: sekret.properties in the current module folder
        
        nativeCopy {
            androidJNIFolder.set(project.layout.projectDirectory.dir("src/androidMain/jniLibs")) // REQUIRED if targeting android
            desktopComposeResourcesFolder.set(project.layout.projectDirectory.dir("src/jvmMain/resources")) // for targeting desktop compose
        }
    }
}
```

### Desktop Compose

When targeting Compose on desktop you should read the [Packaging resources](https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#packaging-resources).

Set the same `appResourcesRootDir` to `desktopComposeResourcesFolder` to work with native binaries correctly.

## Setup

The plugin will generate a separate module, since it needs to compile for native targets even if you just use Android or JVM for example.

### Add generated sekret module

Therefore, you need to include the generated sekret module in your `settings.gradle.kts`.

⚠️ Make sure to exclude this module by adding it to your `.gitignore`

Example: `composeApp/sekret/src`

```gradle
include(":composeApp", ":composeApp:sekret")
```

### Add secrets

Create a `sekret.properties` file in the project/module where you applied the plugin and simply add key-value pairs.

⚠️ Make sure to exclude this file by adding it to your `.gitignore`

Example: `composeApp/sekret.properties`

```properties
YOUR_KEY_NAME=yourSuperSecretSecret
OTHER_SECRET=th1s1s4n0th3rS3cr3t
```

### Generate secrets

To generate source code for your provided secrets, just call `./gradlew generateSekret`.

### Customize targets

You can change the generated `build.gradle.kts` depending on your needs, just make sure to apply the required native targets:

- For **Android**: `androidNativeX`
- For **JVM**: `linuxX`, `mingwX` and `macOsX`, depending on your output platform.

If you misconfigured your build script or added new targets to your base module, you can run `./gradlew composeApp:generateSekretBuildScript`.

## Load secrets in code

- If you use **Android** or **JVM** target you have to call `./gradlew createAndCopySekretNativeLibrary`.
- If you use any other target you can just use the generated `Sekret` class as is.

```kotlin
// Android Code
val binaryLoaded = NativeLoader.loadLibrary("sekret")

// Desktop Code
val binaryLoaded = NativeLoader.loadLibrary("sekret", System.getProperty("compose.application.resources.dir")?.let { File(it) })

// Other Targets like JS, WASM, Native
val binaryLoaded = true // always

if (binaryLoaded) {
  val yourKeyName = Sekret.yourKeyName("YourCustomEncryptionKey")
  val otherSecret = Sekret.otherSecret("YourCustomEncryptionKey")
}
```